package org.sakaiproject.attendance.api.repository;

import org.sakaiproject.attendance.api.model.AttendanceSite;
import org.sakaiproject.attendance.api.model.stats.AttendanceUserStats;
import org.sakaiproject.springframework.data.SpringCrudRepository;

import java.util.List;

public interface AttendanceSiteRepository  extends SpringCrudRepository<AttendanceSite, Long> {

    AttendanceSite findBySiteId(String siteId);
    AttendanceUserStats calculateAttendanceUserStats(String userId, AttendanceSite attendanceSite);
}
