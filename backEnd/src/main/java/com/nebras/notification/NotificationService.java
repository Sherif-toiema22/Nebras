package com.nebras.notification;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) { this.repo = repo; }

    @Transactional
    public void send(Long userId, String body) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setBody(body);
        repo.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> mine(Long userId) {
        return repo.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream().map(NotificationDto::from).toList();
    }

    @Transactional
    public void markRead(Long id, Long userId) {
        repo.findByIdAndUserId(id, userId).ifPresent(n -> n.setRead(true));
    }
}
