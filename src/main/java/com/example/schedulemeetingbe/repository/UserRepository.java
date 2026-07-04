package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.dto.response.user.FullNameAndEmailResponse;
import com.example.schedulemeetingbe.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    Optional<User> findByUsernameAndIsActiveIsTrue(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsActiveIsTrue(String email);

    List<User> findByEmailIn(List<String> emails);

    Set<User> findByUserIdIn(List<Long> userIds);

    Set<User> findByDepartment_DepartmentId(Long departmentId);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.email LIKE CONCAT(:keyword, '%')
            OR u.fullName LIKE CONCAT(:keyword, '%')
            """)
    Page<User> findByEmailOrFullName(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT new com.example.schedulemeetingbe.dto.response.user.FullNameAndEmailResponse(
                 u.fullName,
                 u.email
            )
            FROM User u
            WHERE u.isActive = true
            ORDER BY u.userId ASC
            """
    )
    Page<FullNameAndEmailResponse> getFullNameAndEmail(Pageable pageable);
}
