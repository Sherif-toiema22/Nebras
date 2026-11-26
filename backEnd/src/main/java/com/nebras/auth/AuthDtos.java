package com.nebras.auth;

import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record LoginResponse(String token, String role, String name) {}
}
