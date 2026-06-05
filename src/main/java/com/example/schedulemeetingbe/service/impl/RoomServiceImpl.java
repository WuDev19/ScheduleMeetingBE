package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.service.base.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements IRoomService {
    @Override
    public Map<String, Object> createRoom() {
        return Map.of();
    }
}
