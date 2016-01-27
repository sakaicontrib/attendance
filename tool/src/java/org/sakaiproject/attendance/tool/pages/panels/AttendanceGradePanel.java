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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.AttendanceSite;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class AttendanceGradePanel extends BasePanel {
    private static final    long                    serialVersionUID = 1L;
    private                 IModel<AttendanceGrade> agIModel;
    private                 FeedbackPanel           pageFeedbackPanel;
    private                 AttendanceSite          attendanceSite;

    public AttendanceGradePanel(String id, AttendanceGrade aG, FeedbackPanel fP) {
        super(id);
        this.agIModel = new CompoundPropertyModel<AttendanceGrade>(aG);
        this.attendanceSite = agIModel.getObject().getAttendanceSite();
        this.pageFeedbackPanel = fP;

        init();
    }

    private void init() {
        add(createGradeForm());
    }

    private Form<AttendanceGrade> createGradeForm() {
        Form<AttendanceGrade> gForm = new Form<AttendanceGrade>("attendance-grade", this.agIModel) {
            @Override
            public void onSubmit() {
                AttendanceGrade aG = (AttendanceGrade) getDefaultModelObject();

                boolean result = attendanceLogic.updateAttendanceGrade(aG);

                String displayName = sakaiProxy.getUserSortName(aG.getUserID());

                StringResourceModel temp;

                if (result) {
                    String grade = aG.getGrade() == null ? "null" : aG.getGrade().toString();
                    temp = new StringResourceModel("attendance.grade.update.success", null, new String[]{grade, displayName});
                    getSession().info(temp.getString());
                } else {
                    temp = new StringResourceModel("attendance.grade.update.failure", null, new String[]{displayName});
                    getSession().error(temp.getString());
                }

            }
        };

        final Double maximumGrade = this.attendanceSite.getMaximumGrade();

        NumberTextField<Double> points = new NumberTextField<Double>("grade") {
            @Override
            public boolean isEnabled(){
                return maximumGrade != null;
            }
        };
        points.setMinimum(0.0);
        points.setStep(0.1);

        points.add(new AjaxFormSubmitBehavior(gForm, "input") {
            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(pageFeedbackPanel);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if(target != null) {
                    target.add(pageFeedbackPanel);
                }
            }
        });

        Label maximum;

        if(maximumGrade == null) {
            maximum = new Label("maximum", "-");
            points.add(new AttributeModifier("title", new StringResourceModel("attendance.grade.tooltip.disabled", null, new String[]{getString("settings.link.label")})));
        } else {
            maximum = new Label("maximum", maximumGrade.toString());
            points.setMaximum(maximumGrade);
        }

        gForm.add(maximum);
        gForm.add(points);

        return gForm;
    }
}
