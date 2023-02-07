package org.sakaiproject.attendance.api.repository;

import org.sakaiproject.attendance.api.model.AttendanceGrade;
import org.sakaiproject.attendance.api.model.AttendanceSite;
import org.sakaiproject.springframework.data.SpringCrudRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceGradeRepository extends SpringCrudRepository<AttendanceGrade, Long> {

    List<AttendanceGrade> findAllByAttendanceSite(AttendanceSite attendanceSite);
    Optional<AttendanceGrade> findByAttendanceSiteAndUserId(AttendanceSite attendanceSite, String userId);
}
