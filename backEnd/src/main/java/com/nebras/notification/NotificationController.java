package com.nebras.notification;

import com.nebras.security.AuthUser;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) { this.service = service; }

    @GetMapping
    public List<NotificationDto> mine(@AuthenticationPrincipal AuthUser u) { return service.mine(u.id()); }

    @PostMapping("/{id}/read")
    public void markRead(@PathVariable long id, @AuthenticationPrincipal AuthUser u) { service.markRead(id, u.id()); }
}
