package com.nebras.attendance;

import com.nebras.attendance.AttendanceDtos.*;
import com.nebras.security.AuthUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AttendanceController {
    private final AttendanceService service;

    public AttendanceController(AttendanceService service) { this.service = service; }

    @PostMapping("/attendance")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public SaveResult save(@Valid @RequestBody SaveRequest r, @AuthenticationPrincipal AuthUser u) {
        return service.save(u, r);
    }

    @GetMapping("/classrooms/{id}/attendance")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public List<ClassroomRow> classroom(@PathVariable long id, @RequestParam LocalDate day,
                                        @AuthenticationPrincipal AuthUser u) {
        return service.classroomSheet(u, id, day);
    }

    @GetMapping("/students/{id}/attendance")
    public StudentAttendance student(@PathVariable long id, @AuthenticationPrincipal AuthUser u) {
        return service.forStudent(u, id);
    }
}
