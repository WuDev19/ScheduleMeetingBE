package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.EventType;
import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.unavailability_room.CreateUnavailabilityRoomRequest;
import com.example.schedulemeetingbe.dto.request.unavailability_room.UnavailabilityRoomFilterRequest;
import com.example.schedulemeetingbe.dto.request.unavailability_room.UpdateUnavailabilityRoomRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.UnavailabilityRoomResponse;
import com.example.schedulemeetingbe.dto.response.booking.booking_notification.BookingAndEmailAttendeeResponse;
import com.example.schedulemeetingbe.dto.response.booking.booking_notification.BookingAndUserResponse;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.payload.SimpleCancelBookingPayload;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.mapper.RoomMapper;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.repository.RoomRepository;
import com.example.schedulemeetingbe.repository.UnavailabilityRoomRepository;
import com.example.schedulemeetingbe.repository.specification.UnavailabilityRoomSpecification;
import com.example.schedulemeetingbe.service.base.IBookingAttendeeService;
import com.example.schedulemeetingbe.service.base.IBookingService;
import com.example.schedulemeetingbe.service.base.INotificationService;
import com.example.schedulemeetingbe.service.base.IUnavailabilityRoomService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnavailabilityRoomServiceImpl implements IUnavailabilityRoomService {

    private final UnavailabilityRoomRepository unavailabilityRoomRepository;
    private final RoomRepository roomRepository;
    private final OutboxEventRepository outboxEventRepository;

    private final IBookingAttendeeService iBookingAttendeeService;
    private final INotificationService iNotificationService;
    private final IBookingService iBookingService;

    private final JsonMapper jsonMapper;

    @Transactional
    @Override
    public UnavailabilityRoomResponse create(CreateUnavailabilityRoomRequest request) {
        Room room = roomRepository.findById(request.roomId()).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));

        List<Long> bookingIds = request.bookingIdOverLap();
        if (bookingIds != null && !bookingIds.isEmpty()) {
            createNotificationAndSendEmail(bookingIds, request);
        }
        RoomUnavailability roomUnavailability = RoomUnavailability.builder()
                .room(room)
                .reason(request.reason())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();
        RoomUnavailability saved = unavailabilityRoomRepository.save(roomUnavailability);
        return RoomMapper.mapToUnavailabilityRoomResponse(saved);
    }

    private void createNotificationAndSendEmail(List<Long> bookingIds, CreateUnavailabilityRoomRequest request) {
        Map<Long, Booking> bookingLookUp = iBookingService.getBookingInBookingIds(bookingIds)
                .stream()
                .collect(Collectors.toMap(Booking::getBookingId, Function.identity()));
        //lấy danh sách users của booking để gửi notification
        Map<Long, List<User>> usersLookUp = iBookingAttendeeService.getAttendOfBooking(bookingIds)
                .stream()
                .collect(
                        Collectors.groupingBy(
                                BookingAndUserResponse::bookingId,
                                Collectors.mapping(
                                        BookingAndUserResponse::user,
                                        Collectors.toList()
                                )
                        )
                );
        //lấy danh sách email của user tham gia booking để gửi email
        Map<Long, List<String>> userEmailsLookUp = iBookingAttendeeService.getEmailAttendOfBooking(bookingIds)
                .stream()
                .collect(
                        Collectors.groupingBy(
                                BookingAndEmailAttendeeResponse::bookingId,
                                Collectors.mapping(
                                        BookingAndEmailAttendeeResponse::email,
                                        Collectors.toList()
                                )
                        )
                );
        createNotification(bookingIds, bookingLookUp, usersLookUp, request);
        sendEmailToAttendee(bookingIds, bookingLookUp, userEmailsLookUp, request);
    }

    private void createNotification(
            List<Long> bookingIds,
            Map<Long, Booking> bookingLookUp,
            Map<Long, List<User>> usersLookUp,
            CreateUnavailabilityRoomRequest request
    ) {
        List<Notification> notifications = new ArrayList<>();
        bookingIds.forEach(bookingId -> {
            List<User> userAttendee = usersLookUp.get(bookingId);
            Booking booking = bookingLookUp.get(bookingId);
            String message = """
                    Lịch họp "%s" diễn ra vào lúc %s và kết thúc lúc %s đã bị hủy.
                    Lý do: %s
                    """.formatted(
                    booking.getTitle(),
                    TimeUtils.dateTimeFormat(booking.getStartTime()),
                    TimeUtils.dateTimeFormat(booking.getEndTime()),
                    request.reason()
            );
            userAttendee.forEach(user -> {
                Notification notification = Notification.builder()
                        .user(user)
                        .booking(booking)
                        .title(StringCommon.TITLE_NOTIFICATION_CANCEL_BOOKING)
                        .message(message)
                        .build();
                notifications.add(notification);
            });
        });
        iNotificationService.save(notifications);
    }

    private void sendEmailToAttendee(
            List<Long> bookingIds,
            Map<Long, Booking> bookingLookUp,
            Map<Long, List<String>> userEmailsLookUp,
            CreateUnavailabilityRoomRequest request
    ) {
        List<OutboxEvent> outboxEvents = new ArrayList<>();
        bookingIds.forEach(bookingId -> {
            String startTime = TimeUtils.dateTimeFormat(bookingLookUp.get(bookingId).getStartTime());
            String endTime = TimeUtils.dateTimeFormat(bookingLookUp.get(bookingId).getEndTime());
            SimpleCancelBookingPayload payload = SimpleCancelBookingPayload.builder()
                    .reason(request.reason())
                    .receivers(userEmailsLookUp.get(bookingId))
                    .bookingId(bookingId)
                    .startTime(startTime)
                    .endTime(endTime)
                    .title(StringCommon.TITLE_NOTIFICATION_CANCEL_BOOKING)
                    .build();
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventType(EventType.CANCEL_BOOKING_BY_MAINTENANCE_TO_ATTENDEE.name())
                    .status(OutboxStatus.PENDING)
                    .payload(jsonMapper.valueToTree(payload))
                    .build();
            outboxEvents.add(outboxEvent);
        });
        outboxEventRepository.saveAll(outboxEvents);
    }

    @Transactional
    @Override
    public UnavailabilityRoomResponse update(Long id, UpdateUnavailabilityRoomRequest request) {
        RoomUnavailability roomUnavailability = unavailabilityRoomRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (request.reason() != null) {
            roomUnavailability.setReason(request.reason());
        }
        if (request.startTime() != null) {
            roomUnavailability.setStartTime(request.startTime());
        }
        if (request.endTime() != null) {
            roomUnavailability.setEndTime(request.endTime());
        }
        return RoomMapper.mapToUnavailabilityRoomResponse(roomUnavailability);
    }

    @Transactional
    @Override
    public Map<String, Object> delete(Long id) {
        boolean exist = unavailabilityRoomRepository.existsById(id);
        if (!exist) {
            throw new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND);
        }
        unavailabilityRoomRepository.deleteById(id);
        return CRUDResponseHelper.deleteSuccess();
    }

    @Transactional
    @Override
    public Map<String, Object> softDelete(Long id) {
        RoomUnavailability roomUnavailability = unavailabilityRoomRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        roomUnavailability.setIsDeleted(true);
        return CRUDResponseHelper.deleteSuccess();
    }

    @Override
    public UnavailabilityRoomResponse getDetail(Long id) {
        RoomUnavailability roomUnavailability = unavailabilityRoomRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        return RoomMapper.mapToUnavailabilityRoomResponse(roomUnavailability);
    }

    @Override
    public PageResponse<UnavailabilityRoomResponse> getAll(Pageable pageable) {
        Page<RoomUnavailability> page = unavailabilityRoomRepository.findAll(pageable);
        return new PageResponse<>(
                page.getNumber(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getContent().stream()
                        .map(RoomMapper::mapToUnavailabilityRoomResponse)
                        .toList()
        );
    }

    @Override
    public PageResponse<UnavailabilityRoomResponse> search(String keyword, Pageable pageable) {
        Page<RoomUnavailability> roomPage = unavailabilityRoomRepository.findByReasonContainingIgnoreCase(
                keyword,
                pageable
        );
        return new PageResponse<>(
                roomPage.getNumber(),
                roomPage.getNumberOfElements(),
                roomPage.getTotalElements(),
                roomPage.getTotalPages(),
                roomPage.getContent().stream()
                        .map(RoomMapper::mapToUnavailabilityRoomResponse)
                        .toList()
        );
    }

    @Override
    public PageResponse<UnavailabilityRoomResponse> filter(UnavailabilityRoomFilterRequest request, Pageable pageable) {
        Page<RoomUnavailability> roomPage = unavailabilityRoomRepository.findAll(
                UnavailabilityRoomSpecification.filter(
                        request.start(),
                        request.end()),
                pageable
        );
        return new PageResponse<>(
                roomPage.getNumber(),
                roomPage.getNumberOfElements(),
                roomPage.getTotalElements(),
                roomPage.getTotalPages(),
                roomPage.getContent().stream()
                        .map(RoomMapper::mapToUnavailabilityRoomResponse)
                        .toList()
        );
    }

}
