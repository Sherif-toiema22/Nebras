package com.nebras.classroom;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** صف الطالب في جدول students: يربط حساب الطالب بفصله وفريقه. */
@Entity
@Table(name = "students")
@Getter @Setter @NoArgsConstructor
public class StudentProfile {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "classroom_id")
    private Long classroomId;

    @Column(name = "team_id")
    private Long teamId;
}
