package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.design_pattern.strategy.recurring.RecurrenceStrategyFactory;
import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.dto.response.booking.RecurringPatternResponse;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.mapper.RecurringPatternMapper;
import com.example.schedulemeetingbe.repository.RecurringPatternRepository;
import com.example.schedulemeetingbe.service.base.IRecurringPatternService;
import com.example.schedulemeetingbe.service.base.IRoomService;
import com.example.schedulemeetingbe.service.base.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecurringPatternServiceImpl implements IRecurringPatternService {

    private final RecurringPatternRepository recurringPatternRepository;
    private final IUserService iUserService;
    private final IRoomService iRoomService;
    private final RecurrenceStrategyFactory factory;

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

    @Override
    public RecurringPattern save(RecurringPattern recurringPattern) {
        return recurringPatternRepository.save(recurringPattern);
    }

}