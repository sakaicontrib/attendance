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
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.*;

import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.tool.dataproviders.EventDataProvider;
import org.sakaiproject.attendance.tool.pages.panels.EventInputPanel;
import org.sakaiproject.attendance.tool.util.ConfirmationLink;

import java.util.Date;

/**
 * A simple page which allows for events to be added.
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public class AddEventPage extends BasePage {
	protected AttendanceEvent attendanceEvent;

	EventDataProvider eventDataProvider;

	WebMarkupContainer eventListContainer;
	WebMarkupContainer eventFormContainer;

	Form eventForm;
	ConfirmationLink<Void> deleteItem;

	public AddEventPage() {
		this(null);
	}

	public AddEventPage(AttendanceEvent aE) {
		disableLink(addEventLink);

		add(new Label("items-header", getString("attendance.items.header")));

		this.attendanceEvent = aE;

		eventFormContainer = new WebMarkupContainer("event-form-container");
		eventFormContainer.setOutputMarkupId(true);


		if(attendanceEvent != null) {
			deleteItem = new ConfirmationLink<Void>("delete-event", getString("attendance.delete.confirm")) {
				@Override
				public void onClick(AjaxRequestTarget ajaxRequestTarget) {
					String name = attendanceEvent.getName();
					if(attendanceLogic.deleteAttendanceEvent(attendanceEvent)) {
						getSession().info(name + " deleted successfully.");
						refreshPageComponents(ajaxRequestTarget);
					}
				}
			};
			eventFormContainer.add(deleteItem);
		} else {
			// Add dummy/hidden delete link
			deleteItem = new ConfirmationLink<Void>("delete-event", "") {
				@Override
				public void onClick(AjaxRequestTarget ajaxRequestTarget) {
					// Do nothing
				}
			};
			deleteItem.setVisible(false);
			eventFormContainer.add(deleteItem);
		}

		eventForm = createForm();
		eventFormContainer.add(eventForm);

		eventFormContainer.add(new Label("add-edit-header", getString("attendance.add.edit.header")));

		add(eventFormContainer);

		eventListContainer = new WebMarkupContainer("event-list-container");
		eventListContainer.setOutputMarkupId(true);

		eventDataProvider = new EventDataProvider();

		DataView<AttendanceEvent> attendanceEventDataView = new DataView<AttendanceEvent>("event-list", eventDataProvider) {
			@Override
			protected void populateItem(final Item<AttendanceEvent> item) {
				ConfirmationLink<Void> deleteLink = new ConfirmationLink<Void>("delete-link", getString("attendance.delete.confirm")) {
					@Override
					public void onClick(AjaxRequestTarget ajaxRequestTarget) {
						String name = item.getModelObject().getName();
						if(attendanceLogic.deleteAttendanceEvent(item.getModelObject())) {
							getSession().info(name + " deleted successfully.");
							refreshPageComponents(ajaxRequestTarget);
						} else {
							getSession().error("Failed to delete " + name);
							refreshPageComponents(ajaxRequestTarget);
						}
					}
				};
				item.add(deleteLink);
				Link<Void> editLink = new Link<Void>("edit-link") {
					@Override
					public void onClick() {
						setResponsePage(new AddEventPage(item.getModelObject()));
					}
				};
				item.add(editLink);
				Link<Void> eventLink = new Link<Void>("event-link") {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new EventView(item.getModelObject(), BasePage.ITEMS_PAGE));
					}
				};
				eventLink.add(new Label("event-name", item.getModelObject().getName()));
				item.add(eventLink);
				item.add(new Label("item-date", item.getModelObject().getStartDateTime()));
			}
		};

		eventListContainer.add(attendanceEventDataView);
		eventListContainer.add(new Label("items-list-header", getString("attendance.items.list.header")));

		add(eventListContainer);
	}

	private Form createForm() {
		if(attendanceEvent == null) {
			attendanceEvent = new AttendanceEvent();
		}

		Form form = new Form("form");
		form.add(new EventInputPanel("event", new CompoundPropertyModel<AttendanceEvent>(attendanceEvent)));
		form.add(new AjaxSubmitLink("submit") {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				refreshPageComponents(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(feedbackPanel);
			}
		});

		return form;
	}

	private void refreshPageComponents(AjaxRequestTarget target) {
		attendanceEvent = null;
		eventForm = createForm();
		eventFormContainer.addOrReplace(eventForm);
		deleteItem.setVisible(false);
		eventFormContainer.addOrReplace(deleteItem);
		target.add(eventFormContainer);
		target.add(eventListContainer);
		target.add(feedbackPanel);
	}
}
