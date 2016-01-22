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
package org.sakaiproject.attendance.tool.pages;


import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.tool.pages.panels.AttendanceStatusFormPanel;

public class SettingsPage extends BasePage {
	private static final long serialVersionUID = 1L;
	
	public SettingsPage() {
		disableLink(settingsLink);

		Label headerSettings = new Label("header-settings",	new ResourceModel("attendance.settings.header"));
		add(headerSettings);

		createEditStatusesPanel();
	}

	private void createEditStatusesPanel() {

		WebMarkupContainer allStatusesContainer = new WebMarkupContainer("all-statuses-container");
		allStatusesContainer.add(new AttendanceStatusFormPanel("edit-status-panel", feedbackPanel));
		add(allStatusesContainer);
	}
}
