package com.example.schedulemeetingbe.service.impl;

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
}
