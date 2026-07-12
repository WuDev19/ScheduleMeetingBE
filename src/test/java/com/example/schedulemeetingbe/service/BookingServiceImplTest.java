package com.example.schedulemeetingbe.service;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.response.booking.BookingDetailResponse;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.constant.enums.BookingViewType;
import com.example.schedulemeetingbe.design_pattern.command.booking.approve.BookingApproveCommandFactory;
import com.example.schedulemeetingbe.design_pattern.command.booking.rollback.BookingRollbackCommandFactory;
import com.example.schedulemeetingbe.dto.request.booking.*;
import com.example.schedulemeetingbe.dto.response.booking.BookingResponse;
import com.example.schedulemeetingbe.dto.response.booking.StatusBookingResponse;
import com.example.schedulemeetingbe.dto.response.equipment.EquipmentAndQuantityResponse;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.composite_key.BookingAttendeeId;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.exception.custom_exception.OverlapBookingException;
import com.example.schedulemeetingbe.repository.*;
import com.example.schedulemeetingbe.service.base.*;
import com.example.schedulemeetingbe.service.impl.BookingServiceImpl;
import com.example.schedulemeetingbe.utils.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl Unit Tests")
class BookingServiceImplTest {

    // ======================= MOCKS =======================

    @Mock BookingRepository bookingRepository;
    @Mock BookingEquipmentRepository bookingEquipmentRepository;
    @Mock BookingHistoryRepository bookingHistoryRepository;
    @Mock BookingReservationRepository bookingReservationRepository;
    @Mock BookingEquipmentReservationRepository bookingEquipmentReservationRepository;
    @Mock VerificationTokenRepository verificationTokenRepository;
    @Mock OutboxEventRepository outboxEventRepository;
    @Mock BookingAttendeeRepository bookingAttendeeRepository;

    @Mock IUserService iUserService;
    @Mock IRoomService iRoomService;
    @Mock IEquipmentService iEquipmentService;
    @Mock INotificationService iNotificationService;

    @Mock JsonMapper jsonMapper;
    @Mock BookingRollbackCommandFactory rollbackFactory;
    @Mock BookingApproveCommandFactory approveFactory;
    @Mock org.redisson.api.RLock mockLock;
    @Mock org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @InjectMocks
    BookingServiceImpl bookingService;

    // ======================= FIXTURES =======================

    private static final Long USER_ID = 1L;
    private static final Long ROOM_ID = 10L;
    private static final Long BOOKING_ID = 100L;

    private static final OffsetDateTime NOW = OffsetDateTime.of(2026, 7, 4, 10, 0, 0, 0, ZoneOffset.of("+07:00"));
    private static final OffsetDateTime FUTURE_START = NOW.plusHours(2);
    private static final OffsetDateTime FUTURE_END = NOW.plusHours(3);
    private static final OffsetDateTime PAST = NOW.minusHours(1);

    private User mockUser;
    private Room mockRoom;
    private Building mockBuilding;
    private Booking mockBooking;

