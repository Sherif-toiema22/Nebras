package com.nebras.classroom;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    boolean existsByIdAndTeacherId(Long id, Long teacherId);
}
