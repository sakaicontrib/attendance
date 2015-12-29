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
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.Event;
import org.sakaiproject.attendance.tool.dataproviders.EventDataProvider;
import org.sakaiproject.attendance.tool.dataproviders.StudentDataProvider;
import org.sakaiproject.user.api.User;

import java.util.Collections;
import java.util.List;

/**
 * An example page
 * 
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public class Overview extends BasePage {
	private static final long serialVersionUID = 1L;

	public Overview() {
		disableLink(this.firstLink);
		
		add(new Label("student-name-header", new ResourceModel("student_name")));
		add(new Label("overview-header", new ResourceModel("overview")));

		List<Event> eventData = attendanceLogic.getEventsForCurrentSite();
		Collections.reverse(eventData);
		EventDataProvider eventDataProvider = new EventDataProvider(eventData);

		add(new DataView<Event>("event-headers", eventDataProvider) {
			@Override
			protected void populateItem(Item<Event> item) {
				item.add(new Label("event-name", item.getModelObject().getName()));
			}
		});

		List<User> userData = sakaiProxy.getCurrentSiteMembership();
		Collections.reverse(userData);
		StudentDataProvider studentDataProvider = new StudentDataProvider(userData);

		add(new DataView<User>("students", studentDataProvider) {
			@Override
			protected void populateItem(Item<User> item) {
				final User student = item.getModelObject();
				String overviewName = student.getSortName() + " (" + student.getDisplayId() + ")";
				item.add(new Label("student-name", overviewName));
			}
		});
	}
}
