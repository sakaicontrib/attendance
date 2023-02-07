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

package org.sakaiproject.attendance.tool.panels;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.*;
import org.sakaiproject.attendance.api.model.AttendanceEvent;
import org.sakaiproject.attendance.tool.pages.EventView;
import org.sakaiproject.attendance.tool.pages.Overview;
import org.sakaiproject.attendance.tool.util.AttendanceFeedbackPanel;
import org.sakaiproject.attendance.tool.util.PlaceholderBehavior;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.wicket.component.SakaiDateTimeField;

import java.time.ZoneId;
import java.time.ZonedDateTime;


/**
 * EventInputPanel is used to get AttendanceEvent settings for a new or existing AttendanceEvent
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class EventInputPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    private TextField<String> eventNameField;
    private SakaiDateTimeField startDateTimeField;

    private ZonedDateTime startDateTime;
    private AttendanceEvent attendanceEvent;

    private ModalWindow window;

    private boolean isEditing;
    private boolean recursiveAddAnother;

    public EventInputPanel(String id, final ModalWindow window, final IModel<Long> eventIdModel) {
        this(id, window, eventIdModel, false);
    }

    public EventInputPanel(String id, final ModalWindow window, final IModel<Long> eventIdModel, boolean recursive) {
        super(id, eventIdModel);
        this.window = window;
        this.recursiveAddAnother = recursive;

        if(eventIdModel == null) {
            this.isEditing = false;
            this.attendanceEvent = new AttendanceEvent();
        } else {
            this.isEditing = true;
            this.attendanceEvent = attendanceLogic.getAttendanceEvent(eventIdModel.getObject());
            this.startDateTime = this.attendanceEvent.getStartDateTime() == null ? null : ZonedDateTime.ofInstant(this.attendanceEvent.getStartDateTime(), ZoneId.systemDefault());
        }

        add(createEventInputForm());
    }

    private Form<?> createEventInputForm() {
        Form<?> eventForm = new Form<Void>("event");

        eventForm.add(new AttendanceFeedbackPanel("addEditItemFeedback"));

        AjaxSubmitLink submit = createSubmitLink("submit", eventForm, false);

        submit.add(new Label("submitLabel", getSubmitLabel()));
        eventForm.add(submit);

        eventForm.add(createSubmitLink("submitAndAddAnother", eventForm, true));

        final AjaxButton cancel = new AjaxButton("cancel") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                window.close(target);

                if(recursiveAddAnother) { // assumes will only occur from Overview page
                    setResponsePage(getPage().getPageClass());
                }
            }
        };
        cancel.setDefaultFormProcessing(false);
        eventForm.add(cancel);
        createValues(eventForm);

        return eventForm;
    }

    public ZonedDateTime getStartDateTime() {
        return this.startDateTime;
    }

    public void setStartDateTime(ZonedDateTime zoned)	{
        this.startDateTime = zoned;
    }

    private void processSave(AjaxRequestTarget target, Form<?> form, boolean addAnother) {
        try {
            AttendanceEvent saveEvent = attendanceLogic.getAttendanceEvent(this.attendanceEvent.getId());

            saveEvent.setName(eventNameField.getModelObject());

            ZonedDateTime startDate = startDateTimeField.getModelObject();
            saveEvent.setStartDateTime(startDate == null ? null : startDate.toInstant());

            saveEvent = attendanceLogic.updateAttendanceEvent(saveEvent);

            StringResourceModel temp = new StringResourceModel("attendance.add.success", null, (Object[]) new String[]{saveEvent.getName()});
            getSession().success(temp.getString());
        } catch (Exception ex) {
            error(getString("attendance.add.failure"));
            target.addChildren(form, FeedbackPanel.class);
        }
        final Class<? extends Page> currentPageClass = getPage().getPageClass();
        if(addAnother) {
            if(Overview.class.equals(currentPageClass)) {
                window.close(target);

                Overview overviewPage = (Overview) getPage();
                final ModalWindow window2 = overviewPage.getAddOrEditItemWindow();
                window2.setTitle(new ResourceModel("attendance.add.header"));
                window2.setContent(new EventInputPanel(window2.getContentId(), window2, null, true));
                target.addChildren(form, FeedbackPanel.class);
                window2.show(target);
            }
        } else {
            if(EventView.class.equals(currentPageClass)) {
                setResponsePage(new EventView(attendanceEvent, ((EventView)getPage()).getReturnPage()));
            } else {
                setResponsePage(currentPageClass);
            }
        }
    }

    private AjaxSubmitLink createSubmitLink(final String id, final Form<?> form, final boolean createAnother) {
        return new AjaxSubmitLink(id, form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);

                processSave(target, form, createAnother);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.addChildren(form, FeedbackPanel.class);
            }

            @Override
            public boolean isVisible() {
                if(createAnother) {
                    return !isEditing;
                }

                return super.isVisible();
            }
        };
    }

    private void createValues(Form<?> form){
        eventNameField = new TextField<>("name", Model.of(attendanceEvent.getName())) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                add(new PlaceholderBehavior(getString("event.placeholder.name")));
            }
        };
        eventNameField.setRequired(true);
        form.add(eventNameField);

        startDateTimeField = new SakaiDateTimeField("startDateTime", new PropertyModel<>(this, "startDateTime"), ZoneId.systemDefault());
        startDateTimeField.setUseTime(true);
        startDateTimeField.setAllowEmptyDate(true);
        form.add(startDateTimeField);
    }

    private ResourceModel getSubmitLabel() {
        if(this.isEditing) {
            return new ResourceModel("attendance.add.edit");
        }

        return new ResourceModel("attendance.add.create");
    }

    public void renderHead(final IHeaderResponse response) {

        final String version = PortalUtils.getCDNQuery();
        response.render(StringHeaderItem.forString(
                "<script src=\"/webcomponents/rubrics/sakai-rubrics-utils.js" + version + "\"></script>"));
        response.render(StringHeaderItem.forString(
                "<script type=\"module\" src=\"/webcomponents/rubrics/rubric-association-requirements.js" + version + "\"></script>"));
    }
}



