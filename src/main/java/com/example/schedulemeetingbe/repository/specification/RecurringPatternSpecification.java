package com.example.schedulemeetingbe.repository.specification;

import com.example.schedulemeetingbe.dto.request.recurrence.RecurringPatternFilterRequest;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RecurringPatternSpecification {

    private RecurringPatternSpecification() {
    }

    public static Specification<RecurringPattern> filter(Long userId, Set<String> permissions, RecurringPatternFilterRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if(!permissions.contains("RECURRING_BOOKING:VIEW_ALL")){
                predicates.add(criteriaBuilder.equal(root.get("createdBy"), userId));
            }
            if (request.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.status()));
            }
            if (request.startDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), request.startDate()));
            }
            if (request.endDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), request.endDate()));
            }
            if (request.recurrenceType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("recurrenceType"), request.recurrenceType()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
