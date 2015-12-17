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

//import com.google.ical.values.RRule;
//import de.scravy.pair.Pair;
import org.sakaiproject.attendance.model.Event;
import org.sakaiproject.attendance.model.Reoccurrence;

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
	 * @return true if success, false if not
	 */
	boolean addEvent(Event t);

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
	boolean addReoccurrence(Reoccurrence r);
}
