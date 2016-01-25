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

package org.sakaiproject.attendance.dao;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.AttendanceStatus;
//import org.sakaiproject.attendance.model.Reoccurrence;

/**
 * DAO interface for our project
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public interface AttendanceDao {
	/**
	 * Get an attendance site from the DB
	 */
	AttendanceSite getAttendanceSite(String siteId);

	/**
	 * Add a new AttendanceSite record to the database
	 * @param as
	 * @return
     */
	boolean addAttendanceSite(AttendanceSite as);

	/**
	 * Gets a single Event from the db
	 * 
	 * @return an item or null if no result
	 */
	AttendanceEvent getAttendanceEvent(long id);
	
	/**
	 * Get all Events
	 * @return a list of items, an empty list if no items
	 */
	List<AttendanceEvent> getAttendanceEvents();

	/**
	 * Get all Events for A site
	 * @return a list of items, an empty list if no items
	 */
	List<AttendanceEvent> getAttendanceEventsForSite(String siteID);

	/**
	 * get all the events by attendanceSite
	 * @param aS, the AttendanceSite
	 * @return a list of events or empty if no items.
     */
	List<AttendanceEvent> getAttendanceEventsForSite(AttendanceSite aS);
		
	/**
	 * Add a new Event record to the database. Only the name property is actually used.
	 * @param t	, Event
	 * @return	true if success, false if not
	 */
	boolean addAttendanceEvent(AttendanceEvent t);

	/**
	 * Update an AttendanceEvent
	 * @param aE, the AttendanceEvent
	 * @return
     */
	boolean updateAttendanceEvent(AttendanceEvent aE);

	/**
	 * Deletes an AttendanceEvent
	 * @param aE, the AttendanceEvent
	 * @return
     */
	boolean deleteAttendanceEvent(AttendanceEvent aE);

	/**
	 * Add a list of events to the Database
	 * @param es, the ArrayList of events
	 * @return true if success, false if not
	 */
	boolean addAttendanceEvents(ArrayList<AttendanceEvent> es);

	/**
	 * Add a Reoccurrence rule
	 * @param r, the object
	 * @return true if success, false if not
	 */
//	boolean addReoccurrence(Reoccurrence r);

	/**
	 * Get Status Record by ID
	 * @param id, the id of the status record
	 * @return the status record
     */
	AttendanceRecord getStatusRecord(long id);

	/**
	 * Add an AttendanceRecord
	 * @param aR, the AttendanceRecord to add
	 * @return true if success, false if not
     */
	boolean addAttendanceRecord(AttendanceRecord aR);

	/**
	 * Updates an AttendanceRecord
	 * @param aR, the AttendanceRecord to update with new values
	 * @return
     */
	boolean updateAttendanceRecord(AttendanceRecord aR);

	/**
	 * Update a set of AttendanceRecords
	 * @param aRs, a List of AttendanceRecords
	 * @return
     */
	boolean updateAttendanceRecords(List<AttendanceRecord> aRs);

	/**
	 * Update a set of AttendanceStatuses
	 * @param attendanceStatusList, a List of AttendanceStatuses
	 * @return
     */
	boolean updateAttendanceStatuses(List<AttendanceStatus> attendanceStatusList);

	/**
	 * Get a list of the active statuses in an Attendance Site
	 * @param attendanceSite
	 * @return
     */
	List<AttendanceStatus> getActiveStatusesForSite(AttendanceSite attendanceSite);

	/**
	 * Get a lit of all of the attendance statuses for a site
	 * @param attendanceSite
	 * @return
     */
	List<AttendanceStatus> getAllStatusesForSite(AttendanceSite attendanceSite);

	/**
	 * Get an attendance status record by its id
	 * @param id
	 * @return
     */
	AttendanceStatus getAttendanceStatusById(Long id);

	/**
	 * Get a list of AttendanceGrades
	 * @param aS, the AttendanceSite to get the AGs for.
	 * @return
     */
	List<AttendanceGrade> getAttendanceGrades(AttendanceSite aS);


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

	// Hibernate Object Fields
	String ID = "id";
	String SITE_ID = "siteID";
	String ATTENDANCE_SITE = "attendanceSite";
	String ATTENDANCE_EVENT = "attendanceEvent";
}
