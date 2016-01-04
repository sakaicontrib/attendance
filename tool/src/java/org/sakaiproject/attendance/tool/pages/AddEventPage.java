/*
 *  Copyright (c) 2015, The Apereo Foundation
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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.*;

import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.tool.pages.panels.EventInputPanel;
import org.sakaiproject.attendance.tool.util.ConfirmationLink;

/**
 * A simple page which allows for events to be added.
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public class AddEventPage extends BasePage {
	protected AttendanceEvent attendanceEvent;

	public AddEventPage() {
		this(null);
	}

	public AddEventPage(AttendanceEvent aE) {
		disableLink(addEventLink);

		this.attendanceEvent = aE;

		if(attendanceEvent != null) {
			ConfirmationLink<Void> deleteEvent = new ConfirmationLink<Void>("delete-event", "Are you sure you want to delete this event?") {
				@Override
				public void onClick(AjaxRequestTarget ajaxRequestTarget) {
					String name = attendanceEvent.getName();
					if(attendanceLogic.deleteAttendanceEvent(attendanceEvent)) {
						getSession().info(name + " deleted successfully.");
						setResponsePage(new Overview());
					}
				}
			};
			add(deleteEvent);
		} else {
			// Add dummy/hidden delete link
			add(new Link<Void>("delete-event") {
				@Override
				public void onClick() {
					// Do nothing
				}
			}.setVisible(false));
		}

		add(createForm());
	}

	private Form createForm() {
		if(attendanceEvent == null) {
			attendanceEvent = new AttendanceEvent();
		}

		Form form = new Form("form");
		form.add(new EventInputPanel("event", new CompoundPropertyModel<AttendanceEvent>(attendanceEvent)));
		form.add(new SubmitLink("submit"));

		return form;
	}
}
