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

package org.sakaiproject.attendance.impl.logic;

import java.util.*;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.attendance.api.AttendanceGradebookProvider;
import org.sakaiproject.attendance.api.logic.AttendanceLogic;
import org.sakaiproject.attendance.api.logic.SakaiProxy;
import org.sakaiproject.attendance.api.model.*;
import org.sakaiproject.attendance.api.model.stats.AttendanceItemStats;
import org.sakaiproject.attendance.api.model.stats.AttendanceStats;
import org.sakaiproject.attendance.api.model.stats.AttendanceUserStats;
import org.sakaiproject.attendance.api.repository.*;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.user.api.User;

/**
 * Implementation of {@link AttendanceLogic}
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Slf4j
@Setter
public class AttendanceLogicImpl implements AttendanceLogic, EntityTransferrer {

	private AttendanceEventRepository attendanceEventRepository;

	private AttendanceGradeRepository attendanceGradeRepository;

	private AttendanceRecordRepository attendanceRecordRepository;

	private AttendanceSiteRepository attendanceSiteRepository;

	private AttendanceStatusRepository attendanceStatusRepository;

	private GradingRuleRepository gradingRuleRepository;

	private SakaiProxy sakaiProxy;

	private AttendanceGradebookProvider attendanceGradebookProvider;

	private EntityManager entityManager;

	/**
	 * {@inheritDoc}
	 */
	public AttendanceSite getAttendanceSite(String siteID) {
		AttendanceSite attendanceSite = attendanceSiteRepository.findBySiteId(siteID);

		if (attendanceSite == null) {
			attendanceSite = new AttendanceSite(siteID);

			// This will create the site and add statuses to the new site
			if (!addSite(attendanceSite)) {
				return null;
			}

			// We need to re-load the AttendanceSite because of the status creation above
			attendanceSite = attendanceSiteRepository.findBySiteId(siteID);
		}

		return attendanceSite;
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceSite updateAttendanceSite(AttendanceSite aS) throws IllegalArgumentException {
		if(aS == null) {
			throw new IllegalArgumentException("AttendanceSite must not be null");
		}
		return attendanceSiteRepository.save(aS);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceSite getCurrentAttendanceSite() {
		String currentSiteID = sakaiProxy.getCurrentSiteId();
		return getAttendanceSite(currentSiteID);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceEvent getAttendanceEvent(long id) {
		return attendanceEventRepository.findById(id).orElseThrow(
				() -> new IllegalArgumentException("No event for id " + id)
		);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceEvent> getAttendanceEventsForSite(AttendanceSite aS) {
		return attendanceEventRepository.findAllByAttendanceSite(aS);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceEvent> getAttendanceEventsForCurrentSite(){
		return attendanceEventRepository.findAllByAttendanceSite(getCurrentAttendanceSite());
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceEvent addAttendanceEventNow(AttendanceEvent e) {
		return attendanceEventRepository.save(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceEvent updateAttendanceEvent(AttendanceEvent aE) throws IllegalArgumentException {
		if(aE == null) {
			throw new IllegalArgumentException("AttendanceEvent is null");
		}
		return attendanceEventRepository.save(aE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAttendanceEvent(AttendanceEvent aE) throws IllegalArgumentException {
		if(aE == null) {
			throw new IllegalArgumentException("AttendanceEvent is null");
		}
		attendanceEventRepository.deleteById(aE.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	public Optional<AttendanceRecord> getAttendanceRecord(Long id) {
		if(id == null) {
			return Optional.empty();
		}
		return attendanceRecordRepository.findById(id);
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
		return attendanceStatusRepository.findAllActiveByAttendanceSite(attendanceSite);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceStatus> getAllStatusesForSite(AttendanceSite attendanceSite) {
		return attendanceStatusRepository.findAllByAttendanceSite(attendanceSite);
	}

	/**
	 * {@inheritDoc}
	 */
	public Optional<AttendanceStatus> getAttendanceStatusById(Long id) {
		return attendanceStatusRepository.findById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceRecord updateAttendanceRecord(AttendanceRecord aR, Status oldStatus) throws IllegalArgumentException {
		if(aR == null) {
			throw new IllegalArgumentException("AttendanceRecord cannot be null");
		}
		regradeForAttendanceRecord(aR);
		return attendanceRecordRepository.save(aR);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceRecord> updateAttendanceRecordsForEvent(AttendanceEvent aE, Status s) {
		aE = getAttendanceEvent(aE.getId());
		List<AttendanceRecord> records = new ArrayList<>(aE.getRecords());
		if(records.isEmpty()) {
			return generateAttendanceRecords(aE, s);
		}
		for(AttendanceRecord aR : records) {
			aR.setStatus(s);
		}
		return records;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateAttendanceRecordsForEvent(AttendanceEvent aE, Status s, String groupId) {
		aE = getAttendanceEvent(aE.getId());	//why are we getting this as a new event when it's a parameter passed in already?
		List<AttendanceRecord> allRecords = new ArrayList<>(aE.getRecords());
		List<AttendanceRecord> recordsToUpdate = new ArrayList<>();
		if(allRecords.isEmpty()) {
			allRecords = generateAttendanceRecords(aE, null);
		} else {
			List<String> ids = sakaiProxy.getCurrentSiteMembershipIds();
			allRecords.forEach(record -> ids.remove(record.getUserID()));
			allRecords.addAll(updateMissingRecordsForEvent(aE, null, ids));
		}
		if(groupId == null || groupId.isEmpty()) {
			recordsToUpdate.addAll(allRecords);
		} else {
			List<String> groupMemberIds = sakaiProxy.getGroupMembershipIds(aE.getAttendanceSite().getSiteID(), groupId);
			for(String userId : groupMemberIds) {
				for(AttendanceRecord record: allRecords) {
					if(record.getUserID().equals(userId)) {
						recordsToUpdate.add(record);
						break;	//once we've found the record, the loop can break.
					}
				}
			}
		}
		for(AttendanceRecord aR : recordsToUpdate) {
			aR.setStatus(s);
			regradeForAttendanceRecord(aR);
		}
		attendanceRecordRepository.saveAll(recordsToUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceRecord> updateMissingRecordsForEvent(AttendanceEvent attendanceEvent, Status defaultStatus, List<String> missingStudentIds)
	throws IllegalArgumentException {
		if(attendanceEvent == null) {
			throw new IllegalArgumentException("attendanceEvent cannot be null.");
		}

		List<AttendanceRecord> recordList = new ArrayList<>();

		if(defaultStatus == null) {
			defaultStatus = attendanceEvent.getAttendanceSite().getDefaultStatus();
		}

		if(missingStudentIds != null && !missingStudentIds.isEmpty()) {
			for(String studentId : missingStudentIds) {
				AttendanceRecord attendanceRecord = generateAttendanceRecord(attendanceEvent, studentId, defaultStatus);
				recordList.add(attendanceRecord);
			}
		}

		return recordList;
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceItemStats getStatsForEvent(AttendanceEvent event) {
		return attendanceEventRepository.calculateStatsForEvent(event);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceUserStats getStatsForUser(String userId) {
		return getStatsForUser(userId, getCurrentAttendanceSite());
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceUserStats getStatsForUser(String userId, AttendanceSite aS) {
		return attendanceSiteRepository.calculateAttendanceUserStats(userId, aS);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceUserStats> getUserStatsForCurrentSite(String group) {
		return getUserStatsForSite(getCurrentAttendanceSite(), group);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AttendanceUserStats> getUserStatsForSite(AttendanceSite aS, String group) {
		List<String> users;
		if(group == null || group.isEmpty()) {
			users = sakaiProxy.getCurrentSiteMembershipIds();
		} else {
			users = sakaiProxy.getGroupMembershipIds(aS.getSiteID(), group);
		}
		List<AttendanceUserStats> returnList = new ArrayList<>(users.size());
		users.forEach(user -> {
			returnList.add(attendanceSiteRepository.calculateAttendanceUserStats(user, aS));
		});
		return returnList;
	}

	/**
	 * {@inheritDoc}
	 */
	public Optional<AttendanceGrade> getAttendanceGrade(Long id) throws IllegalArgumentException {
		if(id == null) {
			throw new IllegalArgumentException("ID must not be null");
		}
		return attendanceGradeRepository.findById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttendanceGrade getAttendanceGrade(String userId) throws IllegalArgumentException {
		if(StringUtils.isBlank(userId)) {
			throw new IllegalArgumentException("uID must not be null or empty.");
		}
		final AttendanceSite currentSite = getCurrentAttendanceSite();
		return attendanceGradeRepository.findByAttendanceSiteAndUserId(currentSite, userId).orElse(
				new AttendanceGrade(currentSite, userId)
		);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, AttendanceGrade> getAttendanceGrades() {
		Map<String, AttendanceGrade> aGHashMap = new HashMap<>();
		AttendanceSite aS = getCurrentAttendanceSite();
		List<AttendanceGrade> aGs = attendanceGradeRepository.findAllByAttendanceSite(aS);
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

		Map<String, String> returnMap = new HashMap<>(gradeMap.size());

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
	public AttendanceGrade updateAttendanceGrade(AttendanceGrade aG) throws IllegalArgumentException {
		if(aG == null) {
			throw new IllegalArgumentException("AttendanceGrade cannot be null");
		}

		AttendanceGrade savedGrade = attendanceGradeRepository.save(aG);

		if(aG.getAttendanceSite().getSendToGradebook()) {
			if (!attendanceGradebookProvider.sendToGradebook(aG)) {
				log.warn("Unable to send attendance grade to the Gradebook in site " + aG.getAttendanceSite().getSiteID());
			}
		}
		return savedGrade;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getStatsForStatus(AttendanceStats stats, Status status) {
		int stat = 0;

		if (status == Status.PRESENT) {
			stat = stats.getPresent();
		} else if (status == Status.UNEXCUSED_ABSENCE) {
			stat = stats.getUnexcused();
		} else if (status == Status.EXCUSED_ABSENCE) {
			stat = stats.getExcused();
		} else if (status == Status.LATE) {
			stat = stats.getLate();
		} else if (status == Status.LEFT_EARLY) {
			stat = stats.getLeftEarly();
		}

		return stat;
	}

	/**
	 * {@inheritDoc}
	 */
	public GradingRule addGradingRule(GradingRule gradingRule) {
		return gradingRuleRepository.save(gradingRule);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteGradingRule(GradingRule gradingRule) {
		gradingRuleRepository.deleteById(gradingRule.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<GradingRule> getGradingRulesForSite(AttendanceSite attendanceSite) {
		return gradingRuleRepository.findAllByAttendanceSite(attendanceSite);
	}

	/**
	 * {@inheritDoc}
	 */
	public void regradeAll(AttendanceSite attendanceSite) {
		List<String> users = sakaiProxy.getSiteMembershipIds(attendanceSite.getSiteID());
		if (users != null) {
			for (String userId : users) {
				AttendanceGrade grade = getAttendanceGrade(userId);
				if (grade.getOverride() == null || !grade.getOverride()) {
					grade.setGrade(grade(userId, attendanceSite));
					updateAttendanceGrade(grade);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Double regrade(AttendanceGrade attendanceGrade, boolean saveGrade) {
		Double grade = grade(attendanceGrade.getUserID(), attendanceGrade.getAttendanceSite());
		if (saveGrade) {
			attendanceGrade.setGrade(grade);
			updateAttendanceGrade(attendanceGrade);
		}
		return grade;
	}

	private boolean addSite(AttendanceSite s) {
		AttendanceSite attendanceSite = attendanceSiteRepository.save(s);
		if (attendanceSite != null) {
			generateMissingAttendanceStatusesForSite(s);
			return true;
		}

		return false;
	}

	private List<AttendanceGrade> generateAttendanceGrades(AttendanceSite aS) {
		List<String> userList = sakaiProxy.getCurrentSiteMembershipIds();
		List<AttendanceGrade> aGList = new ArrayList<>(userList.size());

		for(String u : userList){
			aGList.add(generateAttendanceGrade(u, aS));
		}

		return aGList;
	}

	private AttendanceGrade generateAttendanceGrade(String userId, AttendanceSite aS) {
		if(aS == null) {
			aS = getCurrentAttendanceSite();
		}

		return new AttendanceGrade(aS, userId);
	}

	private List<AttendanceRecord> generateAttendanceRecords(String id, AttendanceSite aS) {
		List<AttendanceEvent> aEs = getAttendanceEventsForSite(aS);
		List<AttendanceRecord> records = new ArrayList<>(aEs.size());
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

		List<AttendanceRecord> recordList = new ArrayList<>();
		List<User> userList = sakaiProxy.getCurrentSiteMembership();

		if(userList.isEmpty()){
			return recordList;
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

		return new AttendanceRecord(aE, id, s);
	}

	private void generateMissingAttendanceStatusesForSite(AttendanceSite attendanceSite) {
		Set<AttendanceStatus> currentAttendanceStatuses = attendanceSite.getAttendanceStatuses();
		List<Status> previouslyCreatedStatuses = new ArrayList<>();
		List<AttendanceStatus> statusesToBeAdded = new ArrayList<>();

		// Get the max sort order
		int nextSortOrder = getNextSortOrder(new ArrayList<>(currentAttendanceStatuses));

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

		attendanceStatusRepository.saveAll(statusesToBeAdded);
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

	private void regradeForAttendanceRecord(AttendanceRecord attendanceRecord) {
		final AttendanceSite currentSite = getCurrentAttendanceSite();
		// Auto grade if valid record, maximum points is set, and auto grade is enabled
		if (attendanceRecord != null && currentSite.getMaximumGrade() != null && currentSite.getMaximumGrade() > 0 && currentSite.getUseAutoGrading()) {
			final String userId = attendanceRecord.getUserID();
			AttendanceGrade attendanceGrade = getAttendanceGrade(userId);
			// Only update if not overridden
			if (attendanceGrade.getOverride()== null || !attendanceGrade.getOverride()) {
				attendanceGrade.setGrade(grade(userId, currentSite));
				updateAttendanceGrade(attendanceGrade);
			}
		}
	}

	// Must be non-null user and site
	private Double grade(String userId, AttendanceSite attendanceSite) {
		final List<GradingRule> rules = getGradingRulesForSite(attendanceSite);
		final AttendanceUserStats userStats = getStatsForUser(userId);

		double totalPoints = 0D;
		Double maximumGrade = attendanceSite.getMaximumGrade();
		if (attendanceSite.getAutoGradeBySubtraction() && maximumGrade != null) {
			totalPoints = maximumGrade;
		}

		if (rules != null) {
			for (GradingRule rule : rules) {
				Integer statusTotal = null;
				switch (rule.getStatus()) {
					case PRESENT:
						statusTotal = userStats.getPresent();
						break;
					case UNEXCUSED_ABSENCE:
						statusTotal = userStats.getUnexcused();
						break;
					case EXCUSED_ABSENCE:
						statusTotal = userStats.getExcused();
						break;
					case LATE:
						statusTotal = userStats.getLate();
						break;
					case LEFT_EARLY:
						statusTotal = userStats.getLeftEarly();
						break;
				}
				if (statusTotal != null && (statusTotal >= rule.getStartRange() && (rule.getEndRange() == null || statusTotal <= rule.getEndRange()))) {
					totalPoints = Double.sum(totalPoints, rule.getPoints());
				}
			}
		}

		// Don't allow negative total points
		return Math.max(totalPoints, 0D);
	}

	@Override
	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions) {
		return transferCopyEntities(fromContext, toContext, ids, transferOptions, false);
	}

	@Override
	public String[] myToolIds() {
		return new String[]{ "sakai.attendance" };
	}

	@Override
	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions, boolean cleanup) {
		Map<String, String> transversalMap = new HashMap<>();
		AttendanceSite fromSite = getAttendanceSite(fromContext);
		AttendanceSite toSite = getAttendanceSite(toContext);

		if (cleanup) {
			// TODO: implement deleting all content in toContext
			// Maybe wait until soft delete is confirmed everywhere
		}

		try {
			// TODO: consider bringing over the statuses from the original site

			List<AttendanceEvent> fromEvents = getAttendanceEventsForSite(fromSite);
			for (AttendanceEvent fromEvent : fromEvents) {
				AttendanceEvent toEvent = new AttendanceEvent();
				toEvent.setAttendanceSite(toSite);
				toEvent.setName(fromEvent.getName());

				if (fromEvent.getStartDateTime() != null) {
					toEvent.setStartDateTime(fromEvent.getStartDateTime());
				}

				addAttendanceEventNow(toEvent);
				log.info("transferCopyEntities: new attendance event ({})", toEvent.getName());

				transversalMap.put("attendance/" + fromEvent.getId(), "attendance/" + toEvent.getId());
			}
		} catch (Exception e) {
			log.error("transferCopyEntities error", e);
		}

		return transversalMap;
	}

	@Override
	public void updateEntityReferences(String toContext, Map<String, String> transversalMap) {
		return;
	}
}
