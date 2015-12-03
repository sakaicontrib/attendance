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
