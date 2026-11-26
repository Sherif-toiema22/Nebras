package com.nebras.security;

import com.nebras.classroom.ClassroomRepository;
import com.nebras.classroom.ParentLinkRepository;
import com.nebras.classroom.StudentRepository;
import org.springframework.stereotype.Component;

/** من يرى ماذا: الطالب نفسه، ولي أمره، معلم فصله، أو الإدارة. */
@Component
public class Access {
    private final ClassroomRepository classrooms;
    private final StudentRepository students;
    private final ParentLinkRepository links;

    public Access(ClassroomRepository classrooms, StudentRepository students, ParentLinkRepository links) {
        this.classrooms = classrooms; this.students = students; this.links = links;
    }

    public boolean teachesClassroom(AuthUser u, long classroomId) {
        if (u.role().equals("ADMIN")) return true;
        return u.role().equals("TEACHER") && classrooms.existsByIdAndTeacherId(classroomId, u.id());
    }

    public boolean canViewStudent(AuthUser u, long studentId) {
        return switch (u.role()) {
            case "ADMIN" -> true;
            case "STUDENT" -> u.id() == studentId;
            case "PARENT" -> links.existsByParentIdAndStudentId(u.id(), studentId);
            case "TEACHER" -> students.findById(studentId)
                    .map(s -> classrooms.existsByIdAndTeacherId(s.getClassroomId(), u.id())).orElse(false);
            default -> false;
        };
    }
}
