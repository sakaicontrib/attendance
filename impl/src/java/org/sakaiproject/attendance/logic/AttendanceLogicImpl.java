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

package org.sakaiproject.attendance.logic;

import java.util.*;

/*import com.google.ical.compat.jodatime.DateTimeIterator;
import com.google.ical.compat.jodatime.DateTimeIteratorFactory;
import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.values.DateValueImpl;
import com.google.ical.values.RRule;
import de.scravy.pair.Pair;
import de.scravy.pair.Pairs;*/
import lombok.Setter;

import org.apache.log4j.Logger;

//import org.joda.time.DateTime;
import org.sakaiproject.attendance.dao.AttendanceDao;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.StatusRecord;
import org.sakaiproject.user.api.User;
//import org.sakaiproject.attendance.model.Reoccurrence;

/**
 * Implementation of {@link AttendanceLogic}
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public class AttendanceLogicImpl implements AttendanceLogic {
	private static final Logger log = Logger.getLogger(AttendanceLogicImpl.class);

	/**
	 * {@inheritDoc}
     */
	public AttendanceSite getAttendanceSite(String siteID) {
		return dao.getAttendanceSite(siteID);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceSite getCurrentAttendanceSite() {
		String currentSiteID = sakaiProxy.getCurrentSiteId();

		AttendanceSite currentAttendanceSite = getAttendanceSite(currentSiteID);
		// is this the best way to do this?
		if(currentAttendanceSite == null) {
			currentAttendanceSite = new AttendanceSite(currentSiteID);
			if(addSite(currentAttendanceSite)){
				return currentAttendanceSite;
			}
			else {
				return null;
			}
		}

		return currentAttendanceSite;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addSite(AttendanceSite s) {
		return dao.addAttendanceSite(s);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceEvent getAttendanceEvent(long id) {
		return dao.getAttendanceEvent(id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceEvent> getAttendanceEvents() {
		return dao.getAttendanceEvents();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceEvent> getAttendanceEventsForSite(String siteID) {
		AttendanceSite aS = getAttendanceSite(siteID);
		return getAttendanceEventsForSite(aS);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceEvent> getAttendanceEventsForSite(AttendanceSite aS) {
		return dao.getAttendanceEventsForSite(aS);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceEvent> getAttendanceEventsForCurrentSite(){
		return getAttendanceEventsForSite(getCurrentAttendanceSite());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addAttendanceEvent(AttendanceEvent e) {
		AttendanceSite currentAttendanceSite = getCurrentAttendanceSite();

		e.setAttendanceSite(currentAttendanceSite);
		boolean returnVal = dao.addAttendanceEvent(e);

		if(returnVal) {
			// add StatusRecords for Each Student
			List<User> userList = sakaiProxy.getCurrentSiteMembership();
			for(User user : userList) {
				StatusRecord statusRecord = new StatusRecord(e, user.getId(), currentAttendanceSite.getDefaultStatus());
				returnVal = dao.addStatusRecord(statusRecord);
			}
		}

		return returnVal;
	}

/*	/**
	 * {@inheritDoc}
	 *//*
	public boolean addAttendanceEvents(AttendanceEvent t, RRule r){
		if(!t.getIsReoccurring()){ // Only for reoccurring events
			throw new IllegalArgumentException("Event must be reoccurring.");
		}

		Pair<Boolean, Long> rResult = addReoccurrence(r);
		if(!rResult.getFirst()) {
			return false;
		}

		ArrayList<AttendanceEvent> events = new ArrayList<AttendanceEvent>();
		Calendar c = Calendar.getInstance();
		c.setTime(t.getStartDateTime());
		DateValueImpl startDateValue = new DateValueImpl(c.get(Calendar.DATE), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
		RecurrenceIterator reocurrenceIterator = RecurrenceIteratorFactory.createRecurrenceIterator(r, startDateValue, TimeZone.getDefault());
		DateTimeIterator di = DateTimeIteratorFactory.createDateTimeIterator(reocurrenceIterator);
		for(;di.hasNext();){
			DateTime dt = di.next();
			AttendanceEvent copy = new Event(t);
			copy.setStartDateTime(dt.toDate());

			events.add(copy);
		}

		return dao.addAttendanceEvents(events);
	}

	/**
	 * {@inheritDoc}
	 *//*
	public Pair<Boolean, Long> addReoccurrence(RRule r){
		Reoccurrence reoccurrence = new Reoccurrence(null, r.toIcal());

		boolean rResult = addReoccurrence(reoccurrence);
		Pair<Boolean, Long> result = Pairs.from(rResult, reoccurrence.getId());

		return result;
	}*/

	/**
	 * {@inheritDoc}
	 */
	/*public boolean addReoccurrence(Reoccurrence r) {
		return dao.addReoccurrence(r);
	}*/

	/**
	 * {@inheritDoc}
	 */
	public StatusRecord getStatusRecord(Long id) {
		if(id == null) {
			return null;
		}

		return dao.getStatusRecord(id);
	}

	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		log.info("init");
	}
	
	@Setter
	private AttendanceDao dao;

	@Setter
	private SakaiProxy sakaiProxy;
}
