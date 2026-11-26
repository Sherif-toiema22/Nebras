package com.nebras.admin;

import com.nebras.admin.AdminDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService service;

    public AdminController(AdminService service) { this.service = service; }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse createUser(@Valid @RequestBody NewUser r) { return new IdResponse(service.createUser(r)); }

    @PostMapping("/classrooms")
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse createClassroom(@Valid @RequestBody NewClassroom r) {
        return new IdResponse(service.createClassroom(r));
    }

    @PostMapping("/classrooms/{id}/students")
    @ResponseStatus(HttpStatus.CREATED)
    public void addStudent(@PathVariable long id, @Valid @RequestBody AddStudent r) {
        service.addStudent(id, r.studentId());
    }

    @PostMapping("/parent-links")
    @ResponseStatus(HttpStatus.CREATED)
    public void link(@Valid @RequestBody LinkParent r) { service.linkParent(r.parentId(), r.studentId()); }
}
