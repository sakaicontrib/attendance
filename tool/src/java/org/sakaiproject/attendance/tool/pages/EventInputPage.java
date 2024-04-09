package org.sakaiproject.attendance.tool.pages;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.*;
import org.sakaiproject.attendance.api.model.AttendanceEvent;
import org.sakaiproject.attendance.tool.util.AttendanceFeedbackPanel;
import org.sakaiproject.attendance.tool.util.PlaceholderBehavior;
import org.sakaiproject.wicket.component.SakaiDateTimeField;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class EventInputPage extends BasePage{
    private static final long serialVersionUID = 1L;

    private TextField<String> eventNameField;
    private SakaiDateTimeField startDateTimeField;

    private ZonedDateTime startDateTime;

    private String nextPage;
    public static String STUDENT_OVERVIEW = "org.sakaiproject.attendance.tool.pages.StudentView";
    public static String GRADING = "org.sakaiproject.attendance.tool.pages.GradingPage";
    public static String SETTINGS = "org.sakaiproject.attendance.tool.pages.SettingsPage";
    public static String IMPORTEXPORT = "org.sakaiproject.attendance.tool.pages.ExportPage";

    public EventInputPage(final IModel<AttendanceEvent> event) {
        disableLink(this.addLink);
        Form<?> eventForm = createEventInputForm();
        this.add(eventForm);
    }

    private Form<?> createEventInputForm() {
        Form<?> eventForm = new Form<Void>("event");
        eventForm.add(new AttendanceFeedbackPanel("addEditItemFeedback"));
        eventForm.add(new Label("addItemTitle", getString("event.add")));
        AjaxSubmitLink submit = createSubmitLink("submit", eventForm, false);
        submit.add(new Label("submitLabel", new ResourceModel("attendance.add.create")));
        eventForm.add(submit);
        AjaxSubmitLink submitMore = createSubmitLink("submitAndAddAnother", eventForm, true);
        submitMore.add(new Label("submitMoreLabel", new ResourceModel("attendance.add.create.another")));
        eventForm.add(submitMore);
        final AjaxButton cancel = new AjaxButton("cancel") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                // decide where to go based on where we came from.
                String nextPage = EventInputPage.this.getNextPage();
                if(StringUtils.equals(nextPage, EventInputPage.GRADING)) {
                    setResponsePage(new GradingPage());
                }else if(StringUtils.equals(nextPage, EventInputPage.IMPORTEXPORT)) {
                    setResponsePage(new ExportPage());
                }else if(StringUtils.equals(nextPage, EventInputPage.SETTINGS)) {
                    setResponsePage(new SettingsPage());
                }else if(StringUtils.equals(nextPage, EventInputPage.STUDENT_OVERVIEW)){
                    setResponsePage(new StudentView());
                }else{
                    setResponsePage(new Overview());
                }
            }
        };
        cancel.setDefaultFormProcessing(false);
        eventForm.add(cancel);

        eventNameField = new TextField<>("name", new Model<>()) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                add(new PlaceholderBehavior(getString("event.placeholder.name")));
            }
        };
        eventNameField.setRequired(true);
        eventForm.add(eventNameField);

        startDateTimeField = new SakaiDateTimeField("startDateTime", new PropertyModel<>(this, "startDateTime"), ZoneId.systemDefault());
        startDateTimeField.setUseTime(true);
        startDateTimeField.setAllowEmptyDate(true);
        eventForm.add(startDateTimeField);

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
            AttendanceEvent nEvent = new AttendanceEvent();

            nEvent.setAttendanceSite(attendanceLogic.getCurrentAttendanceSite());
            nEvent.setName(eventNameField.getModelObject());

            ZonedDateTime startDate = startDateTimeField.getModelObject();
            nEvent.setStartDateTime(startDate == null ? null : startDate.toInstant());

            nEvent = attendanceLogic.updateAttendanceEvent(nEvent);

            getSession().success(new StringResourceModel("attendance.add.success", null, (Object[]) new String[]{nEvent.getName()}).toString());
        } catch (Exception ex) {
            error(getString("attendance.add.failure"));
            target.addChildren(form, FeedbackPanel.class);
        }
        if(addAnother) {
            setResponsePage(new EventInputPage(new CompoundPropertyModel<>(new AttendanceEvent())));
        } else {
            setResponsePage(Overview.class);
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
                return super.isVisible();
            }
        };
    }

    public void setNextPage(String canonicalName){
        this.nextPage = canonicalName;
    }

    public String getNextPage(){
        return this.nextPage;
    }
}
