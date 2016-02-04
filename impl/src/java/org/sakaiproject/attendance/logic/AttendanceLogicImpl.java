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

package org.sakaiproject.attendance.logic;

import java.io.Serializable;
import java.util.*;

import lombok.Setter;

import org.apache.log4j.Logger;

import org.sakaiproject.attendance.dao.AttendanceDao;
import org.sakaiproject.attendance.model.*;
import org.sakaiproject.user.api.User;

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
	public boolean updateAttendanceSite(AttendanceSite aS) throws IllegalArgumentException {
		if(aS == null) {
			throw new IllegalArgumentException("AttendanceSite must not be null");
		}

		return dao.updateAttendanceSite(aS);
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
			if(!addSite(currentAttendanceSite)){
				return null;
			}
		}

		if(generateMissingAttendanceStatusesForSite(currentAttendanceSite)) {
			currentAttendanceSite = getAttendanceSite(currentSiteID);
		}

		return currentAttendanceSite;
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
	public List<AttendanceEvent> getAttendanceEventsForSite(AttendanceSite aS) {
		return safeAttendanceEventListReturn(dao.getAttendanceEventsForSite(aS));
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
	public Serializable addAttendanceEventNow(AttendanceEvent e) {
		return dao.addAttendanceEventNow(e);
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
	public List<AttendanceRecord> getAttendanceRecordsForUser(String id) {
		return getAttendanceRecordsForUser(id, getCurrentAttendanceSite());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceRecord> getAttendanceRecordsForUser(String id, AttendanceSite aS) {
		return generateAttendanceRecords(id, aS);
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
	public List<AttendanceStatus> getActiveStatusesForSite(AttendanceSite attendanceSite) {
		return safeAttendanceStatusListReturn(dao.getActiveStatusesForSite(attendanceSite));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceStatus> getAllStatusesForSite(AttendanceSite attendanceSite) {
		return safeAttendanceStatusListReturn(dao.getAllStatusesForSite(attendanceSite));
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceStatus getAttendanceStatusById(Long id) {
		return dao.getAttendanceStatusById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceRecord(AttendanceRecord aR) throws IllegalArgumentException {
		if(aR == null) {
			throw new IllegalArgumentException("AttendanceRecord cannot be null");
		}

		return dao.updateAttendanceRecord(aR);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceRecordsForEvent(AttendanceEvent aE, Status s) {
		aE = getAttendanceEvent(aE.getId());
		List<AttendanceRecord> records = new ArrayList<AttendanceRecord>(aE.getRecords());

		if(records.isEmpty()) {
			records = generateAttendanceRecords(aE, s);
		}

		for(AttendanceRecord aR : records) {
			aR.setStatus(s);
		}

		return dao.updateAttendanceRecords(records);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceRecordsForEvent(AttendanceEvent aE, Status s, String groupId) {
		if(groupId == null || groupId.isEmpty()) {
			return updateAttendanceRecordsForEvent(aE, s);
		} else {
			aE = getAttendanceEvent(aE.getId());
			List<AttendanceRecord> allRecords = new ArrayList<AttendanceRecord>(aE.getRecords());
			List<AttendanceRecord> recordsToUpdate = new ArrayList<AttendanceRecord>();

			if(allRecords.isEmpty()) {
				allRecords = generateAttendanceRecords(aE, s);
			}

			// We only want to update records where the user is in the group
			List<String> groupMemberIds = sakaiProxy.getGroupMembershipIds(aE.getAttendanceSite().getSiteID(), groupId);
			for(String userId : groupMemberIds) {
				for(AttendanceRecord record: allRecords) {
					if(record.getUserID().equals(userId)) {
						recordsToUpdate.add(record);
					}
				}
			}

			for(AttendanceRecord aR : recordsToUpdate) {
				aR.setStatus(s);
			}

			return dao.updateAttendanceRecords(recordsToUpdate);
		}
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
	public Map<Status, Integer> getStatsForUser(String userId) {
		return getStatsForUser(userId, getCurrentAttendanceSite());
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<Status, Integer> getStatsForUser(String userId, AttendanceSite aS) {
		Map<Status, Integer> results = new HashMap<Status, Integer>();

		for(Status s : Status.values()){
			generateStatsHelper(results, s, 0);
		}

		List<AttendanceRecord> records = getAttendanceRecordsForUser(userId, aS);

		if(!records.isEmpty()) {
			for(AttendanceRecord record : records) {
				if(record.getUserID().equals(userId)){
					generateStatsHelper(results, record.getStatus(), 1);
				}
			}
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceGrade getAttendanceGrade(Long id) throws IllegalArgumentException {
		if(id == null) {
			throw new IllegalArgumentException("ID must not be null");
		}

		return dao.getAttendanceGrade(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceGrade getAttendanceGrade(String uID) throws IllegalArgumentException {
		if(uID == null || uID.isEmpty()) {
			throw new IllegalArgumentException("uID must not be null or empty.");
		}

		return dao.getAttendanceGrade(uID, getCurrentAttendanceSite());
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, AttendanceGrade> getAttendanceGrades() {
		Map<String, AttendanceGrade> aGHashMap = new HashMap<String, AttendanceGrade>();
		AttendanceSite aS = getCurrentAttendanceSite();
		List<AttendanceGrade> aGs = dao.getAttendanceGrades(aS);
		if(aGs == null || aGs.isEmpty()) {
			aGs = generateAttendanceGrades(aS);
		} else {
			List<String> userList = sakaiProxy.getCurrentSiteMembershipIds();
			for(AttendanceGrade aG : aGs) {
				userList.remove(aG.getUserID());
			}

			if(!userList.isEmpty()) {
				for(String u : userList) {
					aGs.add(generateAttendanceGrade(u, aS));
				}
			}
		}

		for(AttendanceGrade aG : aGs) {
			aGHashMap.put(aG.getUserID(), aG);
		}

		return aGHashMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getAttendanceGradeScores() {
		Map<String, AttendanceGrade> gradeMap = getAttendanceGrades();

		Map<String, String> returnMap = new HashMap<String, String>(gradeMap.size());

		for(Map.Entry<String, AttendanceGrade> entry : gradeMap.entrySet())
		{
			Double doubleValue = entry.getValue().getGrade();
			String value = doubleValue == null ? null : doubleValue.toString();
			returnMap.put(entry.getKey(), value);
		}

		return returnMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateAttendanceGrade(AttendanceGrade aG) throws IllegalArgumentException {
		if(aG == null) {
			throw new IllegalArgumentException("AttendanceGrade cannot be null");
		}

		return dao.updateAttendanceGrade(aG);
	}

	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		log.info("init");
	}

	private boolean addSite(AttendanceSite s) {
		return dao.addAttendanceSite(s);
	}

	private List<AttendanceGrade> generateAttendanceGrades(AttendanceSite aS) {
		List<String> userList = sakaiProxy.getCurrentSiteMembershipIds();
		List<AttendanceGrade> aGList = new ArrayList<AttendanceGrade>(userList.size());

		for(String u : userList){
			aGList.add(generateAttendanceGrade(u, aS));
		}

		return aGList;
	}

	private AttendanceGrade generateAttendanceGrade(String userId, AttendanceSite aS) {
		if(aS == null) {
			aS = getCurrentAttendanceSite();
		}

		AttendanceGrade grade = new AttendanceGrade(aS, userId);
		dao.addAttendanceGrade(grade);
		return grade;
	}

	private List<AttendanceRecord> generateAttendanceRecords(String id, AttendanceSite aS) {
		List<AttendanceEvent> aEs = getAttendanceEventsForSite(aS);
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

	private boolean generateMissingAttendanceStatusesForSite(AttendanceSite attendanceSite) {
		Set<AttendanceStatus> currentAttendanceStatuses = attendanceSite.getAttendanceStatuses();
		List<Status> previouslyCreatedStatuses = new ArrayList<Status>();
		List<AttendanceStatus> statusesToBeAdded = new ArrayList<AttendanceStatus>();

		// Get the max sort order
		int nextSortOrder = getNextSortOrder(new ArrayList<AttendanceStatus>(currentAttendanceStatuses));

		// Generate the list of statuses that already have a record for the site
		for (AttendanceStatus attendanceStatus : currentAttendanceStatuses) {
			previouslyCreatedStatuses.add(attendanceStatus.getStatus());
		}

		// Add attendance status record for each Status that is missing a record
		for (Status status : Status.values()) {
			if (!previouslyCreatedStatuses.contains(status)) {
				AttendanceStatus missingAttendanceStatus = new AttendanceStatus();
				missingAttendanceStatus.setAttendanceSite(attendanceSite);
				missingAttendanceStatus.setStatus(status);
				missingAttendanceStatus.setIsActive(true);
				missingAttendanceStatus.setSortOrder(nextSortOrder);
				nextSortOrder++;
				statusesToBeAdded.add(missingAttendanceStatus);
			}
		}

		return statusesToBeAdded.isEmpty() || dao.updateAttendanceStatuses(statusesToBeAdded);

	}

	private int getNextSortOrder(List<AttendanceStatus> attendanceStatusList) {
		int maxSortOrder = -1;

		for(AttendanceStatus attendanceStatus : attendanceStatusList) {
			if(attendanceStatus.getSortOrder() > maxSortOrder) {
				maxSortOrder = attendanceStatus.getSortOrder();
			}
		}

		return maxSortOrder + 1;
	}

	private List<AttendanceEvent> safeAttendanceEventListReturn(List<AttendanceEvent> l) {
		if(l == null) {
			return new ArrayList<AttendanceEvent>();
		}

		return l;
	}

	private List<AttendanceStatus> safeAttendanceStatusListReturn(List<AttendanceStatus> l) {
		if(l == null) {
			return new ArrayList<AttendanceStatus>();
		}

		return l;
	}
	
	@Setter
	private AttendanceDao dao;

	@Setter
	private SakaiProxy sakaiProxy;
}
