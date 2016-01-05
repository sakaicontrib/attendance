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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.EventDataProvider;

import java.util.Map;

/**
 * The overview page which lists AttendanceEvents and basic statistics of each
 * events AttendanceRecords.
 * 
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public class Overview extends BasePage {
	private static final long serialVersionUID = 1L;

	public Overview() {
		disableLink(this.firstLink);
		
		createHeaders();
		createTable();

	}

	private void createHeaders() {
		// Main header
		Label headerOverview 		= new Label("header-overview",			new ResourceModel("attendance.overview.header"));

		//headers for the table
		Label headerEventName 		= new Label("header-event-name", 			new ResourceModel("attendance.overview.header.event.name"));
		Label headerEventDate 		= new Label("header-event-date", 			new ResourceModel("attendance.overview.header.event.date"));
		Label headerStatusPresent 	= new Label("header-status-present", 		new ResourceModel("attendance.overview.header.status.present"));
		Label headerStatusLate 		= new Label("header-status-late", 		new ResourceModel("attendance.overview.header.status.late"));
		Label headerStatusLeftEarly = new Label("header-status-left-early", 	new ResourceModel("attendance.overview.header.status.left.early"));
		Label headerStatusExcused 	= new Label("header-status-excused", 		new ResourceModel("attendance.overview.header.status.excused"));
		Label headerStatusUnexcused = new Label("header-status-unexcused", 	new ResourceModel("attendance.overview.header.status.unexcused"));
		Label headerEventEdit		= new Label("header-event-edit", 		new ResourceModel("attendance.overview.header.event.edit"));

		add(headerOverview);
		add(headerEventName);
		add(headerEventDate);
		add(headerStatusPresent);
		add(headerStatusLate);
		add(headerStatusLeftEarly);
		add(headerStatusExcused);
		add(headerStatusUnexcused);
		add(headerEventEdit);

		add(new Label("overview-info", new ResourceModel("attendance.overview.info")));
	}

	private void createTable() {
		add(new DataView<AttendanceEvent>("events", new EventDataProvider()) {
			@Override
			protected void populateItem(final Item<AttendanceEvent> item) {
				Map<Status, Integer> stats = item.getModelObject().getStats();
				item.add(new Label("event-name", item.getModelObject().getName()));
				item.add(new Label("event-date", item.getModelObject().getStartDateTime()));
				item.add(new Label("event-stats-present", stats.get(Status.PRESENT)));
				item.add(new Label("event-stats-late", stats.get(Status.LATE)));
				item.add(new Label("event-stats-left-early", stats.get(Status.LEFT_EARLY)));
				item.add(new Label("event-stats-excused", stats.get(Status.EXCUSED_ABSENCE)));
				item.add(new Label("event-stats-absent", stats.get(Status.UNEXCUSED_ABSENCE)));
				item.add(new Link<Void>("event-edit-link") {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new AddEventPage(item.getModelObject()));
					}
				});
			}
		});
	}
}
