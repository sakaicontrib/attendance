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

import lombok.Setter;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import org.sakaiproject.attendance.dao.AttendanceDao;
import org.sakaiproject.attendance.model.Event;

/**
 * Implementation of {@link AttendanceLogic}
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class AttendanceLogicImpl implements AttendanceLogic {

	private static final Logger log = Logger.getLogger(AttendanceLogicImpl.class);

	
	/**
	 * {@inheritDoc}
	 */
	public Event getEvent(long id) {
		return dao.getEvent(id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<Event> getEvents() {
		return dao.getEvents();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean addEvent(Event t) {
		return dao.addEvent(t);
	}
	
	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		log.info("init");
	}
	
	@Setter
	private AttendanceDao dao;

}
