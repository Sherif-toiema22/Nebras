package com.nebras.attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStudentIdAndAttendanceDay(Long studentId, LocalDate day);
    List<Attendance> findTop100ByStudentIdOrderByAttendanceDayDesc(Long studentId);
    long countByStudentId(Long studentId);
    long countByStudentIdAndStatusNot(Long studentId, AttendanceStatus status);

    @Query("select new com.nebras.attendance.ClassroomRow(s.userId, u.fullName, a.status, a.note) "
         + "from StudentProfile s join User u on u.id = s.userId "
         + "left join Attendance a on a.studentId = s.userId and a.attendanceDay = :day "
         + "where s.classroomId = :cid order by u.fullName")
    List<ClassroomRow> classroomSheet(@Param("cid") Long classroomId, @Param("day") LocalDate day);
}
