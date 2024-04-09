package org.sakaiproject.attendance.api.repository;

import org.sakaiproject.attendance.api.model.AttendanceEvent;
import org.sakaiproject.attendance.api.model.AttendanceSite;
import org.sakaiproject.attendance.api.model.stats.AttendanceItemStats;
import org.sakaiproject.springframework.data.SpringCrudRepository;

import java.util.List;

public interface AttendanceEventRepository extends SpringCrudRepository<AttendanceEvent, Long> {

    List<AttendanceEvent> findAllByAttendanceSite(AttendanceSite attendanceSite);
    AttendanceItemStats calculateStatsForEvent(AttendanceEvent event);
}
