package org.sakaiproject.attendance.api.repository;

import org.sakaiproject.attendance.api.model.AttendanceSite;
import org.sakaiproject.attendance.api.model.AttendanceStatus;
import org.sakaiproject.springframework.data.SpringCrudRepository;

import java.util.List;

public interface AttendanceStatusRepository  extends SpringCrudRepository<AttendanceStatus, Long> {

    List<AttendanceStatus> findAllByAttendanceSite(AttendanceSite attendanceSite);
    List<AttendanceStatus> findAllActiveByAttendanceSite(AttendanceSite attendanceSite);
}
