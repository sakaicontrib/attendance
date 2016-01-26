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

package org.sakaiproject.attendance.tool.pages.panels;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;
import org.sakaiproject.attendance.tool.pages.EventView;
import org.sakaiproject.attendance.tool.pages.StudentView;

import java.util.Map;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class StatisticsPanel extends BasePanel {
    private static final    long                    serialVersionUID = 1L;

    private                 AttendanceEvent         attendanceEvent;
    private                 String                  userId;
    private                 String                  fromPage;
    private                 Long                    previousEventId;

    private                 Map<Status, Integer>    stats;

    public StatisticsPanel(String id, String fromPage, AttendanceEvent aE) {
        super(id);
        this.attendanceEvent = aE;
        init(fromPage);
    }

    public StatisticsPanel(String id, String fromPage, String userID, Long eId) {
        super(id);
        this.userId = userID;
        this.previousEventId = eId;
        init(fromPage);
    }

    private void init(String fromPage) {
        setOutputMarkupPlaceholderTag(true);
        this.fromPage = fromPage;
        add(createTable());
    }

    private WebMarkupContainer createTable() {
        WebMarkupContainer infoContainer = new WebMarkupContainer("info-container");
        infoContainer.add(createRefreshLink());
        infoContainer.setOutputMarkupId(true);

        if(attendanceEvent != null) {
            infoContainer.add(new Label("item-info-header", getString("attendance.event.view.item.info")));
            stats = attendanceLogic.getStatsForEvent(attendanceEvent);
        } else {
            infoContainer.add(new Label("item-info-header", getString("attendance.student.view.item.info")));
            stats = attendanceLogic.getStatsForUser(userId);
        }

        AttendanceStatusProvider attendanceStatusProvider = new AttendanceStatusProvider(attendanceLogic.getCurrentAttendanceSite(), AttendanceStatusProvider.ACTIVE);

        DataView<AttendanceStatus> statusHeaders = new DataView<AttendanceStatus>("status-headers", attendanceStatusProvider) {
            @Override
            protected void populateItem(Item<AttendanceStatus> item) {
                item.add(new Label("header-status-name", getStatusString(item.getModelObject().getStatus())));
            }
        };
        infoContainer.add(statusHeaders);

        DataView<AttendanceStatus> activeStatusStats = new DataView<AttendanceStatus>("active-status-stats", attendanceStatusProvider) {
            @Override
            protected void populateItem(Item<AttendanceStatus> item) {
                item.add(new Label("stats", stats.get(item.getModelObject().getStatus())));
            }
        };
        infoContainer.add(activeStatusStats);


        infoContainer.add(new Label("info", new ResourceModel("attendance.statistics.info")));

        return infoContainer;
    }

    private Link<Void> createRefreshLink() {
        Link<Void> refreshPage = new Link<Void>("refreshPage") {
            public void onClick() {
                if(attendanceEvent != null) {
                    setResponsePage(new EventView(attendanceEvent, fromPage));
                } else {
                    setResponsePage(new StudentView(userId, previousEventId,fromPage));
                }
            }
        };
        return refreshPage;
    }
}