    @BeforeEach
    void setUp() {
        mockBuilding = Building.builder()
                .buildingId(1L)
                .buildingName("Tòa A")
                .address("123 Đường ABC")
                .build();

        mockRoom = Room.builder()
                .roomId(ROOM_ID)
                .roomName("Phòng 101")
                .capacity(10)
                .floorNumber(1)
                .building(mockBuilding)
                .build();

        mockUser = User.builder()
                .userId(USER_ID)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .build();

        mockBooking = Booking.builder()
                .bookingId(BOOKING_ID)
                .bookedBy(mockUser)
                .room(mockRoom)
                .title("Test Meeting")
                .startTime(FUTURE_START)
                .endTime(FUTURE_END)
                .attendeeCount(5)
                .status(BookingStatus.PENDING)
                .build();

        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            org.springframework.transaction.support.TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        lenient().when(iRoomService.getRoomDateLock(anyLong(), any())).thenReturn(mockLock);
        lenient().when(iRoomService.getRoomDatesLock(any())).thenReturn(mockLock);
        try {
            lenient().when(mockLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
        } catch (InterruptedException e) {
            // ignore
        }
        lenient().when(mockLock.isHeldByCurrentThread()).thenReturn(true);
    }

    // =====================================================
    //                   CREATE BOOKING
    // =====================================================

    @Nested
    @DisplayName("createBooking()")
    class CreateBookingTests {

        // Record fields: roomId, title, description, start, end, attendee, equipments, receivers
        private CreateBookingRequest buildRequest(OffsetDateTime start, OffsetDateTime end,
                                                  int attendee, List<String> receivers) {
            return new CreateBookingRequest(
                    ROOM_ID, "Họp nhóm", "Mô tả", start, end,
                    attendee, null, receivers
            );
        }

        @Test
        @DisplayName("Ném START_END_DATE_BEFORE_NOW_ERROR khi start trước thời điểm hiện tại")
        void createBooking_WhenStartBeforeNow_ShouldThrowBusinessException() {
            CreateBookingRequest request = buildRequest(PAST, FUTURE_END, 1, null);

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);

                assertThatThrownBy(() -> bookingService.createBooking(request, USER_ID))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.START_END_DATE_BEFORE_NOW_ERROR));
            }
        }

        @Test
        @DisplayName("Ném INCONSISTENCY_ATTENDEE khi receivers.size() != attendee")
        void createBooking_WhenReceiversSizeMismatch_ShouldThrowBusinessException() {
            // attendee = 2, nhưng chỉ có 1 receiver
            CreateBookingRequest request = buildRequest(FUTURE_START, FUTURE_END, 2, List.of("other@example.com"));

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);

                assertThatThrownBy(() -> bookingService.createBooking(request, USER_ID))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.INCONSISTENCY_ATTENDEE));
            }
        }

        @Test
        @DisplayName("Ném START_END_DATE_ERROR khi start sau end")
        void createBooking_WhenStartAfterEnd_ShouldThrowBusinessException() {
            // start > end
            CreateBookingRequest request = buildRequest(FUTURE_END, FUTURE_START, 1, null);

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);

                assertThatThrownBy(() -> bookingService.createBooking(request, USER_ID))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.START_END_DATE_ERROR));
            }
        }

        @Test
        @DisplayName("Ném RESOURCE_NOT_FOUND khi user không tồn tại")
        void createBooking_WhenUserNotFound_ShouldThrowBusinessException() {
            CreateBookingRequest request = buildRequest(FUTURE_START, FUTURE_END, 1, null);

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);
                when(iUserService.getDetail(USER_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> bookingService.createBooking(request, USER_ID))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.RESOURCE_NOT_FOUND));
            }
        }

        @Test
        @DisplayName("Ném RESOURCE_NOT_FOUND khi room không tồn tại")
        void createBooking_WhenRoomNotFound_ShouldThrowBusinessException() {
            CreateBookingRequest request = buildRequest(FUTURE_START, FUTURE_END, 1, null);

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);
                when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
                when(iRoomService.getRoomDetail(ROOM_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> bookingService.createBooking(request, USER_ID))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.RESOURCE_NOT_FOUND));
            }
        }

        @Test
        @DisplayName("Ném EXCEED_ATTENDEE khi attendee vượt capacity phòng")
        void createBooking_WhenAttendeesExceedCapacity_ShouldThrowBusinessException() {
            // attendee = 15, capacity = 10
            CreateBookingRequest request = buildRequest(FUTURE_START, FUTURE_END, 15, null);

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);
                when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
                when(iRoomService.getRoomDetail(ROOM_ID)).thenReturn(Optional.of(mockRoom));

                assertThatThrownBy(() -> bookingService.createBooking(request, USER_ID))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.EXCEED_ATTENDEE));
            }
        }

        @Test
        @DisplayName("Ném OverlapBookingException khi lịch bị trùng")
        void createBooking_WhenTimeOverlaps_ShouldThrowOverlapBookingException() {
            CreateBookingRequest request = buildRequest(FUTURE_START, FUTURE_END, 5, null);

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);
                when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
                when(iRoomService.getRoomDetail(ROOM_ID)).thenReturn(Optional.of(mockRoom));
                when(bookingRepository.checkOverlap(eq(ROOM_ID), any(String[].class)))
                        .thenReturn(List.of("Trùng với lịch hiện tại"));

                assertThatThrownBy(() -> bookingService.createBooking(request, USER_ID))
                        .isInstanceOf(OverlapBookingException.class);
            }
        }

        @Test
        @DisplayName("Tạo booking thành công và lưu vào repository")
        void createBooking_HappyPath_ShouldSaveAndReturnResponse() {
            CreateBookingRequest request = buildRequest(FUTURE_START, FUTURE_END, 5, null);

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);
                when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
                when(iRoomService.getRoomDetail(ROOM_ID)).thenReturn(Optional.of(mockRoom));
                when(bookingRepository.checkOverlap(eq(ROOM_ID), any(String[].class)))
                        .thenReturn(Collections.emptyList());
                when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
                when(jsonMapper.valueToTree(any())).thenReturn(null);

                BookingResponse response = bookingService.createBooking(request, USER_ID);

                assertThat(response).isNotNull();
                verify(bookingRepository).save(any(Booking.class));
                verify(bookingHistoryRepository).save(any(BookingHistory.class));
            }
        }

        @Test
        @DisplayName("Tạo booking với thiết bị thành công")
        void createBooking_WithEquipment_HappyPath_ShouldSaveBookingAndEquipments() {
            CreateBookingEquipmentRequest equipReq = new CreateBookingEquipmentRequest(1L, 5);
            CreateBookingRequest request = new CreateBookingRequest(
                    ROOM_ID, "Họp nhóm", "Mô tả", FUTURE_START, FUTURE_END,
                    5, List.of(equipReq), null
            );

            Equipment mockEquip = Equipment.builder().equipmentId(1L).equipmentName("Projector").totalQuantity(15).build();
            EquipmentAndQuantityResponse eqQtyResp = new EquipmentAndQuantityResponse(1L, "Projector", 10);

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);
                when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
                when(iRoomService.getRoomDetail(ROOM_ID)).thenReturn(Optional.of(mockRoom));
                when(bookingRepository.checkOverlap(eq(ROOM_ID), any(String[].class))).thenReturn(Collections.emptyList());
                when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
                
                when(iEquipmentService.findEquipmentAndRemainingQuantity(anyList())).thenReturn(List.of(eqQtyResp));
                when(iEquipmentService.getEquipmentDetail(1L)).thenReturn(Optional.of(mockEquip));
                when(iEquipmentService.findEquipmentIn(anyList())).thenReturn(List.of(mockEquip));

                BookingResponse response = bookingService.createBooking(request, USER_ID);

                assertThat(response).isNotNull();
                verify(iEquipmentService).lockEquipment(anyList());
                verify(bookingEquipmentRepository).saveAll(anyList());
            }
        }

        @Test
        @DisplayName("Ném ExceedEquipmentException khi số lượng thiết bị yêu cầu vượt quá số lượng trống")
        void createBooking_WithEquipmentExceedsQuantity_ShouldThrowExceedEquipmentException() {
            CreateBookingEquipmentRequest equipReq = new CreateBookingEquipmentRequest(1L, 12);
            CreateBookingRequest request = new CreateBookingRequest(
                    ROOM_ID, "Họp nhóm", "Mô tả", FUTURE_START, FUTURE_END,
                    5, List.of(equipReq), null
            );

            Equipment mockEquip = Equipment.builder().equipmentId(1L).equipmentName("Projector").totalQuantity(15).build();
            EquipmentAndQuantityResponse eqQtyResp = new EquipmentAndQuantityResponse(1L, "Projector", 10);

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);
                when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
                when(iRoomService.getRoomDetail(ROOM_ID)).thenReturn(Optional.of(mockRoom));
                when(bookingRepository.checkOverlap(eq(ROOM_ID), any(String[].class))).thenReturn(Collections.emptyList());
                when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
                
                when(iEquipmentService.findEquipmentAndRemainingQuantity(anyList())).thenReturn(List.of(eqQtyResp));
                when(iEquipmentService.getEquipmentDetail(1L)).thenReturn(Optional.of(mockEquip));

                assertThatThrownBy(() -> bookingService.createBooking(request, USER_ID))
                        .isInstanceOf(com.example.schedulemeetingbe.exception.custom_exception.ExceedEquipmentException.class);
            }
        }
    }

    // =====================================================
    //                   UPDATE BOOKING
    // =====================================================

    @Nested
    @DisplayName("updateBooking()")
    class UpdateBookingTests {

        // Record fields: title, description, attendeeCount, start, end, isCompleted, roomId, newRoomId
        private UpdateBookingRequest buildRequest(String title, OffsetDateTime start, OffsetDateTime end) {
            return new UpdateBookingRequest(title, null, null, start, end, null, null, null);
        }

        @Test
        @DisplayName("Ném UPDATE_BOOKING_AUTH_ERROR khi user khác cố sửa và không phải ADMIN")
        void updateBooking_WhenNotOwnerAndNotAdmin_ShouldThrowBusinessException() {
            User anotherUser = User.builder().userId(999L).build();
            when(iUserService.getDetail(999L)).thenReturn(Optional.of(anotherUser));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));

            // mockBooking.bookedBy = mockUser (userId=1), but requester is userId=999, roles không có ADMIN
            assertThatThrownBy(() ->
                    bookingService.updateBooking(BOOKING_ID, buildRequest(null, null, null), 999L, List.of("REGISTER")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.UPDATE_BOOKING_AUTH_ERROR));
        }

        @Test
        @DisplayName("Ném BOOKING_STATUS_ERROR khi booking đã bị REJECTED")
        void updateBooking_WhenBookingIsRejected_ShouldThrowBusinessException() {
            mockBooking.setStatus(BookingStatus.REJECTED);
            when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));

            assertThatThrownBy(() ->
                    bookingService.updateBooking(BOOKING_ID, buildRequest(null, null, null), USER_ID, List.of("REGISTER")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.BOOKING_STATUS_ERROR));
        }

        @Test
        @DisplayName("Ném BOOKING_STATUS_ERROR khi booking đã bị CANCELLED")
        void updateBooking_WhenBookingIsCancelled_ShouldThrowBusinessException() {
            mockBooking.setStatus(BookingStatus.CANCELLED);
            when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));

            assertThatThrownBy(() ->
                    bookingService.updateBooking(BOOKING_ID, buildRequest(null, null, null), USER_ID, List.of()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.BOOKING_STATUS_ERROR));
        }

        @Test
        @DisplayName("Ném COMPLETED_UPDATE_BOOKING_ERROR khi kết thúc cuộc họp chưa diễn ra")
        void updateBooking_WhenMarkCompletedButNotStartedYet_ShouldThrowBusinessException() {
            // isCompleted=true, other fields null → (title=null, desc=null, attendeeCount=null, start=null, end=null, isCompleted=true, roomId=null, newRoomId=null)
            UpdateBookingRequest request = new UpdateBookingRequest(null, null, null, null, null, true, null, null);
            when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(bookingAttendeeRepository.getAttendeeOfBooking(BOOKING_ID)).thenReturn(List.of());

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                // NOW < startTime → chưa diễn ra
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);
                mockBooking.setStartTime(NOW.plusHours(1));

                assertThatThrownBy(() ->
                        bookingService.updateBooking(BOOKING_ID, request, USER_ID, List.of("REGISTER")))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.COMPLETED_UPDATE_BOOKING_ERROR));
            }
        }

        @Test
        @DisplayName("Ném START_END_DATE_BEFORE_NOW_ERROR khi đổi time nhưng start trong quá khứ")
        void updateBooking_WhenNewStartIsInPast_ShouldThrowBusinessException() {
            // start=PAST, end=FUTURE_END → (title=null, desc=null, attendeeCount=null, start=PAST, end=FUTURE_END, isCompleted=null, roomId=null, newRoomId=null)
            UpdateBookingRequest request = new UpdateBookingRequest(null, null, null, PAST, FUTURE_END, null, null, null);
            when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(bookingAttendeeRepository.getAttendeeOfBooking(BOOKING_ID)).thenReturn(List.of());

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);

                assertThatThrownBy(() ->
                        bookingService.updateBooking(BOOKING_ID, request, USER_ID, List.of("REGISTER")))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.START_END_DATE_BEFORE_NOW_ERROR));
            }
        }

        @Test
        @DisplayName("Ném START_END_DATE_ERROR khi đổi time nhưng start > end")
        void updateBooking_WhenNewStartAfterEnd_ShouldThrowBusinessException() {
            // start > end
            // start=FUTURE_END > end=FUTURE_START → (title=null, desc=null, attendeeCount=null, start=FUTURE_END, end=FUTURE_START, isCompleted=null, roomId=null, newRoomId=null)
            UpdateBookingRequest request = new UpdateBookingRequest(null, null, null, FUTURE_END, FUTURE_START, null, null, null);
            when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(bookingAttendeeRepository.getAttendeeOfBooking(BOOKING_ID)).thenReturn(List.of());

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);

                assertThatThrownBy(() ->
                        bookingService.updateBooking(BOOKING_ID, request, USER_ID, List.of("REGISTER")))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.START_END_DATE_ERROR));
            }
        }

        @Test
        @DisplayName("Update chỉ title thành công (không đổi room/time)")
        void updateBooking_WhenOnlyTitleChanged_ShouldSucceed() {
            UpdateBookingRequest request = new UpdateBookingRequest("New Title", null, null, null, null, null, null, null);
            when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(bookingAttendeeRepository.getAttendeeOfBooking(BOOKING_ID)).thenReturn(List.of());
            when(jsonMapper.valueToTree(any())).thenReturn(null);

            Map<String, Long> result = bookingService.updateBooking(BOOKING_ID, request, USER_ID, List.of("REGISTER"));

            assertThat(result).containsKey("bookingId");
            assertThat(result.get("bookingId")).isEqualTo(BOOKING_ID);
            verify(bookingHistoryRepository).save(any(BookingHistory.class));
        }
    }

    // =====================================================
    //                   CANCEL BOOKING
    // =====================================================

    @Nested
    @DisplayName("cancelBooking()")
    class CancelBookingTests {

        private final CancelBookingRequest cancelRequest = new CancelBookingRequest("Lý do hủy");

        @Test
        @DisplayName("Ném UPDATE_BOOKING_ERROR khi hủy trong vòng 30 phút trước khi bắt đầu")
        void cancelBooking_WhenWithin30MinutesOfStart_ShouldThrowBusinessException() {
            // startTime = NOW + 20 phút → còn 20 phút → < 30 phút
            mockBooking.setStartTime(NOW.plusMinutes(20));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);

                assertThatThrownBy(() -> bookingService.cancelBooking(BOOKING_ID, cancelRequest, USER_ID))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.UPDATE_BOOKING_ERROR));
            }
        }

        @Test
        @DisplayName("Ném BOOKING_CANCEL_ERROR khi booking không ở trạng thái PENDING hoặc APPROVED")
        void cancelBooking_WhenStatusIsNotCancellable_ShouldThrowBusinessException() {
            mockBooking.setStartTime(NOW.plusHours(2));
            mockBooking.setStatus(BookingStatus.COMPLETED);
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);

                assertThatThrownBy(() -> bookingService.cancelBooking(BOOKING_ID, cancelRequest, USER_ID))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.BOOKING_CANCEL_ERROR));
            }
        }

        @Test
        @DisplayName("Ném BOOKING_CANCEL_ERROR khi booking đã bị CANCELLED")
        void cancelBooking_WhenAlreadyCancelled_ShouldThrowBusinessException() {
            mockBooking.setStartTime(NOW.plusHours(2));
            mockBooking.setStatus(BookingStatus.CANCELLED);
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);

                assertThatThrownBy(() -> bookingService.cancelBooking(BOOKING_ID, cancelRequest, USER_ID))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.BOOKING_CANCEL_ERROR));
            }
        }

        @Test
        @DisplayName("Hủy booking PENDING thành công khi trước 30 phút")
        void cancelBooking_HappyPath_ShouldSetStatusCancelledAndSaveHistory() {
            mockBooking.setStartTime(NOW.plusHours(2));
            mockBooking.setStatus(BookingStatus.PENDING);
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(bookingAttendeeRepository.getAttendeeOfBooking(BOOKING_ID)).thenReturn(List.of());
            when(bookingAttendeeRepository.getEmailAttendee(BOOKING_ID)).thenReturn(List.of());
            when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
            when(iNotificationService.save(anyList())).thenReturn(null);
            when(jsonMapper.valueToTree(any())).thenReturn(null);

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);

                StatusBookingResponse response = bookingService.cancelBooking(BOOKING_ID, cancelRequest, USER_ID);

                assertThat(mockBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
                assertThat(mockBooking.getCancellationReason()).isEqualTo("Lý do hủy");
                verify(bookingHistoryRepository).save(any(BookingHistory.class));
                verify(outboxEventRepository).save(any(OutboxEvent.class));
            }
        }
    }

    // =====================================================
    //              VERIFY EMAIL & UPSERT ATTENDEE
    // =====================================================

    @Nested
    @DisplayName("verifyEmailAndUpsertBookingAttendee()")
    class VerifyEmailAndUpsertAttendeeTests {

        private VerificationToken buildToken(boolean verified, boolean revoked, OffsetDateTime expiresAt) {
            return VerificationToken.builder()
                    .token("test-token")
                    .verified(verified)
                    .revoked(revoked)
                    .expiresAt(expiresAt)
                    .user(mockUser)
                    .build();
        }

        @Test
        @DisplayName("Token không tồn tại → ném EMAIL_LINK_UNAVAILABILITY")
        void verify_WhenTokenNotFound_ShouldThrow() {
            when(verificationTokenRepository.findByToken("test-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.verifyEmailAndUpsertBookingAttendee("test-token", BOOKING_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.EMAIL_LINK_UNAVAILABILITY));
        }

        @Test
        @DisplayName("Token đã verified → return sớm, không làm gì thêm")
        void verify_WhenTokenAlreadyVerified_ShouldReturnEarly() {
            VerificationToken token = buildToken(true, false, NOW.plusHours(1));
            when(verificationTokenRepository.findByToken("test-token")).thenReturn(Optional.of(token));

            bookingService.verifyEmailAndUpsertBookingAttendee("test-token", BOOKING_ID);

            // Không gọi bookingRepository vì return sớm
            verify(bookingRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Token đã bị revoked → ném VERIFY_TOKEN_REVOKED")
        void verify_WhenTokenRevoked_ShouldThrow() {
            VerificationToken token = buildToken(false, true, NOW.plusHours(1));
            when(verificationTokenRepository.findByToken("test-token")).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> bookingService.verifyEmailAndUpsertBookingAttendee("test-token", BOOKING_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.VERIFY_TOKEN_REVOKED));
        }

        @Test
        @DisplayName("Token hết hạn → revoke token và ném VERIFY_TOKEN_EXPIRED")
        void verify_WhenTokenExpired_ShouldRevokeAndThrow() {
            // expiresAt = quá khứ
            VerificationToken token = buildToken(false, false, NOW.minusHours(1));
            when(verificationTokenRepository.findByToken("test-token")).thenReturn(Optional.of(token));

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);

                assertThatThrownBy(() -> bookingService.verifyEmailAndUpsertBookingAttendee("test-token", BOOKING_ID))
                        .isInstanceOf(BusinessException.class)
                        .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                                .isEqualTo(ErrorResponse.VERIFY_TOKEN_EXPIRED));
                // Phải set revoked = true
                assertThat(token.getRevoked()).isTrue();
            }
        }

        @Test
        @DisplayName("Token hợp lệ → lưu BookingAttendee và đánh dấu token đã dùng")
        void verify_HappyPath_ShouldSaveAttendeeAndMarkTokenUsed() {
            VerificationToken token = buildToken(false, false, NOW.plusHours(1));
            when(verificationTokenRepository.findByToken("test-token")).thenReturn(Optional.of(token));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);

                bookingService.verifyEmailAndUpsertBookingAttendee("test-token", BOOKING_ID);

                assertThat(token.getVerified()).isTrue();
                assertThat(token.getRevoked()).isTrue();
                verify(bookingAttendeeRepository).save(any(BookingAttendee.class));
            }
        }
    }

    // =====================================================
    //                   CONFIRM PARTICIPATE
    // =====================================================

    @Nested
    @DisplayName("confirmParticipateIn()")
    class ConfirmParticipateInTests {

        @Test
        @DisplayName("Đã tồn tại trong BookingAttendee → return sớm, không lưu lại")
        void confirmParticipateIn_WhenAlreadyExists_ShouldReturnEarly() {
            when(bookingAttendeeRepository.existsById(any(BookingAttendeeId.class))).thenReturn(true);

            bookingService.confirmParticipateIn(BOOKING_ID, USER_ID);

            verify(bookingRepository, never()).findById(any());
            verify(bookingAttendeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Booking không tồn tại → ném RESOURCE_NOT_FOUND")
        void confirmParticipateIn_WhenBookingNotFound_ShouldThrow() {
            when(bookingAttendeeRepository.existsById(any(BookingAttendeeId.class))).thenReturn(false);
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.confirmParticipateIn(BOOKING_ID, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.RESOURCE_NOT_FOUND));
        }

        @Test
        @DisplayName("Happy path → lưu BookingAttendee mới")
        void confirmParticipateIn_HappyPath_ShouldSaveAttendee() {
            when(bookingAttendeeRepository.existsById(any(BookingAttendeeId.class))).thenReturn(false);
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);

                bookingService.confirmParticipateIn(BOOKING_ID, USER_ID);

                verify(bookingAttendeeRepository).save(any(BookingAttendee.class));
            }
        }
    }

    // =====================================================
    //                   ADD PARTICIPANTS
    // =====================================================

    @Nested
    @DisplayName("addParticipants()")
    class AddParticipantsTests {

        @Test
        @DisplayName("Ném BOOKING_STATUS_ERROR khi booking đã bị CANCELLED")
        void addParticipants_WhenBookingCancelled_ShouldThrow() {
            mockBooking.setStatus(BookingStatus.CANCELLED);
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));

            assertThatThrownBy(() -> bookingService.addParticipants(BOOKING_ID, List.of("a@b.com")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.BOOKING_STATUS_ERROR));
        }

        @Test
        @DisplayName("Ném BOOKING_STATUS_ERROR khi booking đã COMPLETED")
        void addParticipants_WhenBookingCompleted_ShouldThrow() {
            mockBooking.setStatus(BookingStatus.COMPLETED);
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));

            assertThatThrownBy(() -> bookingService.addParticipants(BOOKING_ID, List.of("a@b.com")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.BOOKING_STATUS_ERROR));
        }

        @Test
        @DisplayName("Happy path với APPROVED booking → lưu OutboxEvent để gửi email")
        void addParticipants_HappyPath_ShouldSaveOutboxEvent() {
            mockBooking.setStatus(BookingStatus.APPROVED);
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(jsonMapper.valueToTree(any())).thenReturn(null);

            bookingService.addParticipants(BOOKING_ID, List.of("participant@example.com"));

            verify(outboxEventRepository).save(any(OutboxEvent.class));
        }

        @Test
        @DisplayName("Ném BOOKING_STATUS_ERROR khi booking đã bị REJECTED")
        void addParticipants_WhenBookingRejected_ShouldThrow() {
            mockBooking.setStatus(BookingStatus.REJECTED);
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));

            assertThatThrownBy(() -> bookingService.addParticipants(BOOKING_ID, List.of("a@b.com")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.BOOKING_STATUS_ERROR));
        }
    }

    // =====================================================
    //                   VIEW BOOKINGS
    // =====================================================

    @Nested
    @DisplayName("viewBookings()")
    class ViewBookingsTests {

        @Test
        @DisplayName("Ném FIELD_INVALID khi viewType là null")
        void viewBookings_WhenViewTypeIsNull_ShouldThrow() {
            // Record fields: viewType, targetDate, fullName, roomId, status
            BookingViewRequest request = new BookingViewRequest(null, LocalDate.now(), null, null, null);

            assertThatThrownBy(() -> bookingService.viewBookings(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.FIELD_INVALID));
        }

        @Test
        @DisplayName("Ném FIELD_INVALID khi targetDate là null")
        void viewBookings_WhenTargetDateIsNull_ShouldThrow() {
            BookingViewRequest request = new BookingViewRequest(BookingViewType.DAY, null, null, null, null);

            assertThatThrownBy(() -> bookingService.viewBookings(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.FIELD_INVALID));
        }

        @Test
        @DisplayName("viewType = DAY → tính start = đầu ngày, end = cuối ngày")
        void viewBookings_WithDayViewType_ShouldReturnDayRange() {
            LocalDate targetDate = LocalDate.of(2026, 7, 4);
            BookingViewRequest request = new BookingViewRequest(BookingViewType.DAY, targetDate, null, null, null);
            // viewBookings() gọi findAll(Specification) - không có Sort
            when(bookingRepository.findAll(
                    any(org.springframework.data.jpa.domain.Specification.class)))
                    .thenReturn(List.of());

            List<BookingResponse> result = bookingService.viewBookings(request);

            assertThat(result).isEmpty();
            verify(bookingRepository).findAll(
                    any(org.springframework.data.jpa.domain.Specification.class));
        }

        @Test
        @DisplayName("viewType = WEEK → tính từ Thứ 2 đến Chủ Nhật")
        void viewBookings_WithWeekViewType_ShouldReturnWeekRange() {
            LocalDate targetDate = LocalDate.of(2026, 7, 4); // Thứ 7
            BookingViewRequest request = new BookingViewRequest(BookingViewType.WEEK, targetDate, null, null, null);
            when(bookingRepository.findAll(
                    any(org.springframework.data.jpa.domain.Specification.class)))
                    .thenReturn(List.of());

            List<BookingResponse> result = bookingService.viewBookings(request);

            assertThat(result).isEmpty();
            verify(bookingRepository).findAll(
                    any(org.springframework.data.jpa.domain.Specification.class));
        }

        @Test
        @DisplayName("viewType = MONTH → tính từ ngày đầu đến ngày cuối tháng")
        void viewBookings_WithMonthViewType_ShouldReturnMonthRange() {
            LocalDate targetDate = LocalDate.of(2026, 7, 4);
            BookingViewRequest request = new BookingViewRequest(BookingViewType.MONTH, targetDate, null, null, null);
            when(bookingRepository.findAll(
                    any(org.springframework.data.jpa.domain.Specification.class)))
                    .thenReturn(List.of());

            List<BookingResponse> result = bookingService.viewBookings(request);

            assertThat(result).isEmpty();
            verify(bookingRepository).findAll(
                    any(org.springframework.data.jpa.domain.Specification.class));
        }
    }

    // =====================================================
    //                   GET BOOKING DETAIL
    // =====================================================

    @Nested
    @DisplayName("getBookingDetail()")
    class GetBookingDetailTests {

        @Test
        @DisplayName("User không phải owner, không phải ADMIN/APPROVER, không phải attendee → ném BOOKING_DETAIL_ERROR")
        void getBookingDetail_WhenUnauthorized_ShouldThrow() {
            User anotherUser = User.builder().userId(999L).build();
            when(iUserService.getDetail(999L)).thenReturn(Optional.of(anotherUser));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(bookingAttendeeRepository.getAttendeeOfBooking(BOOKING_ID)).thenReturn(List.of());
            // getMyRole trả về REGISTER (không phải ADMIN/APPROVER)
            when(iUserService.getMyRole(999L)).thenReturn(java.util.Set.of(StringCommon.REGISTER));

            assertThatThrownBy(() -> bookingService.getBookingDetail(BOOKING_ID, 999L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.BOOKING_DETAIL_ERROR));
        }

        @Test
        @DisplayName("Owner của booking → xem được chi tiết")
        void getBookingDetail_WhenOwner_ShouldReturnDetail() {
            when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(bookingAttendeeRepository.getAttendeeOfBooking(BOOKING_ID)).thenReturn(List.of());
            when(iUserService.getMyRole(USER_ID)).thenReturn(java.util.Set.of(StringCommon.REGISTER));
            when(bookingEquipmentRepository.getBookingEquipments(BOOKING_ID)).thenReturn(List.of());

            var detail = bookingService.getBookingDetail(BOOKING_ID, USER_ID);

            assertThat(detail).isNotNull();
        }

        @Test
        @DisplayName("ADMIN → xem được chi tiết dù không phải owner")
        void getBookingDetail_WhenAdmin_ShouldReturnDetail() {
            User adminUser = User.builder().userId(99L).username("admin").email("admin@x.com").fullName("Admin").build();
            when(iUserService.getDetail(99L)).thenReturn(Optional.of(adminUser));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(bookingAttendeeRepository.getAttendeeOfBooking(BOOKING_ID)).thenReturn(List.of());
            when(iUserService.getMyRole(99L)).thenReturn(java.util.Set.of(StringCommon.ADMIN));
            when(bookingEquipmentRepository.getBookingEquipments(BOOKING_ID)).thenReturn(List.of());

            var detail = bookingService.getBookingDetail(BOOKING_ID, 99L);

            assertThat(detail).isNotNull();
        }

        @Test
        @DisplayName("APPROVER → xem được chi tiết dù không phải owner")
        void getBookingDetail_WhenApprover_ShouldReturnDetail() {
            User approver = User.builder().userId(88L).username("approver").email("approver@x.com").fullName("Approver").build();
            when(iUserService.getDetail(88L)).thenReturn(Optional.of(approver));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(bookingAttendeeRepository.getAttendeeOfBooking(BOOKING_ID)).thenReturn(List.of());
            when(iUserService.getMyRole(88L)).thenReturn(java.util.Set.of(StringCommon.APPROVER));
            when(bookingEquipmentRepository.getBookingEquipments(BOOKING_ID)).thenReturn(List.of());

            var detail = bookingService.getBookingDetail(BOOKING_ID, 88L);

            assertThat(detail).isNotNull();
        }

        @Test
        @DisplayName("Attendee của booking → xem được chi tiết")
        void getBookingDetail_WhenAttendee_ShouldReturnDetail() {
            User attendee = User.builder().userId(77L).username("attendee").email("att@x.com").fullName("Attendee").build();
            when(iUserService.getDetail(77L)).thenReturn(Optional.of(attendee));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(bookingAttendeeRepository.getAttendeeOfBooking(BOOKING_ID)).thenReturn(List.of(attendee));
            when(iUserService.getMyRole(77L)).thenReturn(java.util.Set.of(StringCommon.REGISTER));
            when(bookingEquipmentRepository.getBookingEquipments(BOOKING_ID)).thenReturn(List.of());

            var detail = bookingService.getBookingDetail(BOOKING_ID, 77L);

            assertThat(detail).isNotNull();
        }
    }

    // =====================================================
    //               DELETE BOOKING (ADMIN)
    // =====================================================

    @Nested
    @DisplayName("deleteBooking()")
    class DeleteBookingTests {

        @Test
        @DisplayName("Booking không tồn tại → ném RESOURCE_NOT_FOUND")
        void deleteBooking_WhenBookingNotFound_ShouldThrow() {
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.deleteBooking(BOOKING_ID, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.RESOURCE_NOT_FOUND));
        }

        @Test
        @DisplayName("Happy path → set deletedAt và lưu BookingHistory")
        void deleteBooking_HappyPath_ShouldSetDeletedAtAndSaveHistory() {
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
            when(jsonMapper.valueToTree(any())).thenReturn(null);

            try (MockedStatic<TimeUtils> timeUtilsMock = mockStatic(TimeUtils.class)) {
                timeUtilsMock.when(TimeUtils::now).thenReturn(NOW);
                // CRUDResponseHelper.deleteSuccess() gọi TimeUtils.dateTimeFormat() → cần mock
                timeUtilsMock.when(TimeUtils::dateTimeFormat).thenReturn("04-07-2026 10:00:00");

                Map<String, Object> result = bookingService.deleteBooking(BOOKING_ID, USER_ID);

                assertThat(mockBooking.getDeletedAt()).isEqualTo(NOW);
                verify(bookingHistoryRepository).save(any(BookingHistory.class));
                assertThat(result).isNotNull();
            }
        }
    }

    // =====================================================
    //               ADD EQUIPMENT TO BOOKING
    // =====================================================
    @Nested
    @DisplayName("addEquipmentBooking()")
    class AddEquipmentBookingTests {

        @Test
        @DisplayName("Ném RESOURCE_NOT_FOUND khi booking không tồn tại")
        void addEquipmentBooking_WhenBookingNotFound_ShouldThrow() {
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());
            List<UpdateEquipmentBookingRequest> request = List.of(
                    new UpdateEquipmentBookingRequest(1L, 5, com.example.schedulemeetingbe.constant.enums.BookingEquipmentAction.ADD)
            );

            assertThatThrownBy(() -> bookingService.addEquipmentBooking(BOOKING_ID, request, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorResponse())
                            .isEqualTo(ErrorResponse.RESOURCE_NOT_FOUND));
        }

        @Test
        @DisplayName("Thêm thiết bị vào booking thành công")
        void addEquipmentBooking_HappyPath_ShouldSucceed() {
            List<UpdateEquipmentBookingRequest> request = List.of(
                    new UpdateEquipmentBookingRequest(1L, 5, com.example.schedulemeetingbe.constant.enums.BookingEquipmentAction.ADD)
            );

            Equipment mockEquip = Equipment.builder().equipmentId(1L).equipmentName("Projector").totalQuantity(15).build();
            EquipmentAndQuantityResponse eqQtyResp = new EquipmentAndQuantityResponse(1L, "Projector", 10);

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(mockBooking));
            when(iUserService.getDetail(USER_ID)).thenReturn(Optional.of(mockUser));
            when(iEquipmentService.findEquipmentAndRemainingQuantity(anyList())).thenReturn(List.of(eqQtyResp));
            when(iEquipmentService.getEquipmentDetail(1L)).thenReturn(Optional.of(mockEquip));
            when(iEquipmentService.findEquipmentIn(anyList())).thenReturn(List.of(mockEquip));
            when(bookingEquipmentRepository.getBookingEquipments(BOOKING_ID)).thenReturn(Collections.emptyList());

            Map<String, Long> result = bookingService.addEquipmentBooking(BOOKING_ID, request, USER_ID);

            assertThat(result).containsKey("bookingId");
            assertThat(result.get("bookingId")).isEqualTo(BOOKING_ID);
            assertThat(mockBooking.getStatus()).isEqualTo(BookingStatus.PENDING);
            verify(bookingEquipmentRepository).saveAll(anyList());
            verify(bookingHistoryRepository).save(any(BookingHistory.class));
        }
    }
}
