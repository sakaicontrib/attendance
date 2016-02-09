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

package org.sakaiproject.attendance.tool.pages;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;
import org.sakaiproject.attendance.tool.dataproviders.EventDataProvider;
import org.sakaiproject.attendance.tool.pages.panels.PrintPanel;

import java.util.Date;
import java.util.Map;

/**
 * The overview page which lists AttendanceEvents and basic statistics of each
 * events AttendanceRecords.
 * 
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class Overview extends BasePage {
	private static final long serialVersionUID = 1L;

	PrintPanel printPanel;
	WebMarkupContainer printContainer;

	private AttendanceStatusProvider attendanceStatusProvider;

	private Model<String> printHiddenClass = new Model<String>("printHidden");

	public Overview() {
		disableLink(this.firstLink);

		if (this.role != null && this.role.equals("Student")) {
			throw new RestartResponseException(StudentView.class);
		}

		this.attendanceStatusProvider = new AttendanceStatusProvider(attendanceLogic.getCurrentAttendanceSite(), AttendanceStatusProvider.ACTIVE);
		
		createHeaders();
		createTable();

		this.printContainer = new WebMarkupContainer("print-container");
		printContainer.setOutputMarkupId(true);

		this.printPanel = new PrintPanel("print-panel", new Model<AttendanceEvent>());

		printContainer.add(printPanel);

		printContainer.add(AttributeModifier.append("class", printHiddenClass));

		add(printContainer);

		createTakeAttendanceNow();
	}

	private void createHeaders() {
		// Main header
		Label headerOverview 		= new Label("header-overview",				new ResourceModel("attendance.overview.header"));

		//headers for the table
		Label headerEventName 		= new Label("header-event-name", 			new ResourceModel("attendance.overview.header.event.name"));
		Label headerEventDate 		= new Label("header-event-date", 			new ResourceModel("attendance.overview.header.event.date"));

		DataView<AttendanceStatus> statusHeaders = new DataView<AttendanceStatus>("status-headers", attendanceStatusProvider) {
			@Override
			protected void populateItem(Item<AttendanceStatus> item) {
				item.add(new Label("header-status-name", getStatusString(item.getModelObject().getStatus())));
			}
		};
		add(statusHeaders);

		Label headerEventEdit		= new Label("header-event-edit", 			new ResourceModel("attendance.overview.header.event.edit"));
		Label headerPrintLinks		= new Label("header-print-links",			new ResourceModel("attendance.overview.header.print"));

		add(headerOverview);
		add(headerEventName);
		add(headerEventDate);
		add(headerEventEdit);
		add(headerPrintLinks);

	}

	private void createTable() {
		EventDataProvider eventDataProvider = new EventDataProvider();
		DataView<AttendanceEvent> attendanceEventDataView = new DataView<AttendanceEvent>("events", eventDataProvider) {
			@Override
			protected void populateItem(final Item<AttendanceEvent> item) {
				final Map<Status, Integer> stats = attendanceLogic.getStatsForEvent(item.getModelObject());
				Link<Void> eventLink = new Link<Void>("event-link") {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new EventView(item.getModelObject(), BasePage.OVERVIEW_PAGE));
					}
				};
				eventLink.add(new Label("event-name", item.getModelObject().getName()));

				item.add(eventLink);
				item.add(new Label("event-date", item.getModelObject().getStartDateTime()));

				DataView<AttendanceStatus> activeStatusStats = new DataView<AttendanceStatus>("active-status-stats", attendanceStatusProvider) {
					@Override
					protected void populateItem(Item<AttendanceStatus> item) {
						item.add(new Label("event-stats", stats.get(item.getModelObject().getStatus())));
					}
				};
				item.add(activeStatusStats);

				item.add(new Link<Void>("event-edit-link") {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new AddEventPage(item.getModelObject()));
					}
				});
				item.add(new AjaxLink<Void>("print-link"){
					@Override
					public void onClick(AjaxRequestTarget ajaxRequestTarget) {
						printPanel = new PrintPanel("print-panel", item.getModel());
						printContainer.setOutputMarkupId(true);
						printContainer.addOrReplace(printPanel);
						printHiddenClass.setObject("printVisible");
						ajaxRequestTarget.add(printContainer);
					}
				});
			}
		};
		add(attendanceEventDataView);

		// Create empty table placeholder and make visible based on empty data provider
		Label noEvents = new Label("no-events", getString("attendance.overview.no.items"));
		noEvents.setEscapeModelStrings(false);

		if(eventDataProvider.size() > 0) {
			noEvents.setVisible(false);
		}

		add(noEvents);
	}

	private void createTakeAttendanceNow() {
		final Form<?> takeAttendanceNowForm = new Form<Void>("take-attendance-now-form") {
			@Override
			protected void onSubmit() {
				AttendanceEvent newEvent = new AttendanceEvent();
				newEvent.setAttendanceSite(attendanceLogic.getCurrentAttendanceSite());
				newEvent.setName(new ResourceModel("attendance.now.name").getObject());
				newEvent.setStartDateTime(new Date());
				Long newEventId = (Long) attendanceLogic.addAttendanceEventNow(newEvent);
				if(newEventId != null) {
					newEvent = attendanceLogic.getAttendanceEvent(newEventId);
					setResponsePage(new EventView(newEvent, BasePage.OVERVIEW_PAGE));
				} else {
					error(new ResourceModel("attendance.now.error").getObject());
				}
			}
		};
		takeAttendanceNowForm.add(new SubmitLink("take-attendance-now"));
		add(takeAttendanceNowForm);
	}
}
