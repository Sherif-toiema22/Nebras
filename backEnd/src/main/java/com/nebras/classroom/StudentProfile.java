package com.nebras.classroom;

import jakarta.persistence.*;

/** صف الطالب في جدول students: يربط حساب الطالب بفصله وفريقه. */
@Entity
@Table(name = "students")
public class StudentProfile {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "classroom_id")
    private Long classroomId;

    @Column(name = "team_id")
    private Long teamId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
}
