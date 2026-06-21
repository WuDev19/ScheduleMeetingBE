package com.example.schedulemeetingbe.command.booking.approve;

import com.example.schedulemeetingbe.command.booking.rollback.BookingRollbackCommand;
import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BookingApproveCommandFactory {
    private final Map<BookingActionType, BookingApproveCommand> commands;

    public BookingApproveCommandFactory(List<BookingApproveCommand> commandList) {
        this.commands = commandList.stream()
                .collect(Collectors.toMap(BookingApproveCommand::getActionType, Function.identity()));
    }

    public BookingApproveCommand get(
            BookingActionType actionType
    ) {
        return Optional.ofNullable(commands.get(actionType))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unsupported action type: " + actionType
                        ));
    }
}
