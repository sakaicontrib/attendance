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

package org.sakaiproject.attendance.tool.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceRecordProvider;
import org.sakaiproject.attendance.tool.pages.panels.AttendanceRecordFormPanel;
import org.sakaiproject.user.api.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class EventView extends BasePage {
    private static final    long                serialVersionUID = 1L;

    private                 Long                attendanceID;
    private                 AttendanceEvent     attendanceEvent;

    private                 String              returnPage;

    public EventView(Long id, String fromPage) {
        super();
        this.attendanceID = id;
        this.attendanceEvent = attendanceLogic.getAttendanceEvent(this.attendanceID);

        this.returnPage = fromPage;

        init();
    }

    public EventView(AttendanceEvent aE, String fromPage) {
        super();
        this.attendanceEvent = aE;

        this.returnPage = fromPage;

        init();
    }

    private void init() {
        createHeaderLinks();
        createTable();

        add(new Label("event-name", attendanceEvent.getName()));
    }

    private void createHeaderLinks() {
        Link<Void> editLink = new Link<Void>("edit-link") {
            @Override
            public void onClick() {
                setResponsePage(new AddEventPage(attendanceEvent));
            }
        };

        Link<Void> closeLink = new Link<Void>("close-link") {
            @Override
            public void onClick() {
                if(returnPage.equals(BasePage.ITEMS_PAGE)) {
                    setResponsePage(new AddEventPage());
                } else {
                    setResponsePage(new Overview());
                }
            }
        };
        if(returnPage.equals(BasePage.ITEMS_PAGE)) {
            closeLink.add(new Label("close-link-text", new ResourceModel("attendance.event.view.link.close.items")));
        } else {
            closeLink.add(new Label("close-link-text", new ResourceModel("attendance.event.view.link.close.overview")));
        }

        add(editLink);
        add(closeLink);
    }

    private void createTable() {
        Set<AttendanceRecord> records = this.attendanceEvent.getRecords();

        if(records == null || records.isEmpty()) {
            attendanceLogic.updateAttendanceRecordsForEvent(this.attendanceEvent, this.attendanceEvent.getAttendanceSite().getDefaultStatus());
            this.attendanceEvent = attendanceLogic.getAttendanceEvent(this.attendanceEvent.getId());
        }

        add(new DataView<AttendanceRecord>("records", new AttendanceRecordProvider(this.attendanceEvent)) {
            @Override
            protected void populateItem(final Item<AttendanceRecord> item) {
                //item.add(new AttendanceRecordFormPanel("student-record", item.getModel(), true));
                item.add(new AttendanceRecordFormPanel("record", item.getModel(), false));
            }
        });
    }
}
