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
import org.sakaiproject.attendance.model.*;
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

		return dao.addAttendanceEvent(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceEvent(AttendanceEvent aE) throws IllegalArgumentException {
		if(aE == null) {
			throw new IllegalArgumentException("AttendanceEvent is null");
		}

		return dao.updateAttendanceEvent(aE);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteAttendanceEvent(AttendanceEvent aE) throws IllegalArgumentException {
		if(aE == null) {
			throw new IllegalArgumentException("AttendanceEvent is null");
		}

		return dao.deleteAttendanceEvent(aE);
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
	public AttendanceRecord getAttendanceRecord(Long id) {
		if(id == null) {
			return null;
		}

		return dao.getStatusRecord(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceRecord> getAttendanceRecordsForUserInCurrentSite(String id) {
		return generateAttendanceRecords(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceRecord(AttendanceRecord aR) {
		if(aR == null) {
			throw new IllegalArgumentException("AttendanceRecord cannot be null");
		}

		return dao.updateAttendanceRecord(aR);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceRecords(List<AttendanceRecord> aRs) {
		if(aRs == null) {
			throw new IllegalArgumentException("AttendanceRecordList must not be null");
		}

		if(aRs.isEmpty()) {
			throw new IllegalArgumentException("AttendanceRecordList must not be empty");
		}

		return dao.updateAttendanceRecords(aRs);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceRecordsForEvent(AttendanceEvent aE, Status s) {
		List<AttendanceRecord> records = new ArrayList<AttendanceRecord>(aE.getRecords());

		if(records == null || records.isEmpty()) {
			records = generateAttendanceRecords(aE, s);
		}

		for(AttendanceRecord aR : records) {
			aR.setStatus(s);
		}

		return dao.updateAttendanceRecords(records);
	}

	public Map<Status, Integer> getStatsForEvent(AttendanceEvent event) {
		Map<Status, Integer> results = new HashMap<Status, Integer>();
		List<String> currentStudents = sakaiProxy.getCurrentSiteMembershipIds();

		for(Status s : Status.values()){
			generateStatsHelper(results, s, 0);
		}

		if(event != null) {
			for(AttendanceRecord r : event.getRecords()) {
				if(currentStudents.contains(r.getUserID())) {
					for(Status s : Status.values()){
						if(r.getStatus() == s) {
							generateStatsHelper(results, s, 1);
						}
					}
				}
			}
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateMissingRecordsForEvent(AttendanceEvent attendanceEvent, Status defaultStatus, List<String> missingStudentIds) {
		List<AttendanceRecord> recordList = new ArrayList<AttendanceRecord>();

		if(defaultStatus == null) {
			defaultStatus = attendanceEvent.getAttendanceSite().getDefaultStatus();
		}

		if(missingStudentIds != null && !missingStudentIds.isEmpty()) {
			for(String studentId : missingStudentIds) {
				AttendanceRecord attendanceRecord = generateAttendanceRecord(attendanceEvent, studentId, defaultStatus);
				if(attendanceRecord != null) {
					recordList.add(attendanceRecord);
				}
			}
			return dao.updateAttendanceRecords(recordList);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceStatus> getActiveStatusesForCurrentSite() {
		return getActiveStatusesForSite(getCurrentAttendanceSite());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceStatus> getActiveStatusesForSite(String siteId) {
		return dao.getActiveStatusesForSite(getAttendanceSite(siteId));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceStatus> getActiveStatusesForSite(AttendanceSite attendanceSite) {
		return dao.getActiveStatusesForSite(attendanceSite);
	}

	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		log.info("init");
	}

	private List<AttendanceRecord> generateAttendanceRecords(String id) {
		List<AttendanceEvent> aEs = getAttendanceEventsForCurrentSite();
		List<AttendanceRecord> records = new ArrayList<AttendanceRecord>(aEs.size());
		Status s = getCurrentAttendanceSite().getDefaultStatus();
		// Is there a faster way to do this? Would querying the DB be faster?
		for(AttendanceEvent e : aEs) {
			boolean recordPresent = false;
			Set<AttendanceRecord> eRecords = e.getRecords();

			if(!eRecords.isEmpty()) {
				for (AttendanceRecord r : eRecords) {
					if (r.getUserID().equals(id)) {
						recordPresent = true;
						records.add(r);
					}
				}
			}

			if(!recordPresent) {
				records.add(generateAttendanceRecord(e, id, s));
			}
		}

		return records;
	}

	private List<AttendanceRecord> generateAttendanceRecords(AttendanceEvent aE, Status s) {
		if(s == null) {
			s = aE.getAttendanceSite().getDefaultStatus();
		}

		List<AttendanceRecord> recordList = new ArrayList<AttendanceRecord>();
		List<User> userList = sakaiProxy.getCurrentSiteMembership();

		if(userList.isEmpty()){
			// do something
		}

		for(User user : userList) {
			AttendanceRecord attendanceRecord = generateAttendanceRecord(aE, user.getId(), s);
			recordList.add(attendanceRecord);
		}

		return recordList;
	}

	private AttendanceRecord generateAttendanceRecord(AttendanceEvent aE, String id, Status s) {
		if(s == null) {
			s = aE.getAttendanceSite().getDefaultStatus();
		}

		AttendanceRecord record = new AttendanceRecord(aE, id, s);
		dao.addAttendanceRecord(record);

		return record;
	}

	private void generateStatsHelper(Map<Status, Integer> m, Status s, int base) {
		if(m.containsKey(s)) {
			m.put(s, m.get(s) + 1);
		} else {
			m.put(s, base);
		}
	}
	
	@Setter
	private AttendanceDao dao;

	@Setter
	private SakaiProxy sakaiProxy;
}
