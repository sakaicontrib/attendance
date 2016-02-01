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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.*;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;
import org.sakaiproject.attendance.tool.pages.StudentView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class AttendanceRecordFormDataPanel extends BasePanel {
    private static final    long                        serialVersionUID = 1L;
    private                 IModel<AttendanceRecord>    recordIModel;
    private                 boolean                     isStudentView;
    private                 boolean                     restricted ;
    private                 List<Component>             ajaxTargets = new ArrayList<Component>();
    private                 String                      returnPage;
    private                 FeedbackPanel               pageFeedbackPanel;

    private                 WebMarkupContainer          commentContainer;
    private                 WebMarkupContainer          noComment;
    private                 WebMarkupContainer          yesComment;

    public AttendanceRecordFormDataPanel(String id, IModel<AttendanceRecord> aR, boolean iS, String rP, FeedbackPanel fP) {
        super(id, aR);
        this.recordIModel = aR;
        this.isStudentView = iS;
        this.restricted = this.role != null && this.role.equals("Student");
        this.returnPage = rP;
        this.pageFeedbackPanel = fP;
        this.ajaxTargets.add(this.pageFeedbackPanel);
        add(createRecordInputForm());
    }

    private Form<AttendanceRecord> createRecordInputForm() {
        Form<AttendanceRecord> recordForm = new Form<AttendanceRecord>("attendanceRecord", this.recordIModel) {
            protected void onSubmit() {
                AttendanceRecord aR = (AttendanceRecord) getDefaultModelObject();
                if(aR.getStatus() == null) {
                    aR.setStatus(Status.UNKNOWN);
                }
                boolean result = attendanceLogic.updateAttendanceRecord(aR);
                String[] resultMsgVars = new String[]{sakaiProxy.getUserSortName(aR.getUserID()), aR.getAttendanceEvent().getName(), getStatusString(aR.getStatus())};
                StringResourceModel temp;
                if(result){
                    temp = new StringResourceModel("attendance.record.save.success", null, resultMsgVars);
                    getSession().info(temp.getString());
                } else {
                    temp = new StringResourceModel("attendance.record.save.failure", null, resultMsgVars);
                    getSession().error(temp.getString());
                }
            }
        };

        createStatusRadio(recordForm);
        createCommentBox(recordForm);
        //createLabel(recordForm);

        return recordForm;
    }

    private void createLabel(Form<AttendanceRecord> rF) {
        WebMarkupContainer student = new WebMarkupContainer("student") {
            @Override
            public boolean isVisible(){
                return isStudentView;
            }
        };

        Label studentName = new Label("student-name");
        Link<Void> studentLink = new Link<Void>("student-link") {
            @Override
            public void onClick(){
                // do nothing
            }
        };

        if(isStudentView) {
            final String id = this.recordIModel.getObject().getUserID();
            String s = sakaiProxy.getUserSortName(id);
            studentName = new Label("student-name", s.equals("") ? new ResourceModel("attendance.student.name.unknown") : s);

            studentLink = new Link<Void>("student-link") {
                @Override
                public void onClick() {
                    setResponsePage(new StudentView(id, recordIModel.getObject().getAttendanceEvent().getId(), returnPage));
                }
            };


        }

        studentLink.add(studentName);
        student.add(studentLink);

        Label eventName = new Label("event-name", this.recordIModel.getObject().getAttendanceEvent().getName()){
            @Override
            public boolean isVisible(){
                return !isStudentView;
            }
        };


        rF.add(student);
        rF.add(eventName);
    }

    private void createStatusRadio(final Form<AttendanceRecord> rF) {

        AttendanceStatusProvider attendanceStatusProvider = new AttendanceStatusProvider(attendanceLogic.getCurrentAttendanceSite(), AttendanceStatusProvider.ACTIVE);
        DataView<AttendanceStatus> attendanceStatusRadios = new DataView<AttendanceStatus>("status-radios", attendanceStatusProvider) {
            @Override
            protected void populateItem(Item<AttendanceStatus> item) {
                Radio statusRadio = new Radio<Status>("record-status", new Model<Status>(item.getModelObject().getStatus()));
                item.add(statusRadio);
                statusRadio.add(new AjaxFormSubmitBehavior(rF, "onclick") {
                    protected void onSubmit(AjaxRequestTarget target) {
                        for (Component c : ajaxTargets) {
                            target.add(c);
                        }
                    }
                });
                ajaxTargets.add(statusRadio);
            }
        };

        RadioGroup group = new RadioGroup<Status>("attendance-record-status-group", new PropertyModel<Status>(this.recordIModel,"status"));
        group.setOutputMarkupPlaceholderTag(true);
        group.setRenderBodyOnly(false);
        group.add(attendanceStatusRadios);
        group.setEnabled(!this.restricted);

        rF.add(group);
    }

    private void createCommentBox(final Form<AttendanceRecord> rF) {

        commentContainer = new WebMarkupContainer("comment-container");
        commentContainer.setOutputMarkupId(true);

        noComment = new WebMarkupContainer("no-comment");
        noComment.setOutputMarkupId(true);

        yesComment = new WebMarkupContainer("yes-comment");
        yesComment.setOutputMarkupId(true);

        if(recordIModel.getObject().getComment() != null && !recordIModel.getObject().getComment().equals("")) {
            noComment.setVisible(false);
        } else {
            yesComment.setVisible(false);
        }

        commentContainer.add(noComment);
        commentContainer.add(yesComment);

        final TextArea<String> commentBox = new TextArea<String>("comment", new PropertyModel<String>(this.recordIModel, "comment"));

        final AjaxSubmitLink saveComment = new AjaxSubmitLink("save-comment") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                if(recordIModel.getObject().getComment() != null && !recordIModel.getObject().getComment().equals("")) {
                    noComment.setVisible(false);
                    yesComment.setVisible(true);
                } else {
                    noComment.setVisible(true);
                    yesComment.setVisible(false);
                }
                commentContainer.addOrReplace(noComment);
                commentContainer.addOrReplace(yesComment);
                for (Component c : ajaxTargets) {
                    target.add(c);
                }
            }
        };

        commentContainer.add(saveComment);
        commentContainer.add(commentBox);

        ajaxTargets.add(commentContainer);

        if(restricted) {
            commentContainer.setVisible(false);
        }

        rF.add(commentContainer);
    }
}
