package com.nebras.attendance;

import com.nebras.attendance.AttendanceDtos.*;
import com.nebras.classroom.ParentLink;
import com.nebras.classroom.ParentLinkRepository;
import com.nebras.classroom.StudentRepository;
import com.nebras.notification.NotificationService;
import com.nebras.security.Access;
import com.nebras.security.AuthUser;
import com.nebras.user.User;
import com.nebras.user.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttendanceService {
    private final AttendanceRepository attendance;
    private final StudentRepository students;
    private final UserRepository users;
    private final ParentLinkRepository links;
    private final NotificationService notifications;
    private final Access access;

    public AttendanceService(AttendanceRepository attendance, StudentRepository students, UserRepository users,
                             ParentLinkRepository links, NotificationService notifications, Access access) {
        this.attendance = attendance; this.students = students; this.users = users;
        this.links = links; this.notifications = notifications; this.access = access;
    }

    /** المعلم يسجل حضور فصل ليوم. غياب جديد = إشعار لأولياء الأمور. */
    @Transactional
    public SaveResult save(AuthUser u, SaveRequest r) {
        if (!access.teachesClassroom(u, r.classroomId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        int notified = 0;
        for (Entry e : r.records()) {
            if (!students.existsByUserIdAndClassroomId(e.studentId(), r.classroomId()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "الطالب " + e.studentId() + " ليس في هذا الفصل");
            var existing = attendance.findByStudentIdAndAttendanceDay(e.studentId(), r.day());
            boolean wasAbsent = existing.map(a -> a.getStatus() == AttendanceStatus.ABSENT).orElse(false);
            Attendance a = existing.orElseGet(Attendance::new);
            a.setStudentId(e.studentId());
            a.setAttendanceDay(r.day());
            a.setStatus(e.status());
            a.setNote(e.note());
            attendance.save(a);
            if (e.status() == AttendanceStatus.ABSENT && !wasAbsent) {
                String name = users.findById(e.studentId()).map(User::getFullName).orElse("طالب");
                for (ParentLink l : links.findByStudentId(e.studentId())) {
                    notifications.send(l.getParentId(), "غاب " + name + " بتاريخ " + r.day());
                    notified++;
                }
            }
        }
        return new SaveResult(r.records().size(), notified);
    }

    @Transactional(readOnly = true)
    public List<ClassroomRow> classroomSheet(AuthUser u, long classroomId, LocalDate day) {
        if (!access.teachesClassroom(u, classroomId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return attendance.classroomSheet(classroomId, day);
    }

    @Transactional(readOnly = true)
    public StudentAttendance forStudent(AuthUser u, long studentId) {
        if (!access.canViewStudent(u, studentId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        var records = attendance.findTop100ByStudentIdOrderByAttendanceDayDesc(studentId).stream()
                .map(a -> new AttendanceItem(a.getAttendanceDay(), a.getStatus(), a.getNote())).toList();
        long total = attendance.countByStudentId(studentId);
        long attended = attendance.countByStudentIdAndStatusNot(studentId, AttendanceStatus.ABSENT);
        return new StudentAttendance(records, total, total == 0 ? 100 : Math.round(100.0 * attended / total));
    }
}
