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

import java.util.List;
import java.util.Map;

//import com.google.ical.values.RRule;
//import de.scravy.pair.Pair;
import org.sakaiproject.attendance.model.*;
//import org.sakaiproject.attendance.model.Reoccurrence;

/**
 * An example logic interface
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public interface AttendanceLogic {

	/**
	 * Gets the Attendance site by Sakai Site ID
	 * @param siteID, sakai Site ID
	 * @return the Attendance Site
     */
	AttendanceSite getAttendanceSite(String siteID);

	/**
	 *
	 * @return The Current Attendance Site
     */
	AttendanceSite getCurrentAttendanceSite();

	/**
	 * Get a Event
	 * @return
	 */
	AttendanceEvent getAttendanceEvent(long id);
	
	/**
	 * Get all events (should probably never be used)
	 * @return
	 */
	List<AttendanceEvent> getAttendanceEvents();

	/**
	 * Gets all the evetns by Sakai Site ID
	 * @param siteID, the sakai siteID for a site
	 * @return a list of events, or empty
     */
	List<AttendanceEvent> getAttendanceEventsForSite(String siteID);

	/**
	 * gets all the events by (internal) Site id
	 * @param aS
	 * @return List of events, or empty
     */
	List<AttendanceEvent> getAttendanceEventsForSite(AttendanceSite aS);

	/**
	 * Get events for curenet site
	 * @return list of events
     */
	List<AttendanceEvent> getAttendanceEventsForCurrentSite();
	
	/**
	 * Add a new Event
	 * @param e	Event
	 * @return true if success, false if not
	 */
	boolean addAttendanceEvent(AttendanceEvent e);

	/**
	 * Updates an AttendanceEvent
	 * @param aE, the event to update
	 * @return
     */
	boolean updateAttendanceEvent(AttendanceEvent aE);

	/**
	 * Deletes an AttendanceEvent
	 * @param aE, the event to delete
	 * @return
     */
	boolean deleteAttendanceEvent(AttendanceEvent aE);

/*	/**
	 * Add a reoccurring Event
	 * @param t, the event model
	 * @param r, the RFC-2445 RRule
	 * @return true if success
	 *//*
	boolean addEvents(Event t, RRule r);

	/**
	 * Add a new Reoccurrence
	 * @param r, the Rrule for the object
	 * @return
	 *//*
	Pair<Boolean, Long> addReoccurrence(RRule r);*/

	/**
	 * Add Reoccurrence
	 * @param r, the Reocurrence
	 * @return true if success, false otherwise
	 */
//	boolean addReoccurrence(Reoccurrence r);

	/**
	 * Get's a AttendanceRecord
	 * @param id, the id of the attendanceRecord
	 * @return the attendanceRecord
     */
	AttendanceRecord getAttendanceRecord(Long id);

	/**
	 * get AttendanceRecords For a User in the current site
	 * @param id
	 * @return
     */
	List<AttendanceRecord> getAttendanceRecordsForUser(String id);

	/**
	 * get AttendanceRecords for User in AttendanceSite
	 * @param id
	 * @param aS
	 * @return
	 */
	List<AttendanceRecord> getAttendanceRecordsForUser(String id, AttendanceSite aS);

	/**
	 * get the active statuses for the current site
	 * @return
     */
	List<AttendanceStatus> getActiveStatusesForCurrentSite();

	/**
	 * get the active statuses for a site
	 * @param siteId
	 * @return
     */
	List<AttendanceStatus> getActiveStatusesForSite(String siteId);

	/**
	 * get the active statuses for a site
	 * @param attendanceSite
	 * @return
     */
	List<AttendanceStatus> getActiveStatusesForSite(AttendanceSite attendanceSite);

	/**
	 * Update an AttendanceRecord
	 * @param aR, the AttendanceRecord to update
	 * @return
     */
	boolean updateAttendanceRecord(AttendanceRecord aR);

	/**
	 * update attendance records
	 * @param aRs, a collection of AttendanceRecords
	 * @return
     */
	boolean updateAttendanceRecords(List<AttendanceRecord> aRs);

	/**
	 * Update all AttendanceRecords for an AttendanceEvent
	 * @param aE, the AttendanceEvent to update
	 * @param s, the Status to set the Records too
     * @return
     */
	boolean updateAttendanceRecordsForEvent(AttendanceEvent aE, Status s);

    /**
     *
     * @param attendanceEvent
     * @param defaultStatus
     * @param missingStudentIds
     * @return
     */
	boolean updateMissingRecordsForEvent(AttendanceEvent attendanceEvent, Status defaultStatus, List<String> missingStudentIds);

	/**
	 * Get statistics (total counts for each status) for an event
	 * @param event, the AttendanceEvent
	 * @return A Map with Status as the key and Integer (number of occurrences) as the value
	 */
	Map<Status, Integer> getStatsForEvent(AttendanceEvent event);

	/**
	 * Get statistics for user in current site
	 * @param userId
	 * @return
     */
	Map<Status, Integer> getStatsForUser(String userId);

	/**
	 * get statistics for user in site
	 * @param userId, the user to get stats for
	 * @param aS, the AttendanceSite to get the stats for
     * @return
     */
	Map<Status, Integer> getStatsForUser(String userId, AttendanceSite aS);
}
