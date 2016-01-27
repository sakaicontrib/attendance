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
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.attendance.model.AttendanceSite;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class AttendanceGradeFormPanel extends BasePanel {
    private static final    long            serialVersionUID = 1L;

    private                 FeedbackPanel   pageFeedbackPanel;

    public AttendanceGradeFormPanel(String id, FeedbackPanel pg) {
        super(id);
        this.pageFeedbackPanel = pg;

        init();
    }

    private void init() {
        add(createSettingsForm());
    }

    private Form<AttendanceSite> createSettingsForm() {
        Form<AttendanceSite> aSForm = new Form<AttendanceSite>("settings", new CompoundPropertyModel<AttendanceSite>(attendanceLogic.getCurrentAttendanceSite())) {
            @Override
            public void onSubmit() {
                AttendanceSite aS = (AttendanceSite) getDefaultModelObject();

                boolean result = attendanceLogic.updateAttendanceSite(aS);

                if (result) {
                    getSession().info(getString("attendance.settings.grading.success"));
                } else {
                    getSession().error(getString("attendance.settings.grading.failure"));
                }

            }
        };

        Label maxGradeLabel = new Label("maximum-grade-label", new ResourceModel("attendance.settings.grading.max.points.possible"));
        NumberTextField<Double> maximum = new NumberTextField<Double>("maximumGrade");
        maximum.setMinimum(0.1);
        maximum.setStep(0.1);
        aSForm.add(maximum);
        aSForm.add(maxGradeLabel);

        Label isGradeShownLabel = new Label("is-grade-shown-label", new ResourceModel("attendance.settings.grading.is.grade.shown"));
        CheckBox isGradeShown = new CheckBox("isGradeShown");
        aSForm.add(isGradeShown);
        aSForm.add(isGradeShownLabel);

        AjaxSubmitLink submit = new AjaxSubmitLink("submit") {
            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(pageFeedbackPanel);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                target.add(pageFeedbackPanel);
            }
        };
        submit.add(new AttributeModifier("value", new ResourceModel("attendance.settings.grading.save")));
        aSForm.add(submit);

        return aSForm;
    }
}
