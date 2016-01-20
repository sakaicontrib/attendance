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


import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.StudentDataProvider;
import org.sakaiproject.user.api.User;

import java.util.Map;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class StudentOverview extends BasePage {
    private static final long serialVersionUID = 1L;

    public StudentOverview() {
        disableLink(this.studentOverviewLink);

        if(this.role != null && this.role.equals("Student")) {
            throw new RestartResponseException(StudentView.class);
        }

        add(createHeader());
        add(createStatsTable());
    }

    private WebMarkupContainer createHeader() {
        WebMarkupContainer  contain         = new WebMarkupContainer("student-overview-header");
        Label               title           = new Label("student-overview-title", new ResourceModel("attendance.student.overview.title"));
        Label               subtitle        = new Label("student-overview-subtitle", new ResourceModel("attendance.student.overview.subtitle"));

        contain.add(title);
        contain.add(subtitle);

        return contain;
    }

    private WebMarkupContainer createStatsTable() {
        WebMarkupContainer  statsTable      = new WebMarkupContainer("student-overview-stats-table");

        createStatsTableHeader(statsTable);
        createStatsTableData(statsTable);

        return statsTable;
    }

    private void createStatsTableHeader(WebMarkupContainer t) {
        //headers for the table
        Label               studentName     = new Label("header-student-name",       new ResourceModel("attendance.header.student"));
        Label               statusPresent 	= new Label("header-status-present", 		new ResourceModel("attendance.overview.header.status.present"));
        Label               statusLate      = new Label("header-status-late", 		new ResourceModel("attendance.overview.header.status.late"));
        Label               statusLeftEarly = new Label("header-status-left-early", 	new ResourceModel("attendance.overview.header.status.left.early"));
        Label               statusExcused   = new Label("header-status-excused", 		new ResourceModel("attendance.overview.header.status.excused"));
        Label               statusUnexcused = new Label("header-status-unexcused", 	new ResourceModel("attendance.overview.header.status.unexcused"));

        t.add(studentName);
        t.add(statusPresent);
        t.add(statusLate);
        t.add(statusLeftEarly);
        t.add(statusExcused);
        t.add(statusUnexcused);
    }

    private void createStatsTableData(WebMarkupContainer t) {
        StudentDataProvider     sDP         = new StudentDataProvider();
        final DataView<User>    uDataView   = new DataView<User>("students", sDP) {
            @Override
            protected void populateItem(Item<User> item) {
                final String id = item.getModelObject().getId();
                Map<Status, Integer> stats = attendanceLogic.getStatsForUser(id);
                Link<Void> studentLink = new Link<Void>("student-link") {
                    public void onClick() {
                        setResponsePage(new StudentView(id, BasePage.STUDENT_OVERVIEW_PAGE));
                    }
                };
                studentLink.add(new Label("student-name", item.getModelObject().getSortName()));
                studentLink.add(new Label("student-eid", "(" + item.getModelObject().getEid() + ")"));

                item.add(studentLink);
                item.add(new Label("student-stats-present", stats.get(Status.PRESENT)));
                item.add(new Label("student-stats-late", stats.get(Status.LATE)));
                item.add(new Label("student-stats-left-early", stats.get(Status.LEFT_EARLY)));
                item.add(new Label("student-stats-excused", stats.get(Status.EXCUSED_ABSENCE)));
                item.add(new Label("student-stats-absent", stats.get(Status.UNEXCUSED_ABSENCE)));
            }
        };

        Label noStudents = new Label("no-students", new ResourceModel("attendance.student.overview.no.students")) {
            @Override
            public boolean isVisible(){
                return uDataView.size() <= 0;
            }
        };
        Label noStudents2 = new Label("no-students2", new ResourceModel("attendance.student.overview.no.students.2")) {
            @Override
            public boolean isVisible(){
                return uDataView.size() <= 0;
            }
        };

        t.add(uDataView);
        t.add(noStudents);
        t.add(noStudents2);
    }
}
