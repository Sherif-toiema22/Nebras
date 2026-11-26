package com.nebras.classroom;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** ربط ولي الأمر بابنه (جدول parent_student). */
@Entity
@Table(name = "parent_student")
@IdClass(ParentLinkId.class)
@Getter @Setter @NoArgsConstructor
public class ParentLink {
    @Id @Column(name = "parent_id")
    private Long parentId;

    @Id @Column(name = "student_id")
    private Long studentId;
}
