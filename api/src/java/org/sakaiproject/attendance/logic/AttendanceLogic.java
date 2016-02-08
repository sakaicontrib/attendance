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
import java.util.List;
import java.util.Map;

import org.sakaiproject.attendance.model.*;

/**
 * The brains of the operation.
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public interface AttendanceLogic {

	/**
	 * Gets the AttendanceSite by Sakai Site ID
	 *
	 * @param siteID, sakai Site ID
	 * @return the Attendance Site
     */
	AttendanceSite getAttendanceSite(String siteID);

	/**
	 * Updates an AttendanceSite
	 *
	 * @param aS, the AttendanceSite to update (must not be null)
	 * @return success of the operation
     */
	boolean updateAttendanceSite(AttendanceSite aS) throws IllegalArgumentException;

	/**
	 * Get's the current AttendanceSite
	 *
	 * @return The Current Attendance Site
     */
	AttendanceSite getCurrentAttendanceSite();

	/**
	 * Get an Attendance Event by ID
	 *
	 * @param id, the ID of the AttendanceEvent
	 * @return the AttendanceEvent
     */
	AttendanceEvent getAttendanceEvent(long id);

	/**
	 * gets all the events by AttendanceSite
	 * @param aS, the AttendanceSite
	 * @return List of events, or empty
     */
	List<AttendanceEvent> getAttendanceEventsForSite(AttendanceSite aS);

	/**
	 * Get AttendanceEvents for current site
	 *
	 * @return list of AttendanceEvents
     */
	List<AttendanceEvent> getAttendanceEventsForCurrentSite();

	/**
	 * A Serializable function which returns the ID of the newly added AttendanceEvent
	 *
	 * @param e, the AttendanceEvent to add
	 * @return the Long ID of the AttendanceEvent added
     */
	Serializable addAttendanceEventNow(AttendanceEvent e);

	/**
	 * Updates an AttendanceEvent
	 *
	 * @param aE, the event to update (must not be null)
	 * @return success of the operation
     */
	boolean updateAttendanceEvent(AttendanceEvent aE) throws IllegalArgumentException;

	/**
	 * Deletes an AttendanceEvent
	 *
	 * @param aE, the event to delete (must not be null) // though this can probably be changed
	 * @return success of the operation
     */
	boolean deleteAttendanceEvent(AttendanceEvent aE) throws IllegalArgumentException;

	/**
	 * Get's an AttendanceRecord by ID
	 *
	 * @param id, the id of the attendanceRecord
	 * @return the attendanceRecord
     */
	AttendanceRecord getAttendanceRecord(Long id);

	/**
	 * get AttendanceRecords For a User in the current site
	 *
	 * @param id, the ID of the user
	 * @return a List of AttendanceRecords
     */
	List<AttendanceRecord> getAttendanceRecordsForUser(String id);

	/**
	 * get AttendanceRecords for User in AttendanceSite
	 *
	 * @param id, the User ID
	 * @param aS, the AttendanceSite
	 * @return a List of AttendanceRecords for supplied user
	 */
	List<AttendanceRecord> getAttendanceRecordsForUser(String id, AttendanceSite aS);

	/**
	 * get the active statuses for the current site
	 *
	 * @return a List of Active AttendanceStatuses
     */
	List<AttendanceStatus> getActiveStatusesForCurrentSite();

	/**
	 * get the active statuses for a site
	 *
	 * @param attendanceSite, the AttendanceSite to get active statuses for
	 * @return a List Active of AttendanceStatuses
     */
	List<AttendanceStatus> getActiveStatusesForSite(AttendanceSite attendanceSite);

	/**
	 * Get all of the attendance statuses for a site
	 *
	 * @param attendanceSite, the AttendanceSite to get statuses for
	 * @return a List of AttendanceStatuses
     */
	List<AttendanceStatus> getAllStatusesForSite(AttendanceSite attendanceSite);

	/**
	 * Get an attendance status record by its id
	 *
	 * @param id, the id of the AttendanceStatus
	 * @return the AttendanceStatus
     */
	AttendanceStatus getAttendanceStatusById(Long id);

	/**
	 * Update an AttendanceRecord
	 *
	 * @param aR, the AttendanceRecord to update (must not be null)
	 * @return the success of the operation
     */
	boolean updateAttendanceRecord(AttendanceRecord aR) throws IllegalArgumentException;

	/**
	 * Update all AttendanceRecords for an AttendanceEvent
	 *
	 * @param aE, the AttendanceEvent to update
	 * @param s, the Status to set the AttendanceRecords (if null, uses the default of the AttendanceSite)
     */
	void updateAttendanceRecordsForEvent(AttendanceEvent aE, Status s);

	/**
	 * Update all AttendanceRecords where the user belongs to the supplied group for an AttendanceEvent
	 *
	 * @param aE, the AttendanceEvent
	 * @param s, the Status to use (if null, use the Site's default status)
	 * @param groupId, only update the AttendanceRecords for members of the groupID (if null or empty calls
	 *                 {@link org.sakaiproject.attendance.logic.AttendanceLogic#updateAttendanceRecordsForEvent(AttendanceEvent, Status)})
     */
	void updateAttendanceRecordsForEvent(AttendanceEvent aE, Status s, String groupId);

    /**
	 * Creates AttendanceRecords for an AttendanceEvent using a default Status for the provided Student IDs
	 *
     * @param attendanceEvent, the AttendanceEvent
     * @param defaultStatus, the status to use (if null use the site default)
     * @param missingStudentIds, a List of UserIDs which need records
     */
	void updateMissingRecordsForEvent(AttendanceEvent attendanceEvent, Status defaultStatus, List<String> missingStudentIds);

	/**
	 * Get statistics (total counts for each status) for an event
	 *
	 * @param event, the AttendanceEvent
	 * @return A Map with Status as the key and Integer (number of occurrences) as the value
	 */
	Map<Status, Integer> getStatsForEvent(AttendanceEvent event);

	/**
	 * Get statistics for user in current site
	 *
	 * @param userId, the user to get stats for
	 * @return Map, key: Status enum type. Value: number of occurrences
     */
	Map<Status, Integer> getStatsForUser(String userId);

	/**
	 * get statistics for user in site
	 *
	 * @param userId, the user to get stats for
	 * @param aS, the AttendanceSite to get the stats for
     * @return Map, key: Status enum type. Value: number of occurrences
     */
	Map<Status, Integer> getStatsForUser(String userId, AttendanceSite aS);

	/**
	 * get an AttendanceGrade by ID
	 *
	 * @param id, the ID (must not be null)
	 * @return the AttendanceGrade
     */
	AttendanceGrade getAttendanceGrade(Long id) throws IllegalArgumentException;

	/**
	 * Get's the AttendanceGrade for user in current site
	 *
	 * @param uID, the user's grade to retrieve
	 * @return the AttendanceGrade for a User
     */
	AttendanceGrade getAttendanceGrade(String uID) throws IllegalArgumentException;

	/**
	 * Returns a Map of AttendanceGrades for the current Site with the userId being the key.
	 *
	 * @return Map, key: userID. value: AttendanceGrade
     */
	Map<String, AttendanceGrade> getAttendanceGrades();

	/**
	 * Returns a Map of AttendanceGrades Scores for current site with userId being the key
	 *
	 * @return Map, key: String of useriD. value: String of score earned>
     */
	Map<String, String> getAttendanceGradeScores();

	/**
	 * Updates an AttendanceGrade
	 *
	 * @param aG, the AG to update (must not be null)
	 * @return the success of the operation
     */
	boolean updateAttendanceGrade(AttendanceGrade aG) throws IllegalArgumentException;
}
