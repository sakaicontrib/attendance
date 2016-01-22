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
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;

import java.util.ArrayList;
import java.util.List;

public class AttendanceStatusFormPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private IModel<AttendanceSite> attendanceSiteIModel;

    public AttendanceStatusFormPanel(String id) {
        super(id);
        this.attendanceSiteIModel = new Model<AttendanceSite>(attendanceLogic.getCurrentAttendanceSite());
        init();
    }

    public AttendanceStatusFormPanel(String id, IModel<AttendanceSite> attendanceSiteIModel) {
        super(id, attendanceSiteIModel);
        this.attendanceSiteIModel = attendanceSiteIModel;
        init();
    }

    private void init() {

        Form<AttendanceSite> editStatusSettingsForm = new Form<AttendanceSite>("edit-status-settings-form", this.attendanceSiteIModel) {
            @Override
            protected void onSubmit() {
                AttendanceSite aS = (AttendanceSite) getDefaultModelObject();
                List<AttendanceStatus> attendanceStatuses = new ArrayList<AttendanceStatus>(aS.getAttendanceStatuses());
                boolean result = attendanceLogic.updateAttendanceStatuses(attendanceStatuses);
                if(result){
                    getSession().info("Success");
                } else {
                    getSession().error("Failure");
                }
            }
        };
        add(editStatusSettingsForm);

        AttendanceStatusProvider attendanceStatusProvider = new AttendanceStatusProvider(attendanceSiteIModel.getObject(), AttendanceStatusProvider.DISPLAY);
        DataView<AttendanceStatus> attendanceStatusDataView = new DataView<AttendanceStatus>("all-statuses", attendanceStatusProvider) {
            @Override
            protected void populateItem(Item<AttendanceStatus> item) {
                String statusName = attendanceLogic.getStatusString(item.getModelObject().getStatus());
                final CheckBox isActive = new CheckBox("is-active", new PropertyModel<Boolean>(item.getModelObject(), "isActive"));
                item.add(isActive);
                item.add(new Label("status", statusName));
                item.add(new Label("sort-order", item.getModelObject().getSortOrder()));
            }
        };

        editStatusSettingsForm.add(attendanceStatusDataView);

        AjaxSubmitLink submit = new AjaxSubmitLink("submit-link") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
            }
        };

        editStatusSettingsForm.add(submit);

    }
}
