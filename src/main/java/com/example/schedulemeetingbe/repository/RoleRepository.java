package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String roleName);

    @Query("""
            SELECT r.roleName
            FROM User u
            JOIN u.roles r
            WHERE u.userId = :userId
            """)
    Set<String> findMyRole(@Param("userId") Long userId);

}
