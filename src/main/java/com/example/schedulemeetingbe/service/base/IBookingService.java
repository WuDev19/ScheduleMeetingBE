package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.booking.CreateBookingRequest;
import com.example.schedulemeetingbe.dto.response.booking.BookingResponse;

public interface IBookingService {
    BookingResponse createBooking(CreateBookingRequest request, String username);
}
