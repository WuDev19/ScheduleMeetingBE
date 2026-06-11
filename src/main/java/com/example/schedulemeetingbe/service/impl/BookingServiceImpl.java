package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.repository.BookingRepository;
import com.example.schedulemeetingbe.service.base.IBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {
    private final BookingRepository bookingRepository;

}
