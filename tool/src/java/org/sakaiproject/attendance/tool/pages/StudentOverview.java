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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.apache.wicket.model.*;
import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;
import org.sakaiproject.attendance.tool.dataproviders.StudentDataProvider;
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

        t.add(studentName);
        t.add(grade);
        t.add(statusHeaders);
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
                item.add(createGradeForm(new CompoundPropertyModel<AttendanceGrade>(gradeMap.get(id))));
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

    private Form<AttendanceGrade> createGradeForm(IModel<AttendanceGrade> aGModel) {
        Form<AttendanceGrade> gForm = new Form<AttendanceGrade>("attendance-grade", aGModel) {
            @Override
            public void onSubmit() {
                AttendanceGrade aG = (AttendanceGrade) getDefaultModelObject();
                if(aG.getGrade() != null) {
                    boolean result = attendanceLogic.updateAttendanceGrade(aG);

                    String displayName = sakaiProxy.getUserSortName(aG.getUserID());

                    StringResourceModel temp;

                    if (result) {
                        temp = new StringResourceModel("attendance.grade.update.success", null, new String[]{aG.getGrade().toString(), displayName});
                        getSession().info(temp.getString());
                    } else {
                        temp = new StringResourceModel("attendance.grade.update.failure", null, new String[]{displayName});
                        getSession().error(temp.getString());
                    }
                }
            }
        };

        NumberTextField<Double> points = new NumberTextField<Double>("grade");
        points.setMinimum(0.0);
        points.add(new AjaxFormSubmitBehavior(gForm, "input") {
            protected void onSubmit(AjaxRequestTarget target) {
                if(target != null) {
                    target.add(feedbackPanel);
                }
            }
        });

        gForm.add(points);

        return gForm;
    }
}
