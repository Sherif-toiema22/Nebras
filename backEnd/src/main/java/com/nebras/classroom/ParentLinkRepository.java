package com.nebras.classroom;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentLinkRepository extends JpaRepository<ParentLink, ParentLinkId> {
    boolean existsByParentIdAndStudentId(Long parentId, Long studentId);
    List<ParentLink> findByStudentId(Long studentId);
}
