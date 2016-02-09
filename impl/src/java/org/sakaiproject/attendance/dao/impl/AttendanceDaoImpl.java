/*
 *  Copyright (c) 2016, University of Dayton
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

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.LongType;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.StringType;
import org.sakaiproject.attendance.dao.AttendanceDao;
import org.sakaiproject.attendance.model.*;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;


/**
 * Implementation of AttendanceDao
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class AttendanceDaoImpl extends HibernateDaoSupport implements AttendanceDao {

	private static final Logger log = Logger.getLogger(AttendanceDaoImpl.class);

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public AttendanceSite getAttendanceSite(final String siteID) {
		if(log.isDebugEnabled()){
			log.debug("getSiteBySite_ID ");
		}

		HibernateCallback hcb = new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
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
	public boolean addAttendanceSite(AttendanceSite aS) {
		if(log.isDebugEnabled()) {
			log.debug("addAttendanceSite ( " + aS.toString() + ")");
		}

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
		if(log.isDebugEnabled()) {
			log.debug("getAttendanceEvent()" + String.valueOf(id));
		}

		return (AttendanceEvent) getByIDHelper(id, QUERY_GET_ATTENDANCE_EVENT);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceEvent> getAttendanceEventsForSite(final AttendanceSite aS) {
		if(log.isDebugEnabled()) {
			log.debug("getAttendanceEventsForSite(AttendanceSite id)");
		}

		return getEventsForAttendanceSiteHelper(aS);
	}

	/**
	 * {@inheritDoc}
	 */
	public Serializable addAttendanceEventNow(AttendanceEvent attendanceEvent) {

		if(log.isDebugEnabled()) {
			log.debug("addAttendanceEventNow( " + attendanceEvent.toString() + ")");
		}

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
		if(log.isDebugEnabled()) {
			log.debug("updateAttendanceEvent aE: " + aE.getName());
		}

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
		if(log.isDebugEnabled()) {
			log.debug("deleteAttendanceEvent aE: " + aE.getName());
		}

		try {
			getHibernateTemplate().delete(aE);
			return true;
		} catch (DataAccessException e) {
			log.error("deleteAttendanceEvent, " + aE.getId() + ", failed.", e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceRecord getStatusRecord(final long id) {
		if(log.isDebugEnabled()) {
			log.debug("getAttendanceRecord()" + String.valueOf(id));
		}

		return (AttendanceRecord) getByIDHelper(id, QUERY_GET_ATTENDANCE_RECORD);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAttendanceRecord(AttendanceRecord aR) {
		if(log.isDebugEnabled()){
			log.debug("addAttendanceRecord sR for User '" + aR.getUserID() + "' event " + aR.getAttendanceEvent().getName() + " with Status " + aR.getStatus().toString());
		}

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
				log.info("save attendanceRecord id: " + aR.getId());
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
				log.info("AttendanceStatus saved, id: " + aS.getId());
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
		if(log.isDebugEnabled()){
			log.debug("getActiveStatusesForSite(AttendanceSite " + attendanceSite.getSiteID() + " )");
		}

		try {
			HibernateCallback hcb = new HibernateCallback() {
				@Override
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.getNamedQuery(QUERY_GET_ACTIVE_ATTENDANCE_STATUSES_FOR_SITE);
					q.setParameter(ATTENDANCE_SITE, attendanceSite, new ManyToOneType(null, "org.sakaiproject.attendance.model.AttendanceSite"));
					return q.list();
				}
			};

			return (List<AttendanceStatus>) getHibernateTemplate().executeFind(hcb);
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
		if(log.isDebugEnabled()){
			log.debug("getAllStatusesForSite(AttendanceSite attendanceSite)");
		}

		try {
			HibernateCallback hcb = new HibernateCallback() {
                @Override
                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    Query q = session.getNamedQuery(QUERY_GET_ALL_ATTENDANCE_STATUSES_FOR_SITE);
                    q.setParameter(ATTENDANCE_SITE, attendanceSite, new ManyToOneType(null, "org.sakaiproject.attendance.model.AttendanceSite"));
                    return q.list();
                }
            };

			return (List<AttendanceStatus>) getHibernateTemplate().executeFind(hcb);
		} catch (DataAccessException e) {
			log.error("getAllStatusesForSite failed", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceStatus getAttendanceStatusById(final Long id) {
		if(log.isDebugEnabled()) {
			log.debug("getAttendanceStatus()" + String.valueOf(id));
		}

		return (AttendanceStatus) getByIDHelper(id, QUERY_GET_ATTENDANCE_STATUS);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceGrade getAttendanceGrade(final Long id) {
		if(log.isDebugEnabled()) {
			log.debug("getAttendanceGrade, id: " + id.toString());
		}

		return (AttendanceGrade) getByIDHelper(id, QUERY_GET_ATTENDANCE_GRADE_BY_ID);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceGrade getAttendanceGrade(final String userID, final AttendanceSite aS) {
		if(log.isDebugEnabled()) {
			log.debug("getAttendanceGrades for user " + userID + " in site " + aS.getSiteID());
		}

		try{
			HibernateCallback hcb = new HibernateCallback() {
				@Override
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.getNamedQuery(QUERY_GET_ATTENDANCE_GRADE);
					q.setParameter(ATTENDANCE_SITE, aS, new ManyToOneType(null, "org.sakaiproject.attendance.model.AttendanceSite"));
					q.setParameter(USER_ID, userID, new StringType());
					return q.uniqueResult();
				}
			};

			return (AttendanceGrade) getHibernateTemplate().execute(hcb);
		} catch (DataAccessException e) {
			log.error("Failed to get AttendanceGrade for " + userID + " in " + aS.getSiteID());
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceGrade> getAttendanceGrades(final AttendanceSite aS) {
		if(log.isDebugEnabled()){
			log.debug("getAttendanceGrades for: " + aS.getSiteID());
		}

		try{
			HibernateCallback hcb = new HibernateCallback() {
				@Override
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Query q = session.getNamedQuery(QUERY_GET_ATTENDANCE_GRADES_FOR_SITE);
					q.setParameter(ATTENDANCE_SITE, aS, new ManyToOneType(null, "org.sakaiproject.attendance.model.AttendanceSite"));
					return q.list();
				}
			};

			return (List<AttendanceGrade>) getHibernateTemplate().executeFind(hcb);
		} catch (DataAccessException e) {
			log.error("DataAccessException getting AttendanceGrades for " + aS.getSiteID() + ". E:", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAttendanceGrade(AttendanceGrade aG) {
		if(log.isDebugEnabled()){
			log.debug("addAttendanceGrade for User '" + aG.getUserID() + "' grade " + aG.getGrade() + " for site  " + aG.getAttendanceSite().getSiteID());
		}

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
		if(log.isDebugEnabled()){
			log.debug("updateAttendanceGrade for User '" + aG.getUserID() + "' grade " + aG.getGrade() + " for site  " + aG.getAttendanceSite().getSiteID());
		}

		try {
			getHibernateTemplate().saveOrUpdate(aG);
			return true;
		} catch (DataAccessException de) {
			log.error("updateAttendanceGrade failed.", de);
			return false;
		}
	}

	/**
	 * init
	 */
	public void init() {
		log.info("init()");
	}

	@SuppressWarnings("unchecked")
	private List<AttendanceEvent> getEventsForAttendanceSiteHelper(final AttendanceSite aS){
		if(log.isDebugEnabled()){
			log.debug("getAttendanceEventsForSiteHelper()");
		}

		try {
			HibernateCallback hcb = new HibernateCallback() {
                @Override
                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    Query q = session.getNamedQuery(QUERY_GET_ATTENDANCE_EVENTS_FOR_SITE);
                    q.setParameter(ATTENDANCE_SITE, aS, new ManyToOneType(null, "org.sakaiproject.attendance.model.AttendanceSite"));
                    return q.list();
                }
            };

			return (List<AttendanceEvent>) getHibernateTemplate().executeFind(hcb);
		} catch (DataAccessException e) {
			log.error("getEventsForAttendanceSiteHelper failed", e);
			return null;
		}
	}

	// Generic Function to get something by it's ID.
	private Object getByIDHelper(final long id, final String queryString) {
		if(log.isDebugEnabled()) {
			log.debug("getByIDHelper() id: '" + String.valueOf(id) + "' String: " + queryString);
		}

		try {
			HibernateCallback hcb = new HibernateCallback() {
                @Override
                public Object doInHibernate(Session session) throws HibernateException, SQLException {
                    Query q = session.getNamedQuery(queryString);
                    q.setParameter(ID, id, new LongType());
                    q.setMaxResults(1);
                    return q.uniqueResult();
                }
            };

			return getHibernateTemplate().execute(hcb);
		} catch (DataAccessException e) {
			log.error("getByIDHelper for " + queryString + " failed", e);
			return null;
		}
	}
}
