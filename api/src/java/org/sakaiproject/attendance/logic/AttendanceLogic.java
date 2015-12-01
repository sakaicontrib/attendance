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

import org.sakaiproject.attendance.model.Event;

/**
 * An example logic interface
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public interface AttendanceLogic {

	/**
	 * Get a Event
	 * @return
	 */
	Event getEvent(long id);
	
	/**
	 * Get all Things
	 * @return
	 */
	List<Event> getEvents();
	
	/**
	 * Add a new Event
	 * @param t	Event
	 * @return boolean if success, false if not
	 */
	boolean addEvent(Event t);
}
