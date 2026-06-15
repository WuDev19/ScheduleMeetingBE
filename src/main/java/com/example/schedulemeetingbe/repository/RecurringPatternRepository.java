package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.RecurringPattern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringPatternRepository extends JpaRepository<RecurringPattern, Long> {
}
