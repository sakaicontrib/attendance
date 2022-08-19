package org.sakaiproject.attendance.impl.repository;

import org.hibernate.Session;
import org.sakaiproject.attendance.api.model.AttendanceSite;
import org.sakaiproject.attendance.api.model.AttendanceStatus;
import org.sakaiproject.attendance.api.repository.AttendanceStatusRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class AttendanceStatusRepositoryImpl extends SpringCrudRepositoryImpl<AttendanceStatus, Long> implements AttendanceStatusRepository {

    @Transactional(readOnly = true)
    public List<AttendanceStatus> findAllByAttendanceSite(AttendanceSite attendanceSite) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AttendanceStatus> query = cb.createQuery(AttendanceStatus.class);
        Root<AttendanceStatus> status = query.from(AttendanceStatus.class);
        query.where(cb.equal(status.get("attendanceSite"), attendanceSite));

        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<AttendanceStatus> findAllActiveByAttendanceSite(AttendanceSite attendanceSite) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AttendanceStatus> query = cb.createQuery(AttendanceStatus.class);
        Root<AttendanceStatus> status = query.from(AttendanceStatus.class);
        query.where(cb.and(cb.equal(status.get("attendanceSite"), attendanceSite),
                cb.equal(status.get("isActive"), Boolean.TRUE)));

        return session.createQuery(query).list();
    }
}
