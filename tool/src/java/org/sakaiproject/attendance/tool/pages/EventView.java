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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceRecordProvider;
import org.sakaiproject.attendance.tool.pages.panels.AttendanceRecordFormDataPanel;
import org.sakaiproject.attendance.tool.pages.panels.AttendanceRecordFormHeaderPanel;
import org.sakaiproject.attendance.tool.pages.panels.PrintPanel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class EventView extends BasePage {
    private static final    long                serialVersionUID = 1L;

    private                 Long                attendanceID;
    private                 AttendanceEvent     attendanceEvent;

    private                 String                  returnPage;

    private                 DropDownChoice<Status>  setAllStatus;

                            PrintPanel              printPanel;
                            WebMarkupContainer      printContainer;

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
        createHeader();
        createTable();

        createStatsTable();

        add(new Label("event-name", attendanceEvent.getName()));
        add(new Label("event-date", attendanceEvent.getStartDateTime()));
        add(new Label("take-attendance-header", getString("attendance.event.view.take.attendance")));

        final Form<?> setAllForm = new Form<Void>("set-all-form"){
            @Override
            protected void onSubmit() {
                if(attendanceLogic.updateAttendanceRecordsForEvent(attendanceEvent, setAllStatus.getModelObject())){
                    getSession().info("All attendance records for " + attendanceEvent.getName() + " set to " + setAllStatus.getModelObject());
                    setResponsePage(new EventView(attendanceEvent, returnPage));
                }
            }
        };
        setAllForm.add(setAllStatus = new DropDownChoice<Status>("set-all-status", new Model<Status>(), Arrays.asList(Status.values()), new EnumChoiceRenderer<Status>(this)));
        setAllStatus.add(new AjaxFormSubmitBehavior("onchange") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                super.onSubmit(target);
            }
        });
        add(setAllForm);

        this.printContainer = new WebMarkupContainer("print-container");
        printContainer.setOutputMarkupId(true);

        this.printPanel = new PrintPanel("print-panel", new Model<AttendanceEvent>(attendanceEvent));

        printContainer.add(printPanel);

        add(printContainer);
    }

    private void createStatsTable() {
        WebMarkupContainer infoContainer = new WebMarkupContainer("info-container");
        infoContainer.setOutputMarkupId(true);

        infoContainer.add(new Label("item-info-header", getString("attendance.event.view.item.info")));

        Map<Status, Integer> stats = attendanceLogic.getStatsForEvent(attendanceEvent);

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

        add(infoContainer);
    }

    private void createHeader() {
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

        add(new Label("student-name", new ResourceModel("attendance.event.view.student.name")));

        add(new AttendanceRecordFormHeaderPanel("record-header"));

        // Generate records if none exist
        if(records == null || records.isEmpty()) {
            attendanceLogic.updateAttendanceRecordsForEvent(this.attendanceEvent, this.attendanceEvent.getAttendanceSite().getDefaultStatus());
            this.attendanceEvent = attendanceLogic.getAttendanceEvent(this.attendanceEvent.getId());
        } else {
            // Generate records for added students
            List<String> currentStudentIds = sakaiProxy.getCurrentSiteMembershipIds();
            for(AttendanceRecord record : records) {
                currentStudentIds.remove(record.getUserID());
            }
            attendanceLogic.updateMissingRecordsForEvent(this.attendanceEvent, this.attendanceEvent.getAttendanceSite().getDefaultStatus(), currentStudentIds);
            this.attendanceEvent = attendanceLogic.getAttendanceEvent(this.attendanceEvent.getId());
        }

        add(new DataView<AttendanceRecord>("records", new AttendanceRecordProvider(this.attendanceEvent)) {
            @Override
            protected void populateItem(final Item<AttendanceRecord> item) {
                final String stuId = item.getModelObject().getUserID();
                final String sortName = sakaiProxy.getUserSortName(stuId);
                final String displayId = sakaiProxy.getUserDisplayId(stuId);
                Label stuName = new Label("stu-name", sortName + " (" + displayId + ")");

                Link<Void> studentLink = new Link<Void>("stu-link") {
                    @Override
                    public void onClick() {
                        setResponsePage(new StudentView(stuId, item.getModelObject().getAttendanceEvent().getId(), returnPage));
                    }
                };
                studentLink.add(stuName);
                item.add(studentLink);
                item.add(new AttendanceRecordFormDataPanel("record", item.getModel(), true, returnPage, feedbackPanel));
            }
        });
    }
}
