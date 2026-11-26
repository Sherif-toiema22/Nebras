package com.nebras.attendance;

/** سطر في كشف حضور الفصل (status فارغ = لم يُسجَّل بعد). */
public record ClassroomRow(Long studentId, String fullName, AttendanceStatus status, String note) {}
