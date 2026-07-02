package com.example.schedulemeetingbe.repository.specification;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.request.recurrence.RecurringPatternFilterRequest;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import com.example.schedulemeetingbe.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RecurringPatternSpecification {

    private RecurringPatternSpecification() {
    }

    public static Specification<RecurringPattern> filter(Long userId, Set<String> roles, RecurringPatternFilterRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (roles.contains(StringCommon.REGISTER)) {
                Join<RecurringPattern, User> recurringPatternUser = root.join("createdBy");
                predicates.add(criteriaBuilder.equal(recurringPatternUser.get("userId"), userId));
            }
            if (request.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.status()));
            }
            if (request.startDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), request.startDate()));
            }
            if (request.endDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), request.endDate()));
            }
            if (request.recurrenceType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("recurrenceType"), request.recurrenceType()));
            }
            if (request.userCreatedId() != null) {
                Join<RecurringPattern, User> recurringPatternUser = root.join("createdBy");
                predicates.add(criteriaBuilder.equal(recurringPatternUser.get("userId"), request.userCreatedId()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
