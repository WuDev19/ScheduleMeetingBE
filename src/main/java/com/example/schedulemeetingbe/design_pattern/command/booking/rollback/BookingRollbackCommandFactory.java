package com.example.schedulemeetingbe.design_pattern.command.booking.rollback;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BookingRollbackCommandFactory {

    private final Map<BookingActionType, BookingRollbackCommand> commands;

    public BookingRollbackCommandFactory(List<BookingRollbackCommand> commandList) {
        this.commands = commandList.stream()
                .collect(Collectors.toMap(BookingRollbackCommand::getActionType, Function.identity()));
    }

    public BookingRollbackCommand get(
            BookingActionType actionType
    ) {
        return Optional.ofNullable(commands.get(actionType))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unsupported action type: " + actionType
                        ));
    }
}
