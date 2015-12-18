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

import org.apache.wicket.markup.html.link.Link;

/**
 * An example page
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class SettingsPage extends BasePage {

	Link<Void> toAddEventPageLink;
	
	public SettingsPage() {
		disableLink(settingsLink);
		
		
		//link to third page
		//the i18n label for this is directly in the HTML
		toAddEventPageLink = new Link<Void>("toAddEventPageLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new AddEventPage());
			}
		};
		add(toAddEventPageLink);

		
		
	}
}
