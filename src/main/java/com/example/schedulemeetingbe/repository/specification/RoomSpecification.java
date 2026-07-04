package com.example.schedulemeetingbe.repository.specification;

import com.example.schedulemeetingbe.entity.Room;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RoomSpecification {

    private RoomSpecification() {
    }

    public static Specification<Room> filter(Integer capacity, Integer floorNumber) {
        return (root, query, criteriaBuilder)
                -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));
            if (capacity != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("capacity"),
                                capacity
                        )
                );
            }
            if (floorNumber != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("floorNumber"),
                                floorNumber
                        )
                );
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
