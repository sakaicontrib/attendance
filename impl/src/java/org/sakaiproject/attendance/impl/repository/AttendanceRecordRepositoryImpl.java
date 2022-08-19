package org.sakaiproject.attendance.impl.repository;

import org.hibernate.Session;
import org.sakaiproject.attendance.api.model.AttendanceEvent;
import org.sakaiproject.attendance.api.model.AttendanceRecord;
import org.sakaiproject.attendance.api.repository.AttendanceRecordRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class AttendanceRecordRepositoryImpl extends SpringCrudRepositoryImpl<AttendanceRecord, Long> implements AttendanceRecordRepository {

    @Transactional(readOnly = true)
    public List<AttendanceRecord> findAllByAttendanceEvent(AttendanceEvent attendanceEvent) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<AttendanceRecord> query = cb.createQuery(AttendanceRecord.class);
        Root<AttendanceRecord> record = query.from(AttendanceRecord.class);
        query.where(cb.equal(record.get("attendanceEvent"), attendanceEvent));

        return session.createQuery(query).list();
    }
}
