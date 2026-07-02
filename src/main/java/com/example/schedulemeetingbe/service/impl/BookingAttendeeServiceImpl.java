package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.dto.response.booking.booking_notification.BookingAndEmailAttendeeResponse;
import com.example.schedulemeetingbe.dto.response.booking.booking_notification.BookingAndUserResponse;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.repository.BookingAttendeeRepository;
import com.example.schedulemeetingbe.service.base.IBookingAttendeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingAttendeeServiceImpl implements IBookingAttendeeService {
    private final BookingAttendeeRepository bookingAttendeeRepository;

    @Override
    public List<User> getAttendOfBooking(Long bookingId) {
        return bookingAttendeeRepository.getAttendeeOfBooking(bookingId);
    }

    @Override
    public List<BookingAndUserResponse> getAttendOfBooking(List<Long> bookingIds) {
        return bookingAttendeeRepository.getAttendeeOfBooking(bookingIds);
    }

    @Override
    public List<BookingAndEmailAttendeeResponse> getEmailAttendOfBooking(List<Long> bookingIds) {
        return bookingAttendeeRepository.getEmailAttendOfBooking(bookingIds);
    }
}
