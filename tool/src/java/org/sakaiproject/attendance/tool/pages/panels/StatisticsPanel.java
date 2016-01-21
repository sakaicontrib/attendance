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
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.Status;

import java.util.Map;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class StatisticsPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    private AttendanceEvent attendanceEvent;
    private String userId;

    public StatisticsPanel(String id, AttendanceEvent aE) {
        super(id);
        this.attendanceEvent = aE;
        init();
    }

    public StatisticsPanel(String id, String userID) {
        super(id);
        this.userId = userID;
        init();
    }

    private void init() {
        add(createTable());
    }

    private WebMarkupContainer createTable() {
        WebMarkupContainer infoContainer = new WebMarkupContainer("info-container");
        infoContainer.setOutputMarkupId(true);

        Map<Status, Integer> stats;

        if(attendanceEvent != null) {
            infoContainer.add(new Label("item-info-header", getString("attendance.event.view.item.info")));
            stats = attendanceLogic.getStatsForEvent(attendanceEvent);
        } else {
            infoContainer.add(new Label("item-info-header", getString("attendance.student.view.item.info")));
            stats = attendanceLogic.getStatsForUser(userId);
        }

        infoContainer.add(new Label("header-status-present", 		new ResourceModel("attendance.overview.header.status.present")));
        infoContainer.add(new Label("header-status-late", 		new ResourceModel("attendance.overview.header.status.late")));
        infoContainer.add(new Label("header-status-left-early", 	new ResourceModel("attendance.overview.header.status.left.early")));
        infoContainer.add(new Label("header-status-excused", 		new ResourceModel("attendance.overview.header.status.excused")));
        infoContainer.add(new Label("header-status-unexcused", 	new ResourceModel("attendance.overview.header.status.unexcused")));

        infoContainer.add(new Label("event-stats-present", stats.get(Status.PRESENT)));
        infoContainer.add(new Label("event-stats-late", stats.get(Status.LATE)));
        infoContainer.add(new Label("event-stats-left-early", stats.get(Status.LEFT_EARLY)));
        infoContainer.add(new Label("event-stats-excused", stats.get(Status.EXCUSED_ABSENCE)));
        infoContainer.add(new Label("event-stats-absent", stats.get(Status.UNEXCUSED_ABSENCE)));

        return infoContainer;
    }
}
