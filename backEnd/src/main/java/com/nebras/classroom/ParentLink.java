package com.nebras.classroom;

import jakarta.persistence.*;

/** ربط ولي الأمر بابنه (جدول parent_student). */
@Entity
@Table(name = "parent_student")
@IdClass(ParentLinkId.class)
public class ParentLink {
    @Id @Column(name = "parent_id")
    private Long parentId;

    @Id @Column(name = "student_id")
    private Long studentId;

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
}
