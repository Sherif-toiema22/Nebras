package com.nebras.notification;

import java.time.OffsetDateTime;

public record NotificationDto(Long id, String body, boolean read, OffsetDateTime createdAt) {
    static NotificationDto from(Notification n) {
        return new NotificationDto(n.getId(), n.getBody(), n.isRead(), n.getCreatedAt());
    }
}
