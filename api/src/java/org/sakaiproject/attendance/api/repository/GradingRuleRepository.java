package org.sakaiproject.attendance.api.repository;

import org.sakaiproject.attendance.api.model.AttendanceSite;
import org.sakaiproject.attendance.api.model.GradingRule;
import org.sakaiproject.springframework.data.SpringCrudRepository;

import java.util.List;

public interface GradingRuleRepository  extends SpringCrudRepository<GradingRule, Long> {

    List<GradingRule> findAllByAttendanceSite(AttendanceSite attendanceSite);
}
