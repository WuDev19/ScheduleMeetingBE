package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.service.base.IRoomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/room")
@Tag(name = "Tài liệu API cho Room")
@RequiredArgsConstructor
public class RoomController {

    private final IRoomService iRoomService;

}
