package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.design_pattern.strategy.recurring.RecurrenceStrategyFactory;
import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.dto.request.recurrence.ApproveRejectRecurringRequest;
import com.example.schedulemeetingbe.dto.request.recurrence.CancelRecurringPatternRequest;
import com.example.schedulemeetingbe.dto.request.recurrence.RecurringPatternFilterRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.booking.BookingRecurrenceResponse;
import com.example.schedulemeetingbe.dto.response.recurrence.*;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.helper.RecurrenceHelper;
import com.example.schedulemeetingbe.mapper.RecurringPatternMapper;
import com.example.schedulemeetingbe.repository.BookingRepository;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.repository.RecurringPatternRepository;
import com.example.schedulemeetingbe.repository.specification.RecurringPatternSpecification;
import com.example.schedulemeetingbe.service.base.INotificationService;
import com.example.schedulemeetingbe.service.base.IRecurringPatternService;
import com.example.schedulemeetingbe.service.base.IRoomService;
import com.example.schedulemeetingbe.service.base.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurringPatternServiceImpl implements IRecurringPatternService {

    private final RecurringPatternRepository recurringPatternRepository;
    private final BookingRepository bookingRepository;
    private final OutboxEventRepository outboxEventRepository;

    private final IUserService iUserService;
    private final IRoomService iRoomService;
    private final INotificationService iNotificationService;

    private final RecurrenceStrategyFactory factory;
    private final JsonMapper jsonMapper;

    @Transactional
    @Override
    public RecurringPatternResponse createRecurring(RecurringPatternCreateRequest request, Long userId) {
        User register = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        Room room = iRoomService.getRoomDetail(request.roomId())
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        RecurringPattern recurringPattern = RecurringPatternMapper.mapToRecurringPattern(request, register);
        RecurringPattern recurSaved = recurringPatternRepository.save(recurringPattern);
        factory.getStrategy(request.type()).create(recurSaved, request, register, room);
        return RecurringPatternMapper.mapToRecurringPatternResponse(recurSaved, userId, register.getFullName());
    }

    @Transactional
    @Override
    public CancelRecurrenceResponse cancelRecurring(Long recurringId, CancelRecurringPatternRequest request) {
        RecurringPattern recurringPattern = recurringPatternRepository.findById(recurringId)
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        recurringPattern.setStatus(BookingStatus.CANCELLED);
        bookingRepository.cancelBookingByRecurringPattern(recurringId, request.reason(), OffsetDateTime.now());
        return RecurringPatternMapper.mapToCancelRecurrenceResponse(recurringId, request.reason());
    }

    @Transactional
    @Override
    public ApproveRejectRecurrenceResponse approveOrRejectRecurring(Long recurringId, Long approverId, ApproveRejectRecurringRequest request) {
        RecurringPattern recurringPattern = recurringPatternRepository.findById(recurringId)
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        recurringPattern.setStatus(request.status());
        bookingRepository.approveOrRejectBookingByRecurringPattern(
                recurringId,
                request.status(),
                approverId,
                OffsetDateTime.now()
        );
        RecurrenceHelper.createNotificationAndOutboxEvent(
                request,
                recurringPattern,
                iNotificationService,
                jsonMapper,
                outboxEventRepository
        );
        return RecurringPatternMapper.mapToApproveOrRejectRecurrenceResponse(recurringId, request.status());
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<RecurringPatternResponse> getRecurringPatternWaiting(Pageable pageable) {
        Page<RecurringPatternProjection> page = recurringPatternRepository.getRecurringPatternWaiting(pageable);
        List<Long> recurringIds = page.getContent()
                .stream()
                .map(RecurringPatternProjection::getRecurringId)
                .toList();
        Map<Long, List<BookingRecurrenceResponse>> result = bookingRepository
                .getBookingByRecurrence(recurringIds)
                .stream()
                .collect(Collectors.groupingBy(BookingRecurrenceResponse::recurringId));
        List<RecurringPatternResponse> responses = page.getContent()
                .stream()
                .map(projection ->
                        RecurringPatternMapper.mapToRecurringPatternResponse(
                                projection,
                                result.get(projection.getRecurringId())
                        )
                )
                .toList();
        return new PageResponse<>(
                page.getNumber(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                responses

        );
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<RecurringPatternResponse> getMyRecurringPattern(Long userId, Pageable pageable) {
        Page<RecurringPatternProjection> page = recurringPatternRepository.getMyRecurringPattern(userId, pageable);
        List<Long> recurringIds = page.getContent()
                .stream()
                .map(RecurringPatternProjection::getRecurringId)
                .toList();
        Map<Long, List<BookingRecurrenceResponse>> result = bookingRepository
                .getBookingByRecurrence(recurringIds)
                .stream()
                .collect(Collectors.groupingBy(BookingRecurrenceResponse::recurringId));
        List<RecurringPatternResponse> responses = page.getContent()
                .stream()
                .map(projection ->
                        RecurringPatternMapper.mapToRecurringPatternResponse(
                                projection,
                                result.get(projection.getRecurringId())
                        )
                )
                .toList();
        return new PageResponse<>(
                page.getNumber(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                responses
        );
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<RecurringPatternResponse> filter(Long userId, List<String> permissions, RecurringPatternFilterRequest request, Pageable pageable) {
        Set<String> stringSet = new HashSet<>(permissions);
        Page<RecurringPattern> page = recurringPatternRepository.findAll(
                RecurringPatternSpecification.filter(userId, stringSet, request),
                pageable
        );
        List<Long> recurringIds = page.getContent()
                .stream()
                .map(RecurringPattern::getRecurringId)
                .toList();
        Map<Long, List<BookingRecurrenceResponse>> result = bookingRepository
                .getBookingByRecurrence(recurringIds)
                .stream()
                .collect(Collectors.groupingBy(BookingRecurrenceResponse::recurringId));
        Map<Long, RecurrenceUserResponse> recurrenceUser = recurringPatternRepository
                .getUserCreatedRecurrence(recurringIds)
                .stream()
                .collect(Collectors.toMap(RecurrenceUserResponse::recurringId, Function.identity()));
        List<RecurringPatternResponse> responses = page.getContent()
                .stream()
                .map(recurringPattern ->
                        RecurringPatternMapper.mapToRecurringPatternResponse(
                                recurringPattern,
                                recurrenceUser.get(recurringPattern.getRecurringId()),
                                result.get(recurringPattern.getRecurringId())
                        )
                )
                .toList();
        return new PageResponse<>(
                page.getNumber(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                responses
        );
    }

}