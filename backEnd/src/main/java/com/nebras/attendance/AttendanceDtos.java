package com.nebras.attendance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public final class AttendanceDtos {
    private AttendanceDtos() {}

    public record Entry(@NotNull Long studentId, @NotNull AttendanceStatus status, @Size(max = 200) String note) {}
    public record SaveRequest(@NotNull Long classroomId, @NotNull LocalDate day,
                              @NotEmpty List<@Valid Entry> records) {}
    public record SaveResult(int saved, int parentsNotified) {}
    public record AttendanceItem(LocalDate day, AttendanceStatus status, String note) {}
    public record StudentAttendance(List<AttendanceItem> records, long total, long attendancePercent) {}
}
