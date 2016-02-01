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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.Status;

import java.util.*;

public class AttendanceStatusFormPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private IModel<AttendanceSite> attendanceSiteIModel;
    private FeedbackPanel pageFeedbackPanel;

    public AttendanceStatusFormPanel(String id, FeedbackPanel feedbackPanel) {
        super(id);
        this.pageFeedbackPanel = feedbackPanel;
        this.attendanceSiteIModel = new Model<AttendanceSite>(attendanceLogic.getCurrentAttendanceSite());
        init();
    }

    public AttendanceStatusFormPanel(String id, IModel<AttendanceSite> attendanceSiteIModel, FeedbackPanel feedbackPanel) {
        super(id, attendanceSiteIModel);
        this.pageFeedbackPanel = feedbackPanel;
        this.attendanceSiteIModel = attendanceSiteIModel;
        init();
    }

    private void init() {

        Form<AttendanceSite> editStatusSettingsForm = new Form<AttendanceSite>("edit-status-settings-form", new CompoundPropertyModel<AttendanceSite>(this.attendanceSiteIModel)) {
            @Override
            protected void onSubmit() {
                AttendanceSite aS = (AttendanceSite) getDefaultModelObject();
                boolean result = attendanceLogic.updateAttendanceSite(aS);
                if(result){
                    getSession().info(getString("attendance.settings.edit.status.save.success"));
                } else {
                    getSession().error(getString("attendance.settings.edit.status.save.error"));
                }
            }
        };
        add(editStatusSettingsForm);

        final IModel<List<AttendanceStatus>> listModel = new PropertyModel<List<AttendanceStatus>>(this.attendanceSiteIModel, "attendanceStatuses") {
            @Override
            public List<AttendanceStatus> getObject() {
                List<AttendanceStatus> attendanceStatuses = new ArrayList((Set)super.getObject());
                Collections.sort(attendanceStatuses, new Comparator<AttendanceStatus>() {
                    @Override
                    public int compare(AttendanceStatus o1, AttendanceStatus o2) {
                        return o1.getSortOrder() - o2.getSortOrder();
                    }
                });
                return attendanceStatuses;
            }
        };
        editStatusSettingsForm.add(new ListView<AttendanceStatus>("all-statuses", listModel) {
            @Override
            protected void populateItem(ListItem<AttendanceStatus> item) {
                String statusName = getStatusString(item.getModelObject().getStatus());
                final CheckBox isActive = new CheckBox("is-active", new PropertyModel<Boolean>(item.getModelObject(), "isActive"));
                item.add(isActive);
                item.add(new Label("status", statusName));
                item.add(new TextField<Integer>("sort-order", new PropertyModel<Integer>(item.getModelObject(), "sortOrder")));
                if(item.getModelObject().getStatus() == Status.UNKNOWN) {
                    item.setVisible(false);
                }
            }
        });

        AjaxSubmitLink submit = new AjaxSubmitLink("submit-link") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                target.add(pageFeedbackPanel);
            }
        };

        editStatusSettingsForm.add(submit);

        editStatusSettingsForm.add(new CheckBox("show-comments-to-students", new PropertyModel<Boolean>(this.attendanceSiteIModel, "showCommentsToStudents")));

    }
}
