package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.entity.User;

import java.util.List;

public interface IBookingAttendeeService {

    List<User> getAttendOfBooking(Long bookingId);
}
