package org.sakaiproject.attendance.impl.repository;

import org.hibernate.Session;
import org.sakaiproject.attendance.api.model.AttendanceSite;
import org.sakaiproject.attendance.api.model.stats.AttendanceUserStats;
import org.sakaiproject.attendance.api.model.stats.StatusCount;
import org.sakaiproject.attendance.api.repository.AttendanceSiteRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class AttendanceSiteRepositoryImpl extends SpringCrudRepositoryImpl<AttendanceSite, Long> implements AttendanceSiteRepository {

    @Transactional(readOnly = true)
    public AttendanceSite findBySiteId(String siteId) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AttendanceSite> query = cb.createQuery(AttendanceSite.class);
        Root<AttendanceSite> site = query.from(AttendanceSite.class);
        query.where(cb.equal(site.get("siteID"), siteId));

        return session.createQuery(query).uniqueResult();
    }

    @Transactional(readOnly = true)
    public AttendanceUserStats calculateAttendanceUserStats(String userId, AttendanceSite attendanceSite) {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("SELECT new org.sakaiproject.attendance.api.model.stats.StatusCount(r.status, COUNT(r.status)) "
                + "FROM AttendanceRecord AS r JOIN AttendanceEvent AS e ON r.attendanceEvent = e "
                + "WHERE e.attendanceSite = :attendanceSite AND r.userID = :userId GROUP BY r.status");
        query.setParameter("attendanceSite", attendanceSite);
        query.setParameter("userId", userId);
        List<StatusCount> statusCounts = query.getResultList();

        return new AttendanceUserStats(userId, attendanceSite, statusCounts);
    }
}
