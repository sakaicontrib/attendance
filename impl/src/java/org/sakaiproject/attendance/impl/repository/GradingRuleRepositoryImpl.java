package org.sakaiproject.attendance.impl.repository;

import org.hibernate.Session;
import org.sakaiproject.attendance.api.model.AttendanceSite;
import org.sakaiproject.attendance.api.model.GradingRule;
import org.sakaiproject.attendance.api.repository.GradingRuleRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class GradingRuleRepositoryImpl extends SpringCrudRepositoryImpl<GradingRule, Long> implements GradingRuleRepository {

    @Transactional(readOnly = true)
    public List<GradingRule> findAllByAttendanceSite(AttendanceSite attendanceSite) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GradingRule> query = cb.createQuery(GradingRule.class);
        Root<GradingRule> rule = query.from(GradingRule.class);
        query.where(cb.equal(rule.get("attendanceSite"), attendanceSite));

        return session.createQuery(query).list();
    }
}
