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
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.apache.wicket.model.*;
import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;
import org.sakaiproject.attendance.tool.dataproviders.StudentDataProvider;
import org.sakaiproject.attendance.tool.pages.panels.AttendanceGradePanel;
import org.sakaiproject.user.api.User;

import java.util.Map;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class StudentOverview extends BasePage {
    private static final long serialVersionUID = 1L;

    private AttendanceStatusProvider attendanceStatusProvider;

    public StudentOverview() {
        disableLink(this.studentOverviewLink);

        if(this.role != null && this.role.equals("Student")) {
            throw new RestartResponseException(StudentView.class);
        }

        this.attendanceStatusProvider = new AttendanceStatusProvider(attendanceLogic.getCurrentAttendanceSite(), AttendanceStatusProvider.ACTIVE);

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
        Label               grade           = new Label("header-grade",               new ResourceModel("attendance.header.grade"));

        DataView<AttendanceStatus> statusHeaders = new DataView<AttendanceStatus>("status-headers", attendanceStatusProvider) {
            @Override
            protected void populateItem(Item<AttendanceStatus> item) {
                item.add(new Label("header-status-name", getStatusString(item.getModelObject().getStatus())));
            }
        };

        Link<Void>          settings        = new Link<Void>("settings-link") {
            private static final long serialVersionUID = 1L;

            public void onClick() {
                setResponsePage(new SettingsPage());
            }
        };

        t.add(studentName);
        t.add(grade);
        t.add(statusHeaders);
        t.add(settings);
    }

    private void createStatsTableData(WebMarkupContainer t) {
        final Map<String, AttendanceGrade> gradeMap = attendanceLogic.getAttendanceGrades();
        StudentDataProvider     sDP         = new StudentDataProvider();
        final DataView<User>    uDataView   = new DataView<User>("students", sDP) {
            @Override
            protected void populateItem(Item<User> item) {
                final String id = item.getModelObject().getId();
                final Map<Status, Integer> stats = attendanceLogic.getStatsForUser(id);
                Link<Void> studentLink = new Link<Void>("student-link") {
                    public void onClick() {
                        setResponsePage(new StudentView(id, BasePage.STUDENT_OVERVIEW_PAGE));
                    }
                };
                studentLink.add(new Label("student-name", item.getModelObject().getSortName()));
                studentLink.add(new Label("student-eid", "(" + item.getModelObject().getEid() + ")"));

                item.add(studentLink);

                DataView<AttendanceStatus> activeStatusStats = new DataView<AttendanceStatus>("active-status-stats", attendanceStatusProvider) {
                    @Override
                    protected void populateItem(Item<AttendanceStatus> item) {
                        item.add(new Label("student-stats", stats.get(item.getModelObject().getStatus())));
                    }
                };
                item.add(activeStatusStats);
                item.add(new AttendanceGradePanel("attendance-grade", gradeMap.get(id), feedbackPanel));
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
