package com.nebras.classroom;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<StudentProfile, Long> {
    boolean existsByUserIdAndClassroomId(Long userId, Long classroomId);
}
