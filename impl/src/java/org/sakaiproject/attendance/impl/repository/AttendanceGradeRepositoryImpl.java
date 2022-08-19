package org.sakaiproject.attendance.impl.repository;

import org.hibernate.Session;
import org.sakaiproject.attendance.api.model.AttendanceGrade;
import org.sakaiproject.attendance.api.model.AttendanceSite;
import org.sakaiproject.attendance.api.repository.AttendanceGradeRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class AttendanceGradeRepositoryImpl extends SpringCrudRepositoryImpl<AttendanceGrade, Long> implements AttendanceGradeRepository {

    @Transactional(readOnly = true)
    public List<AttendanceGrade> findAllByAttendanceSite(AttendanceSite attendanceSite) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AttendanceGrade> query = cb.createQuery(AttendanceGrade.class);
        Root<AttendanceGrade> record = query.from(AttendanceGrade.class);
        query.where(cb.equal(record.get("attendanceSite"), attendanceSite));

        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<AttendanceGrade> findByAttendanceSiteAndUserId(AttendanceSite attendanceSite, String userId) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AttendanceGrade> query = cb.createQuery(AttendanceGrade.class);
        Root<AttendanceGrade> grade = query.from(AttendanceGrade.class);
        query.where(cb.and(cb.equal(grade.get("attendanceSite"), attendanceSite),
                cb.equal(grade.get("userID"), userId)));

        return session.createQuery(query).uniqueResultOptional();
    }
}
