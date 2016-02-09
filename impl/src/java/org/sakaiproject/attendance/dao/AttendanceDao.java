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

package org.sakaiproject.attendance.dao;

import org.sakaiproject.attendance.model.*;

import java.io.Serializable;
import java.util.List;

/**
 * DAO interface for the Attendance Tool
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public interface AttendanceDao {
	/**
	 * Get an AttendanceSite
	 *
	 * @param siteId, the Sakai SiteID
	 * @return the AttendanceSite
     */
	AttendanceSite getAttendanceSite(String siteId);

	/**
	 * Add a new AttendanceSite record to the database
	 *
	 * @param as, the AttendanceSite to add
	 * @return success of the operation
     */
	boolean addAttendanceSite(AttendanceSite as);

	/**
	 * Updates an attendanceSite
	 *
	 * @param aS, the AttendanceSite with updated values
	 * @return success of the operation
     */
	boolean updateAttendanceSite(AttendanceSite aS);

	/**
	 * Gets a single AttendanceEvent from the db
	 * 
	 * @return the AttendanceEvent, may be null
	 */
	AttendanceEvent getAttendanceEvent(long id);

	/**
	 * get all the events by attendanceSite
	 *
	 * @param aS, the AttendanceSite
	 * @return a list of events or empty if no items.
     */
	List<AttendanceEvent> getAttendanceEventsForSite(AttendanceSite aS);

	/**
	 * Serializable function used to save an AttendanceEvent as part of the "Take Attendance Now" feature
	 *
	 * @param e, the AttendanceEvent
	 * @return the Long ID of the newly added AttendanceEvent
     */
	Serializable addAttendanceEventNow(AttendanceEvent e);

	/**
	 * Update an AttendanceEvent
	 *
	 * @param aE, the AttendanceEvent
	 * @return success of the operation
     */
	boolean updateAttendanceEvent(AttendanceEvent aE);

	/**
	 * Deletes an AttendanceEvent
	 *
	 * @param aE, the AttendanceEvent
	 * @return success of the operation
     */
	boolean deleteAttendanceEvent(AttendanceEvent aE);

	/**
	 * Get Status Record by ID
	 *
	 * @param id, the id of the status record
	 * @return the status record (may be null if DB operation fails)
     */
	AttendanceRecord getStatusRecord(long id);

	/**
	 * Add an AttendanceRecord
	 *
	 * @param aR, the AttendanceRecord to add
	 * @return true if success, false if not
     */
	boolean addAttendanceRecord(AttendanceRecord aR);

	/**
	 * Updates an AttendanceRecord
	 *
	 * @param aR, the AttendanceRecord to update with new values
	 * @return success of the operation
     */
	boolean updateAttendanceRecord(AttendanceRecord aR);

	/**
	 * Update a set of AttendanceRecords
	 *
	 * @param aRs, a List of AttendanceRecords
	 * @return success of the operation
     */
	void updateAttendanceRecords(List<AttendanceRecord> aRs);

	/**
	 * Update a set of AttendanceStatuses
	 *
	 * @param attendanceStatusList, a List of AttendanceStatuses
	 * @return success of the operation
     */
	void updateAttendanceStatuses(List<AttendanceStatus> attendanceStatusList);

	/**
	 * Get a list of the active statuses in an Attendance Site
	 *
	 * @param attendanceSite, the AttendanceSite
	 * @return List of Active AttendanceStatuses, null if DB issues
     */
	List<AttendanceStatus> getActiveStatusesForSite(AttendanceSite attendanceSite);

	/**
	 * Get a lit of all of the attendance statuses for a site
	 *
	 * @param attendanceSite, the AttendanceSite
	 * @return list of all AttendanceStatuses, null if DB issues
     */
	List<AttendanceStatus> getAllStatusesForSite(AttendanceSite attendanceSite);

	/**
	 * Get an attendance status record by its id
	 *
	 * @param id, the ID of the AttendanceStatus
	 * @return the AttendanceStatus (null if DB issues)
     */
	AttendanceStatus getAttendanceStatusById(Long id);

	/**
	 * Get AttendanceGrade by ID
	 *
	 * @param id, the AttendanceGrade ID
	 * @return the AttendanceGrade (null if DB issues)
     */
	AttendanceGrade getAttendanceGrade(Long id);

	/**
	 * Get an AttendanceGrade
	 *
	 * @param userID, the userID of owner of Grade
	 * @param aS, the site the Grade is present in
     * @return the AttendanceGrade, null if DB issues
     */
	AttendanceGrade getAttendanceGrade(String userID, AttendanceSite aS);

	/**
	 * Get a list of AttendanceGrades
	 *
	 * @param aS, the AttendanceSite to get the AGs for.
	 * @return List of AttendanceGrades, null if DB issues
     */
	List<AttendanceGrade> getAttendanceGrades(AttendanceSite aS);
	
	/**
	 * Add an AttendanceGrade to DB
	 *
	 * @param aG, AttendanceGrade to add
	 * @return success of operation
     */
	boolean addAttendanceGrade(AttendanceGrade aG);

	/**
	 * Updates an AttendanceGrade
	 *
	 * @param aG, the AG to update
	 * @return success of operation
     */
	boolean updateAttendanceGrade(AttendanceGrade aG);

	// Hibernate Query Constants
	String QUERY_GET_ATTENDANCE_EVENT = "getAttendanceEvent";
	String QUERY_GET_ATTENDANCE_EVENTS_FOR_SITE = "getAttendanceEventsForSite";
	String QUERY_GET_ATTENDANCE_EVENTS = "getAttendanceEvents";

	String QUERY_GET_SITE_BY_SITE_ID = "getSiteBySiteID";
	String QUERY_GET_SITE_BY_ID = "getSiteByID";

	String QUERY_GET_ATTENDANCE_RECORD = "getAttendanceRecord";
	String QUERY_GET_ATTENDANCE_RECORDS_FOR_ATTENDANCE_EVENT = "getRecordsForAttendanceEvent";

	String QUERY_GET_ATTENDANCE_STATUS = "getAttendanceStatus";
	String QUERY_GET_ACTIVE_ATTENDANCE_STATUSES_FOR_SITE = "getActiveAttendanceStatusesForSite";
	String QUERY_GET_ALL_ATTENDANCE_STATUSES_FOR_SITE = "getAllAttendanceStatusesForSite";

	String QUERY_GET_ATTENDANCE_GRADES_FOR_SITE = "getAttendanceGradesForSite";
	String QUERY_GET_ATTENDANCE_GRADE_BY_ID = "getAttendanceGradeByID";
	String QUERY_GET_ATTENDANCE_GRADE = "getAttendanceGrade";

	// Hibernate Object Fields
	String ID = "id";
	String USER_ID = "userID";
	String SITE_ID = "siteID";
	String ATTENDANCE_SITE = "attendanceSite";
	String ATTENDANCE_EVENT = "attendanceEvent";
}
