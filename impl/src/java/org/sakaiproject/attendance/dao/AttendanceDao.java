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

import org.sakaiproject.attendance.model.Event;
import org.sakaiproject.attendance.model.Reoccurrence;

/**
 * DAO interface for our project
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public interface AttendanceDao {

	/**
	 * Gets a single Event from the db
	 * 
	 * @return an item or null if no result
	 */
	Event getEvent(long id);
	
	/**
	 * Get all Events
	 * @return a list of items, an empty list if no items
	 */
	List<Event> getEvents();

	/**
	 * Get all Events for A site
	 * @return a list of items, an empty list if no items
	 */
	List<Event> getEventsForSite(String siteID);
		
	/**
	 * Add a new Event record to the database. Only the name property is actually used.
	 * @param t	, Event
	 * @return	true if success, false if not
	 */
	boolean addEvent(Event t);

	/**
	 * Add a list of events to the Database
	 * @param es, the ArrayList of events
	 * @return true if success, false if not
	 */
	boolean addEvents(ArrayList<Event> es);

	/**
	 * Add a Reoccurrence rule
	 * @param r, the object
	 * @return true if success, false if not
	 */
	boolean addReoccurrence(Reoccurrence r);

	// Hibernate Query Constants
	String QUERY_GET_EVENT = "getEvent";
	String QUERY_GET_EVENTS_FOR_SITE = "getEventsForSite";
	String QUERY_GET_EVENTS = "getEvents";

	// Hibernate Object Fields
	String ID = "id";
	String SITE_ID = "siteID";
}
