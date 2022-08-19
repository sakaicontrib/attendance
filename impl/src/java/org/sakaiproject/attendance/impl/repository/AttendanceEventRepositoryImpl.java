package org.sakaiproject.attendance.impl.repository;

import org.hibernate.Session;
import org.sakaiproject.attendance.api.model.AttendanceEvent;
import org.sakaiproject.attendance.api.model.AttendanceSite;
import org.sakaiproject.attendance.api.model.stats.AttendanceItemStats;
import org.sakaiproject.attendance.api.model.stats.StatusCount;
import org.sakaiproject.attendance.api.repository.AttendanceEventRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class AttendanceEventRepositoryImpl extends SpringCrudRepositoryImpl<AttendanceEvent, Long> implements AttendanceEventRepository {

    @Transactional(readOnly = true)
    public List<AttendanceEvent> findAllByAttendanceSite(AttendanceSite attendanceSite) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AttendanceEvent> query = cb.createQuery(AttendanceEvent.class);
        Root<AttendanceEvent> event = query.from(AttendanceEvent.class);
        query.where(cb.equal(event.get("attendanceSite"), attendanceSite));

        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public AttendanceItemStats calculateStatsForEvent(AttendanceEvent event) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("SELECT new org.sakaiproject.attendance.api.model.stats.StatusCount(r.status, COUNT(r.status)) "
                + "FROM AttendanceRecord AS r WHERE r.attendanceEvent = :attendanceEvent GROUP BY r.status");
        query.setParameter("attendanceEvent", event);
        List<StatusCount> statusCounts = query.getResultList();
        return new AttendanceItemStats(event, statusCounts);
    }
}
