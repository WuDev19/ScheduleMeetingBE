package com.example.schedulemeetingbe.design_pattern.strategy.recurring;

import com.example.schedulemeetingbe.constant.enums.RecurrenceType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RecurrenceStrategyFactory {
    private final Map<RecurrenceType, RecurrencePatternStrategy> strategyMap;

    public RecurrenceStrategyFactory(List<RecurrencePatternStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        RecurrencePatternStrategy::getType,
                        Function.identity()
                ));
    }

    public RecurrencePatternStrategy getStrategy(RecurrenceType type) {
        return strategyMap.get(type);
    }
}
