/*
 *  Copyright (c) 2016, The Apereo Foundation
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

package org.sakaiproject.attendance.impl;

import lombok.Setter;
import org.apache.log4j.Logger;
import org.sakaiproject.attendance.util.AttendanceConstants;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;

import java.util.Observable;
import java.util.Observer;

/**
 * Modeled after {@link org.sakaiproject.samigo.impl.SamigoObserver}
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class AttendanceObserver implements Observer {
    private static final Logger log = Logger.getLogger(AttendanceObserver.class);

    @Setter private EventTrackingService eventTrackingService;

    @Setter private AttendanceGradebookProviderImpl attendanceGradebookProvider;

    public void init() {
        log.info("init()");
        eventTrackingService.addLocalObserver(this);
    }

    public void destroy() {
        log.info("destroy");
        eventTrackingService.deleteObserver(this);
    }

    public void update(Observable arg0, Object arg) {
        if(!(arg instanceof Event)) {
            return;
        }

        Event event = (Event) arg;
        String eventType = event.getEvent();

        if(AttendanceConstants.EVENT_GRADE_SAVED.equals(eventType)) {
            log.debug("Attendance Grade Saved Event");
            //call the AttendanceGradeInterface Thingy
            attendanceGradebookProvider.sendToGradebook(new Long(event.getResource()));
        }
    }
}
