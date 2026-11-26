package com.nebras.auth;

import com.nebras.user.Role;
import com.nebras.user.User;
import com.nebras.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** ينشئ أول مدير مرة واحدة من متغيرات البيئة (لا كلمة سر مكتوبة في الكود). */
@Component
public class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String email, password;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder,
                       @Value("${nebras.admin.email}") String email,
                       @Value("${nebras.admin.password}") String password) {
        this.users = users; this.encoder = encoder; this.email = email; this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.existsByRole(Role.ADMIN)) return;
        if (password.length() < 8) {
            log.warn("لا يوجد مدير. اضبط ADMIN_PASSWORD (٨ أحرف على الأقل) وأعد التشغيل.");
            return;
        }
        User u = new User();
        u.setRole(Role.ADMIN);
        u.setFullName("المدير");
        u.setEmail(email.toLowerCase());
        u.setPasswordHash(encoder.encode(password));
        users.save(u);
        log.info("تم إنشاء حساب المدير: {}", email);
    }
}
