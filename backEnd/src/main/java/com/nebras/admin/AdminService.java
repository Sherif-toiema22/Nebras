package com.nebras.admin;

import com.nebras.admin.AdminDtos.*;
import com.nebras.classroom.*;
import com.nebras.user.Role;
import com.nebras.user.User;
import com.nebras.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {
    private final UserRepository users;
    private final ClassroomRepository classrooms;
    private final StudentRepository students;
    private final ParentLinkRepository links;
    private final PasswordEncoder encoder;

    public AdminService(UserRepository users, ClassroomRepository classrooms, StudentRepository students,
                        ParentLinkRepository links, PasswordEncoder encoder) {
        this.users = users; this.classrooms = classrooms; this.students = students;
        this.links = links; this.encoder = encoder;
    }

    @Transactional
    public Long createUser(NewUser r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "البريد مستخدم من قبل");
        User u = new User();
        u.setRole(r.role());
        u.setFullName(r.fullName());
        u.setEmail(email);
        u.setPhone(r.phone());
        u.setPasswordHash(encoder.encode(r.password()));
        return users.save(u).getId();
    }

    @Transactional
    public Long createClassroom(NewClassroom r) {
        requireRole(r.teacherId(), Role.TEACHER);
        Classroom c = new Classroom();
        c.setName(r.name());
        c.setTeacherId(r.teacherId());
        return classrooms.save(c).getId();
    }

    @Transactional
    public void addStudent(long classroomId, long studentId) {
        if (!classrooms.existsById(classroomId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "الفصل غير موجود");
        requireRole(studentId, Role.STUDENT);
        StudentProfile p = students.findById(studentId).orElseGet(StudentProfile::new);
        p.setUserId(studentId);
        p.setClassroomId(classroomId);
        students.save(p);
    }

    @Transactional
    public void linkParent(long parentId, long studentId) {
        requireRole(parentId, Role.PARENT);
        if (!students.existsById(studentId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "الطالب غير مسجل في فصل");
        if (links.existsByParentIdAndStudentId(parentId, studentId)) return;
        ParentLink l = new ParentLink();
        l.setParentId(parentId);
        l.setStudentId(studentId);
        links.save(l);
    }

    private void requireRole(long userId, Role role) {
        users.findById(userId).filter(u -> u.getRole() == role).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "المستخدم " + userId + " ليس بدور " + role));
    }
}
