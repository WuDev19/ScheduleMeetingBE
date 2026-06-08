package com.example.schedulemeetingbe.repository.specification;

import com.example.schedulemeetingbe.entity.RoomUnavailability;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class UnavailabilityRoomSpecification {
    private UnavailabilityRoomSpecification() {
    }

    public static Specification<RoomUnavailability> filter(ZonedDateTime start, ZonedDateTime end) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), start));
            }
            if (end != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endTime"), end));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
