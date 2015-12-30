/*
 *  Copyright (c) 2015, The Apereo Foundation
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import org.hibernate.type.LongType;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.StringType;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.sakaiproject.attendance.dao.AttendanceDao;

import org.sakaiproject.attendance.model.AttendanceSite;
//import org.sakaiproject.attendance.model.Reoccurrence;
import org.sakaiproject.attendance.model.StatusRecord;
import org.springframework.dao.DataAccessException;

import org.sakaiproject.attendance.model.AttendanceEvent;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * Implementation of AttendanceDao
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public class AttendanceDaoImpl extends HibernateDaoSupport implements AttendanceDao {

	private static final Logger log = Logger.getLogger(AttendanceDaoImpl.class);
	
	private PropertiesConfiguration statements;

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
	public List<AttendanceEvent> getAttendanceEvents() {
		if(log.isDebugEnabled()) {
			log.debug("getAttendanceEvents()");
		}

		HibernateCallback hcb = new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.getNamedQuery(QUERY_GET_ATTENDANCE_EVENTS);
				return q.list();
			}
		};

		return (List<AttendanceEvent>) getHibernateTemplate().execute(hcb);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceEvent> getAttendanceEventsForSite(final String siteID) {
		if(log.isDebugEnabled()) {
			log.debug("getAttendanceEventsForSite(String siteID)");
		}

		final AttendanceSite attendanceSite = getAttendanceSite(siteID);

		return getEventsForAttendanceSiteHelper(attendanceSite);
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
	public boolean addAttendanceEvent(AttendanceEvent attendanceEvent) {
		
		if(log.isDebugEnabled()) {
			log.debug("addAttendanceEvent( " + attendanceEvent.toString() + ")");
		}

		try{
			getHibernateTemplate().save(attendanceEvent);
			return true;
		} catch (DataAccessException de) {
			log.error("addAttendanceEvent failed.", de);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAttendanceEvents(ArrayList<AttendanceEvent> es){
		boolean isSuccess = false;

		for(AttendanceEvent e: es){
			isSuccess = addAttendanceEvent(e);
			if(!isSuccess){
				return isSuccess;
			}
		}

		return isSuccess;
	}

	/**
	 * {@inheritDoc}
	 */
/*	public boolean addReoccurrence(Reoccurrence r){
		if(log.isDebugEnabled()) {
			log.debug("addReoccurrence( " + r.toString() + ")");
		}

		try{
			getHibernateTemplate().save(r);
			return true;
		} catch (DataAccessException de) {
			log.error("addReoccurence failed.", de);
			return false;
		}
	}*/
	/**
	 * {@inheritDoc}
	 */
	public StatusRecord getStatusRecord(final long id) {
		if(log.isDebugEnabled()) {
			log.debug("getStatusRecord()" + String.valueOf(id));
		}

		return (StatusRecord) getByIDHelper(id, QUERY_GET_STATUS_RECORD);
	}

	/**
	 * {@inheritDoc}
     */
	@SuppressWarnings("unchecked")
	public List<StatusRecord> getStatusRecordsForAttendanceEvent(final AttendanceEvent aE) {
		if(log.isDebugEnabled()){
			log.debug("getStatusRecordsForAttendanceEvent e: " + aE.getName() + " in AttendanceSite: " + aE.getAttendanceSite().getSiteID());
		}

		HibernateCallback hcb = new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.getNamedQuery(QUERY_GET_STATUS_RECORDS_FOR_ATTENDANCE_EVENT);
				q.setParameter(ATTENDANCE_EVENT, aE, new ManyToOneType("org.sakaiproject.attendance.model.AttendanceEvent"));
				return q.list();
			}
		};

		return (List<StatusRecord>) getHibernateTemplate().executeFind(hcb);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addStatusRecord(StatusRecord sR) {
		if(log.isDebugEnabled()){
			log.debug("addStatusRecord sR for User '" + sR.getUserID() + "' event " + sR.getAttendanceEvent().getName() + " with Status " + sR.getStatus().toString());
		}

		try {
			getHibernateTemplate().save(sR);
			return true;
		} catch (DataAccessException de) {
			log.error("addStatusRecord failed.", de);
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
			log.debug("getAttendanceEventsForSite()");
		}

		HibernateCallback hcb = new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.getNamedQuery(QUERY_GET_ATTENDANCE_EVENTS_FOR_SITE);
				q.setParameter(ATTENDANCE_SITE, aS, new ManyToOneType("org.sakaiproject.attendance.model.AttendanceSite"));
				return q.list();
			}
		};

		return (List<AttendanceEvent>) getHibernateTemplate().executeFind(hcb);
	}

	// Generic Function to get something by it's ID.
	private Object getByIDHelper(final long id, final String queryString) {
		if(log.isDebugEnabled()) {
			log.debug("getByIDHelper() id: '" + String.valueOf(id) + "' String: " + queryString);
		}

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
	}
}
