/*
 *  Copyright (c) 2017, University of Dayton
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

import lombok.Getter;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceRecordProvider;
import org.sakaiproject.attendance.tool.models.ProfileImage;
import org.sakaiproject.attendance.tool.panels.AttendanceRecordFormDataPanel;
import org.sakaiproject.attendance.tool.panels.AttendanceRecordFormHeaderPanel;
import org.sakaiproject.attendance.tool.panels.PrintPanel;
import org.sakaiproject.attendance.tool.panels.StatisticsPanel;

import java.util.*;

/**
 * EventView is a view into an AttendanceEvent
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class EventView extends BasePage {
    private static final    long                serialVersionUID = 1L;

    private                 Long                attendanceID;
    private                 AttendanceEvent     attendanceEvent;

    @Getter
    private                 String                  returnPage;

    private                 DropDownChoice<Status>  setAllStatus;
    private                 DropDownChoice<String>  groupChoice;

    private                 String                  selectedGroup;

                            PrintPanel              printPanel;
                            WebMarkupContainer      printContainer;

    public EventView(Long id, String fromPage) {
        super();
        this.attendanceID = id;
        this.attendanceEvent = attendanceLogic.getAttendanceEvent(this.attendanceID);

        this.returnPage = fromPage;

        init();
    }

    public EventView(Long id, String fromPage, String selectedGroup) {
        super();

        this.attendanceID = id;
        this.attendanceEvent = attendanceLogic.getAttendanceEvent(this.attendanceID);
        this.returnPage = fromPage;
        this.selectedGroup = selectedGroup;

        init();
    }

    public EventView(AttendanceEvent aE, String fromPage) {
        super();
        this.attendanceEvent = aE;

        this.returnPage = fromPage;

        init();
    }

    public EventView(AttendanceEvent aE, String fromPage, String selectedGroup) {
        super();
        this.attendanceEvent = aE;

        this.returnPage = fromPage;

        this.selectedGroup = selectedGroup;

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
                attendanceLogic.updateAttendanceRecordsForEvent(attendanceEvent, setAllStatus.getModelObject(), selectedGroup);
                String who = selectedGroup == null?"":" for " + sakaiProxy.getGroupTitleForCurrentSite(selectedGroup);
                getSession().info("All attendance records " + who + " for " + attendanceEvent.getName() + " set to " + setAllStatus.getModelObject());
                setResponsePage(new EventView(attendanceEvent.getId(), returnPage, selectedGroup));
            }

            @Override
            public boolean isEnabled() {
                return !attendanceEvent.getAttendanceSite().getIsSyncing();
            }
        };

        List<AttendanceStatus> activeAttendanceStatuses = attendanceLogic.getActiveStatusesForCurrentSite();
        Collections.sort(activeAttendanceStatuses, new Comparator<AttendanceStatus>() {
            @Override
            public int compare(AttendanceStatus o1, AttendanceStatus o2) {
                return o1.getSortOrder() - o2.getSortOrder();
            }
        });
        List<Status> activeStatuses = new ArrayList<>();
        for(AttendanceStatus attendanceStatus : activeAttendanceStatuses) {
            activeStatuses.add(attendanceStatus.getStatus());
        }

        setAllForm.add(setAllStatus = new DropDownChoice<>("set-all-status", new Model<>(), activeStatuses, new EnumChoiceRenderer<>(this)));
        setAllStatus.add(new AjaxFormSubmitBehavior("onchange") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                super.onSubmit(target);
            }
        });
        add(setAllForm);

        this.printContainer = new WebMarkupContainer("print-container");
        printContainer.setOutputMarkupId(true);

        this.printPanel = new PrintPanel("print-panel", new Model<>(attendanceEvent));

        printContainer.add(printPanel);

        printContainer.add(AttributeModifier.append("class", "printHidden"));

        add(printContainer);
    }

    private void createStatsTable() {
        StatisticsPanel infoContainer = new StatisticsPanel("statistics", returnPage, attendanceEvent);

        add(infoContainer);
    }

    private void createHeader() {
        Link<Void> closeLink = new Link<Void>("close-link") {
            @Override
            public void onClick() {
                setResponsePage(new Overview());
            }
        };

         closeLink.add(new Label("close-link-text", new ResourceModel("attendance.event.view.link.close.overview")));

        add(getAddEditWindowAjaxLink(attendanceEvent, "edit-link"));
        add(closeLink);
    }

    private void createTable() {
        Set<AttendanceRecord> records = this.attendanceEvent.getRecords();

        add(new Label("student-name", new ResourceModel("attendance.event.view.student.name")));

        add(new Label("student-photo", new ResourceModel("attendance.event.view.student.photo")));

        add(new AttendanceRecordFormHeaderPanel("record-header"));

        // Generate records if none exist
        if(records == null || records.isEmpty()) {
            List<AttendanceRecord> recordList = attendanceLogic.updateAttendanceRecordsForEvent(this.attendanceEvent, this.attendanceEvent.getAttendanceSite().getDefaultStatus());
            records = new HashSet<>(recordList);
        } else {
            // Generate records for added students
            List<String> currentStudentIds = sakaiProxy.getCurrentSiteMembershipIds();
            for(AttendanceRecord record : records) {
                currentStudentIds.remove(record.getUserID());
            }
            List<AttendanceRecord> recordList = attendanceLogic.updateMissingRecordsForEvent(this.attendanceEvent, this.attendanceEvent.getAttendanceSite().getDefaultStatus(), currentStudentIds);
            records.addAll(recordList);
        }
        this.attendanceEvent.setRecords(records);

        // Add form to filter table
        final Form<?> filterForm = new Form<Void>("filter-table-form"){
            @Override
            protected void onSubmit() {
                setResponsePage(new EventView(attendanceEvent, returnPage, groupChoice.getModelObject()));
            }
        };

        add(filterForm);

        List<String> groupIds = sakaiProxy.getAvailableGroupsForCurrentSite();
        Collections.sort(groupIds, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return sakaiProxy.getGroupTitleForCurrentSite(o1).compareTo(sakaiProxy.getGroupTitleForCurrentSite(o2));
            }
        });
        groupChoice = new DropDownChoice<>("group-choice", new PropertyModel<>(this, "selectedGroup"), groupIds, new IChoiceRenderer<String>() {
            @Override
            public Object getDisplayValue(String s) {
                return sakaiProxy.getGroupTitleForCurrentSite(s);
            }

            @Override
            public String getIdValue(String s, int i) {
                return s;
            }
        });
        groupChoice.setNullValid(true);

        groupChoice.add(new AjaxFormSubmitBehavior("onchange") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                super.onSubmit(target);
            }
        });
        filterForm.add(groupChoice);
        filterForm.add(new Label("group-choice-label", new ResourceModel("attendance.event.view.filter")));

        add(new DataView<AttendanceRecord>("records", new AttendanceRecordProvider(this.attendanceEvent, selectedGroup)) {
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
                ProfileImage profilePhoto = new ProfileImage("stu-photo", new Model<String>(String.format("/direct/profile/%s/image/official", stuId)));
                item.add(profilePhoto);
                item.add(new AttendanceRecordFormDataPanel("record", item.getModel(), returnPage, feedbackPanel));
            }
        });
    }
}
