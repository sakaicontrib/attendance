/*
 *  Copyright (c) 2016, University of Dayton
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

import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.tool.util.PlaceholderBehavior;


/**
 * EventInputPanel is used to get AttendanceEvent settings for a new or existing AttendanceEvent
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class EventInputPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private IModel<AttendanceEvent> eventModel;

    public EventInputPanel(String id, IModel<AttendanceEvent> event) {
        super(id, event);
        this.eventModel = event;
        add(createEventInputForm());
    }

    private Form<AttendanceEvent> createEventInputForm() {
        Form<AttendanceEvent> event = new Form<AttendanceEvent>("event", this.eventModel) {
            @Override
            public void onSubmit(){
                AttendanceEvent e = (AttendanceEvent) getDefaultModelObject();
                e.setAttendanceSite(attendanceLogic.getCurrentAttendanceSite());
                boolean result = attendanceLogic.updateAttendanceEvent(e);

                if(result){
                    StringResourceModel temp = new StringResourceModel("attendance.add.success", null, new String[]{e.getName()});
                    getSession().info(temp.getString());
                } else {
                    getSession().error(getString("attendance.add.failure"));
                }
            }
        };
        createLabels(event);
        createValues(event);

        return event;
    }

    private void createLabels(Form<AttendanceEvent> event){
        final Label nameLabel           = new Label("labelName", new ResourceModel("attendance.add.label.name"));
        final Label startDateTimeLabel  = new Label("labelStartDateTime", new ResourceModel("attendance.add.label.startDateTime"));
        final Label endDateTimeLabel    = new Label("labelEndDateTime", new ResourceModel("attendance.add.label.endDateTime"));
        final Label isRequiredLabel     = new Label("labelIsRequired", new ResourceModel("attendance.add.label.isRequired"));
        final Label locationLabel       = new Label("labelLocation", new ResourceModel("attendance.add.label.location"));
        final Label releasedToLabel     = new Label("labelReleasedTo", new ResourceModel("attendance.add.label.releasedTo"));
        final Label isReoccurringLabel  = new Label("labelIsReoccurring", new ResourceModel("attendance.add.label.isReoccurring"));

        releasedToLabel.setVisible(false);
        isReoccurringLabel.setVisible(false);
        endDateTimeLabel.setVisible(false);
        locationLabel.setVisible(false);
        isRequiredLabel.setVisible(false);

        event.add(nameLabel);
        event.add(startDateTimeLabel);
        event.add(endDateTimeLabel);
        event.add(isRequiredLabel);
        event.add(locationLabel);
        event.add(releasedToLabel);
        event.add(isReoccurringLabel);
    }

    private void createValues(Form<AttendanceEvent> event){
        final TextField name = new TextField<String>("name") {
            @Override
            protected void onInitialize(){
                super.onInitialize();
                add(new PlaceholderBehavior(getString("event.placeholder.name")));
            }
        };
        final DateTimeField startDateTime = new DateTimeField("startDateTime");
        final DateTimeField endDateTime = new DateTimeField("endDateTime");
        final CheckBox isRequired = new CheckBox("isRequired");
        final TextField<String> location = new TextField<String>("location") {
            @Override
            protected void onInitialize(){
                super.onInitialize();
                add(new PlaceholderBehavior(getString("event.placeholder.location")));
            }
        };
        final TextField<String> releasedTo = new TextField<String>("releasedTo");

        name.setRequired(true);

        releasedTo.setVisible(false);
        endDateTime.setVisible(false);
        isRequired.setVisible(false);
        location.setVisible(false);

        event.add(name);
        event.add(startDateTime);
        event.add(endDateTime);
        event.add(isRequired);
        event.add(location);
        event.add(releasedTo);
    }
}



