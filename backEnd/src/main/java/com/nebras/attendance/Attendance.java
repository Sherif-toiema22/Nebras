package com.nebras.attendance;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attendance")
@Getter @Setter @NoArgsConstructor
public class Attendance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "day")
    private LocalDate attendanceDay;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    private String note;
}
