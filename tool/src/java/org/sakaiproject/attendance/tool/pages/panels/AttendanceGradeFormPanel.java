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
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.tool.pages.panels.util.GradebookItemNameValidator;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class AttendanceGradeFormPanel extends BasePanel {
    private static final    long            serialVersionUID = 1L;

    private                 FeedbackPanel   pageFeedbackPanel;
    private                 boolean         previousSendToGradebook;
    private                 String          previousName;
    private                 Double          previousMaxGrade;

    public AttendanceGradeFormPanel(String id, FeedbackPanel pg) {
        super(id);
        this.pageFeedbackPanel = pg;

        init();
    }

    private void init() {
        add(createSettingsForm());
    }

    private Form<AttendanceSite> createSettingsForm() {
        final AttendanceSite aS = attendanceLogic.getCurrentAttendanceSite();
        this.previousSendToGradebook = aS.getSendToGradebook() == null ? false : aS.getSendToGradebook();
        this.previousName = aS.getGradebookItemName();
        this.previousMaxGrade = aS.getMaximumGrade();

        Form<AttendanceSite> aSForm = new Form<AttendanceSite>("settings", new CompoundPropertyModel<AttendanceSite>(aS)) {
            @Override
            public void onSubmit() {
                AttendanceSite aS = (AttendanceSite) getDefaultModelObject();

                if(aS.getMaximumGrade() == null && previousMaxGrade != null) {
                    aS.setSendToGradebook(false);
                }

                boolean result = attendanceLogic.updateAttendanceSite(aS);

                if (result) {
                    if(aS.getSendToGradebook()){
                        if(previousSendToGradebook) { // if previously true, see if any relevant values have changed
                            if(!previousName.equals(aS.getGradebookItemName()) || !previousMaxGrade.equals(aS.getMaximumGrade())){
                                attendanceGradebookProvider.update(aS);
                            }

                            previousName = aS.getGradebookItemName();
                        } else {
                            attendanceGradebookProvider.create(aS);
                        }
                    } else {
                        if(previousSendToGradebook) {
                            attendanceGradebookProvider.remove(aS);
                        }
                    }

                    previousMaxGrade = aS.getMaximumGrade();
                    previousSendToGradebook = aS.getSendToGradebook();

                    getSession().info(getString("attendance.settings.grading.success"));
                } else {
                    getSession().error(getString("attendance.settings.grading.failure"));
                }

            }
        };

        final WebMarkupContainer grading = new WebMarkupContainer("grading") {
            @Override
            public boolean isVisible() {
                return !(aS.getMaximumGrade() == null);
            }
        };
        grading.setOutputMarkupPlaceholderTag(true);

        Label maxGradeLabel = new Label("maximum-grade-label", new ResourceModel("attendance.settings.grading.max.points.possible"));
        NumberTextField<Double> maximum = new NumberTextField<Double>("maximumGrade");
        maximum.setMinimum(0.1);
        maximum.setStep(0.1);
        maximum.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(grading);
            }
        });
        aSForm.add(maximum);
        aSForm.add(maxGradeLabel);



        Label isGradeShownLabel = new Label("is-grade-shown-label", new ResourceModel("attendance.settings.grading.is.grade.shown"));
        CheckBox isGradeShown = new CheckBox("isGradeShown");
        grading.add(isGradeShown);
        grading.add(isGradeShownLabel);

        final WebMarkupContainer gradebook = new WebMarkupContainer("gradebook") {
            @Override
            public boolean isVisible() {
                return aS.getSendToGradebook();
            }
        };
        gradebook.setOutputMarkupPlaceholderTag(true);
        Label gbItemName = new Label("gradebook-item-name", new ResourceModel("attendance.settings.grading.gradebook.item.name"));
        TextField<String> gradebookItemName = new TextField<String>("gradebookItemName");
        gradebookItemName.add(new GradebookItemNameValidator(aS, aS.getGradebookItemName()));
        gradebook.add(gbItemName);
        gradebook.add(gradebookItemName);
        grading.add(gradebook);

        Label sendToGBLabel = new Label("send-to-gradebook", new ResourceModel("attendance.settings.grading.send.to.gradebook"));
        final AjaxCheckBox sendToGradebook = new AjaxCheckBox("sendToGradebook") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(gradebook);
            }
        };
        grading.add(sendToGradebook);
        grading.add(sendToGBLabel);
        aSForm.add(grading);

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
