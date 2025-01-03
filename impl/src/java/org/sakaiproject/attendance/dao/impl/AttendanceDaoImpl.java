/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.dao.impl;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.sakaiproject.attendance.dao.AttendanceDao;
import org.sakaiproject.attendance.model.*;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AttendanceDao
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Slf4j
public class AttendanceDaoImpl extends HibernateDaoSupport implements AttendanceDao {

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public AttendanceSite getAttendanceSite(final String siteID) {
		log.debug("getSiteBySite_ID ");

		HibernateCallback hcb = new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException {
				Query q = session.getNamedQuery(QUERY_GET_SITE_BY_SITE_ID);
				q.setParameter(SITE_ID, siteID, new StringType());
				return q.uniqueResult();
			}
		};

		return (AttendanceSite) getHibernateTemplate().execute(hcb);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public AttendanceSite getAttendanceSite(final Long id) {
		log.debug("getAttendanceSite by ID: {}", id);

		return (AttendanceSite) getByIDHelper(id, QUERY_GET_SITE_BY_ID);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean addAttendanceSite(AttendanceSite aS) {
		log.debug("addAttendanceSite ( {})", aS.toString());

		try {
			getHibernateTemplate().save(aS);
			return true;
		} catch (DataAccessException de) {
			log.error("addAttendanceSite failed", de);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceSite(AttendanceSite aS) {
		try{
			getHibernateTemplate().saveOrUpdate(aS);
			return true;
		} catch (DataAccessException e) {
			log.error("updateAttendanceSite aS '" + aS.getSiteID() + "' failed.", e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public AttendanceEvent getAttendanceEvent(final long id) {
		log.debug("getAttendanceEvent(){}", id);

		return (AttendanceEvent) getByIDHelper(id, QUERY_GET_ATTENDANCE_EVENT);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceEvent> getAttendanceEventsForSite(final AttendanceSite aS) {
		log.debug("getAttendanceEventsForSite(AttendanceSite id)");

		return getEventsForAttendanceSiteHelper(aS);
	}

	/**
	 * {@inheritDoc}
	 */
	public Serializable addAttendanceEventNow(AttendanceEvent attendanceEvent) {
		log.debug("addAttendanceEventNow( {})", attendanceEvent.toString());

		try{
			return getHibernateTemplate().save(attendanceEvent);
		} catch (DataAccessException de) {
			log.error("addAttendanceEventNow failed.", de);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceEvent(AttendanceEvent aE) {
		log.debug("updateAttendanceEvent aE: {}", aE.getName());

		try{
			getHibernateTemplate().saveOrUpdate(aE);
			return true;
		} catch (DataAccessException e){
			log.error("updateAttendanceEvent failed.", e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteAttendanceEvent(AttendanceEvent aE) {
		log.debug("deleteAttendanceEvent aE: {}", aE.getName());

		if(aE.getStats() !=null && aE.getStats().getId() == null){
			aE.setStats(null);
		}

		try {
			getHibernateTemplate().delete(getHibernateTemplate().merge(aE));
			return true;
		} catch (DataAccessException e) {
    		log.error("deleteAttendanceEvent, {}, failed.", aE.getId(), e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceRecord getStatusRecord(final long id) {
		log.debug("getAttendanceRecord(){}", id);

		return (AttendanceRecord) getByIDHelper(id, QUERY_GET_ATTENDANCE_RECORD);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAttendanceRecord(AttendanceRecord aR) {
		log.debug("addAttendanceRecord sR for User '{}' event {} with Status {}", aR.getUserID(), aR.getAttendanceEvent().getName(), aR.getStatus().toString());

		try {
			getHibernateTemplate().save(aR);
			return true;
		} catch (DataAccessException de) {
			log.error("addAttendanceRecord failed.", de);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceRecord(AttendanceRecord aR) {
		try {
			getHibernateTemplate().saveOrUpdate(aR);
			return true;
		} catch (Exception e) {
			log.error("update attendanceRecord failed.", e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateAttendanceRecords(List<AttendanceRecord> aRs) {
		for(AttendanceRecord aR : aRs) {
			try {
				getHibernateTemplate().saveOrUpdate(aR);
				log.debug("save attendanceRecord id: " + aR.getId());
			} catch (Exception e) {
				log.error("update attendanceRecords failed.", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
     */
	public void updateAttendanceStatuses(List<AttendanceStatus> attendanceStatusList) {
		for(AttendanceStatus aS : attendanceStatusList) {
			try {
				getHibernateTemplate().saveOrUpdate(aS);
				log.debug("AttendanceStatus saved, id: " + aS.getId());
			} catch (Exception e) {
				log.error("update attendanceStatuses failed.", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceStatus> getActiveStatusesForSite(final AttendanceSite attendanceSite) {
		log.debug("getActiveStatusesForSite(AttendanceSite {} )", attendanceSite.getSiteID());

		try {
			return getHibernateTemplate().execute(session -> session
					.getNamedQuery(QUERY_GET_ACTIVE_ATTENDANCE_STATUSES_FOR_SITE)
					.setParameter(ATTENDANCE_SITE, attendanceSite)
					.getResultList());
		} catch (DataAccessException e) {
			log.error("getActiveStatusesForSite failed", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceStatus> getAllStatusesForSite(final AttendanceSite attendanceSite) {
		log.debug("getAllStatusesForSite(AttendanceSite attendanceSite)");

		try {
			return getHibernateTemplate().execute(session -> session
					.getNamedQuery(QUERY_GET_ALL_ATTENDANCE_STATUSES_FOR_SITE)
					.setParameter(ATTENDANCE_SITE, attendanceSite)
					.getResultList());
		} catch (DataAccessException e) {
			log.error("getAllStatusesForSite failed", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceStatus getAttendanceStatusById(final Long id) {
		log.debug("getAttendanceStatus(){}", id);

		return (AttendanceStatus) getByIDHelper(id, QUERY_GET_ATTENDANCE_STATUS);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceGrade getAttendanceGrade(final Long id) {
        log.debug("getAttendanceGrade, id: {}", id.toString());

		return (AttendanceGrade) getByIDHelper(id, QUERY_GET_ATTENDANCE_GRADE_BY_ID);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceGrade getAttendanceGrade(final String userID, final AttendanceSite aS) {
		log.debug("getAttendanceGrades for user {} in site {}", userID, aS.getSiteID());

		try{
			return (AttendanceGrade) getHibernateTemplate().execute(session -> session
					.getNamedQuery(QUERY_GET_ATTENDANCE_GRADE)
					.setParameter(ATTENDANCE_SITE, aS)
					.setParameter(USER_ID, userID)
					.uniqueResult());
		} catch (DataAccessException e) {
            log.error("Failed to get AttendanceGrade for {} in {}", userID, aS.getSiteID());
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceGrade> getAttendanceGrades(final AttendanceSite aS) {
		log.debug("getAttendanceGrades for: {}", aS.getSiteID());

		try{
			return getHibernateTemplate().execute(session -> session
					.getNamedQuery(QUERY_GET_ATTENDANCE_GRADES_FOR_SITE)
					.setParameter(ATTENDANCE_SITE, aS)
					.getResultList());
		} catch (DataAccessException e) {
            log.error("DataAccessException getting AttendanceGrades for {}. E:", aS.getSiteID(), e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAttendanceGrade(AttendanceGrade aG) {
		log.debug("addAttendanceGrade for User '{}' grade {} for site  {}", aG.getUserID(), aG.getGrade(), aG.getAttendanceSite().getSiteID());

		try {
			getHibernateTemplate().save(aG);
			return true;
		} catch (DataAccessException de) {
			log.error("addAttendanceGrade failed.", de);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceGrade(AttendanceGrade aG) {
		log.debug("updateAttendanceGrade for User '{}' grade {} for site  {}", aG.getUserID(), aG.getGrade(), aG.getAttendanceSite().getSiteID());

		try {
			getHibernateTemplate().saveOrUpdate(aG);
			return true;
		} catch (DataAccessException de) {
			log.error("updateAttendanceGrade failed.", de);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceUserStats getAttendanceUserStats(final String userId, final AttendanceSite aS) {
		log.debug("getAttendanceUserStats for User '{}' and Site: '{}'.", userId, aS.getSiteID());

		try{
			return (AttendanceUserStats) getHibernateTemplate().execute(session -> session
					.getNamedQuery(QUERY_GET_ATTENDANCE_USER_STATS)
					.setParameter(ATTENDANCE_SITE, aS)
					.setParameter(USER_ID, userId)
					.uniqueResult());
		} catch (DataAccessException e) {
    		log.error("DataAccessException getting AttendanceUserStats for User '{}' and Site: '{}'.", userId, aS.getSiteID(), e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceUserStats> getAttendanceUserStatsForSite(final AttendanceSite aS) {
		log.debug("getAttendanceUserStatsForSite for site: {}", aS.getSiteID());

		try{
			return getHibernateTemplate().execute(session -> session
					.getNamedQuery(QUERY_GET_ATTENDANCE_USER_STATS_FOR_SITE)
					.setParameter(ATTENDANCE_SITE, aS)
					.getResultList());
		} catch (DataAccessException e) {
			log.error("DataAccessException getting AttendanceUserStats for Site: " + aS.getSiteID() + ".", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceUserStats(AttendanceUserStats aUS) {
		log.debug("updateAttendanceUserStats for User '{}' and Site: '{}'.", aUS.getUserID(), aUS.getAttendanceSite().getSiteID());

		try {
			getHibernateTemplate().saveOrUpdate(aUS);
			return true;
		} catch (DataAccessException e) {
    		log.error("updateAttendanceUserStats, id: '{}' failed.", aUS.getId(), e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addGradingRule(GradingRule gradingRule) {
			log.debug("add grading rule to site {} status: {} range: {} - {} points: {}", gradingRule.getAttendanceSite().getSiteID(), gradingRule.getStatus(), gradingRule.getStartRange(), gradingRule.getEndRange(), gradingRule.getPoints());

		try {
			getHibernateTemplate().save(gradingRule);
			return true;
		} catch (DataAccessException dae) {
			log.error("addGradingRule failed.", dae);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteGradingRule(GradingRule gradingRule) {
			log.debug("Delete grading rule from site {} grading rule: {}", gradingRule.getAttendanceSite().getSiteID(), gradingRule.getId());

		try {
			getHibernateTemplate().delete(getHibernateTemplate().merge(gradingRule));
			return true;
		} catch (DataAccessException e) {
			log.error("deleteGradingRule, {}, failed.", gradingRule.getId(), e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceItemStats getAttendanceItemStats(AttendanceEvent aE) {
			log.debug("getAttendanceUserStats for Event '{}' and Site: '{}'.", aE.getName(), aE.getAttendanceSite().getSiteID());

		try{
			return (AttendanceItemStats) getHibernateTemplate().execute(session -> session
					.getNamedQuery(QUERY_GET_ATTENDANCE_ITEM_STATS)
					.setParameter(ATTENDANCE_EVENT, aE)
					.uniqueResult());
		} catch (DataAccessException e) {
			log.error("DataAccessException getting AttendanceItemStats for Event '{}' and Site: '{}'.", aE.getName(), aE.getAttendanceSite().getSiteID(), e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceItemStats(AttendanceItemStats aIS) {
			log.debug("updateAttendanceItemStats, '{}', for Event '{}' and site: '{}'.", aIS.getId(), aIS.getAttendanceEvent().getName(), aIS.getAttendanceEvent().getAttendanceSite().getSiteID());

		try {
			getHibernateTemplate().saveOrUpdate(aIS);
			return true;
		} catch (DataAccessException e) {
			log.error("updateAttendanceItemStats, '" + aIS.getId() + "' failed.", e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<GradingRule> getGradingRulesForSite(AttendanceSite attendanceSite) {
			log.debug("getGradingRulesForSite(AttendanceSite {} )", attendanceSite.getSiteID());

		try {
			return getHibernateTemplate().execute(session -> session
					.getNamedQuery(QUERY_GET_GRADING_RULES_FOR_SITE)
					.setParameter(ATTENDANCE_SITE, attendanceSite)
					.getResultList());
		} catch (DataAccessException e) {
			log.error("getGradingRulesForSite failed", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<Long> getAttendanceSiteBatch(final Date syncTime, final Long lastId) {
		final HibernateCallback<List<Long>> hcb = new HibernateCallback<List<Long>>() {
			@Override
			public List<Long> doInHibernate(Session session) throws HibernateException {
				Query q = session.getNamedQuery(QUERY_GET_ATTENDANCE_SITE_BATCH);
				q.setTimestamp(SYNC_TIME, syncTime);
				q.setLong(ID, lastId);
				q.setMaxResults(5);
				return q.list();
			}
		};

		return getHibernateTemplate().execute(hcb);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<Long> getAttendanceSitesInSync() {
		final HibernateCallback<List<Long>> hcb = new HibernateCallback<List<Long>>() {
			@Override
			public List<Long> doInHibernate(Session session) throws HibernateException {
				Query q = session.getNamedQuery(QUERY_GET_ATTENDANCE_SITES_IN_SYNC);
				return q.list();
			}
		};

		return getHibernateTemplate().execute(hcb);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean markAttendanceSiteForSync(final List<Long> ids, final Date syncTime) {
		final HibernateCallback hcb = new HibernateCallback() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				Query q = session.getNamedQuery(QUERY_MARK_ATTENDANCE_SITE_IN_SYNC);
				q.setParameterList(IDS, ids);
				q.setTimestamp(SYNC_TIME, syncTime);
				return q.executeUpdate();
			}
		};

		return getHibernateTemplate().execute(hcb).equals(ids.size());
	}

	/**
	 * init
	 */
	public void init() {
		log.debug("AttendanceDaoImpl init()");
	}

	@SuppressWarnings("unchecked")
	private List<AttendanceEvent> getEventsForAttendanceSiteHelper(final AttendanceSite aS){
		log.debug("getAttendanceEventsForSiteHelper()");

		try {
			return getHibernateTemplate().execute(session -> session
				.getNamedQuery(QUERY_GET_ATTENDANCE_EVENTS_FOR_SITE)
				.setParameter(ATTENDANCE_SITE, aS)
				.getResultList());

		} catch (DataAccessException e) {
			log.error("getEventsForAttendanceSiteHelper failed", e);
			return null;
		}
	}

	// Generic Function to get something by it's ID.
	private Object getByIDHelper(final long id, final String queryString) {
		log.debug("getByIDHelper() id: '{}' String: {}", id, queryString);

		try {
			return getHibernateTemplate().execute(session -> session
					.getNamedQuery(queryString)
					.setParameter(ID, id, new LongType())
					.setMaxResults(1)
					.uniqueResult());

		} catch (DataAccessException e) {
   		log.error("getByIDHelper for {} failed", queryString, e);
			return null;
		}
	}

}
