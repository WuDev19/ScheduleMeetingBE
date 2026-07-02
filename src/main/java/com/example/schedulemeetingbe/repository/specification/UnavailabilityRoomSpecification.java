package com.example.schedulemeetingbe.repository.specification;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.entity.RoomUnavailability;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class UnavailabilityRoomSpecification {

    private UnavailabilityRoomSpecification() {
    }

    public static Specification<RoomUnavailability> filter(
            Boolean isDeleted,
            OffsetDateTime start,
            OffsetDateTime end,
            List<String> roles) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isDeleted != null) {
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), isDeleted));
            } else if (!roles.contains(StringCommon.ADMIN)) {
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            }
            if (start != null && end != null) {
                predicates.add(criteriaBuilder.and(
                        criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), end),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("endTime"), start)
                ));
            } else if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endTime"), start));
            } else if (end != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), end));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
