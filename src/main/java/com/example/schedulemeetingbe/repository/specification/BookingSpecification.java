package com.example.schedulemeetingbe.repository.specification;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.entity.Booking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingSpecification {

    private BookingSpecification() {
    }

    public static Specification<Booking> filter(
            Long roomId,
            String bookedBy,
            BookingStatus statuses,
            OffsetDateTime fromDate,
            OffsetDateTime toDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            if (roomId != null) {
                predicates.add(criteriaBuilder.equal(root.get("room").get("roomId"), roomId));
            }
            if (bookedBy != null) {
                predicates.add(criteriaBuilder.like(root.get("bookedBy").get("fullName"), bookedBy + "%"));
            }
            if (statuses != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), statuses));
            }
            if (fromDate != null && toDate != null) {
                predicates.add(criteriaBuilder.and(
                        criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), toDate),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("endTime"), fromDate)
                ));
            } else if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endTime"), fromDate));
            } else if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), toDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
