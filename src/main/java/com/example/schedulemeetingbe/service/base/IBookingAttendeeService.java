package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.response.booking.booking_notification.BookingAndEmailAttendeeResponse;
import com.example.schedulemeetingbe.dto.response.booking.booking_notification.BookingAndUserResponse;
import com.example.schedulemeetingbe.entity.User;

import java.util.List;

public interface IBookingAttendeeService {

    List<User> getAttendOfBooking(Long bookingId);

    List<BookingAndUserResponse> getAttendOfBooking(List<Long> bookingIds);

    List<BookingAndEmailAttendeeResponse> getEmailAttendOfBooking(List<Long> bookingIds);
}
