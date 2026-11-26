package com.nebras.auth;

import com.nebras.auth.AuthDtos.*;
import com.nebras.security.JwtService;
import com.nebras.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    public LoginResponse login(LoginRequest r) {
        var u = users.findByEmailIgnoreCaseAndActiveTrue(r.email().trim()).orElse(null);
        if (u == null || !encoder.matches(r.password(), u.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "بيانات الدخول غير صحيحة");
        String role = u.getRole().name();
        return new LoginResponse(jwt.create(u.getId(), role), role, u.getFullName());
    }
}
