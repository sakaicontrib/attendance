package org.sakaiproject.attendance.api.repository;

import org.sakaiproject.attendance.api.model.AttendanceEvent;
import org.sakaiproject.attendance.api.model.AttendanceRecord;
import org.sakaiproject.springframework.data.SpringCrudRepository;

import java.util.List;

public interface AttendanceRecordRepository  extends SpringCrudRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findAllByAttendanceEvent(AttendanceEvent attendanceEvent);
}
