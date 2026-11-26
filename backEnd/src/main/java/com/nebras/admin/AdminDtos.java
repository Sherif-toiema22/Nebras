package com.nebras.admin;

import com.nebras.user.Role;
import jakarta.validation.constraints.*;

public final class AdminDtos {
    private AdminDtos() {}

    public record NewUser(@NotNull Role role, @NotBlank @Size(max = 120) String fullName,
                          @NotBlank @Email String email, @NotBlank @Size(min = 8, max = 72) String password,
                          String phone) {}
    public record NewClassroom(@NotBlank @Size(max = 80) String name, @NotNull Long teacherId) {}
    public record AddStudent(@NotNull Long studentId) {}
    public record LinkParent(@NotNull Long parentId, @NotNull Long studentId) {}
    public record IdResponse(Long id) {}
}
