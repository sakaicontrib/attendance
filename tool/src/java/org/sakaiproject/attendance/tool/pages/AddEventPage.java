package org.sakaiproject.attendance.tool.pages;

import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.*;

import org.sakaiproject.attendance.model.Event;
import org.sakaiproject.attendance.tool.pages.panels.EventInputPanel;

/**
 * An example page. This interacts with a list of items from the database
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class AddEventPage extends BasePage {
	
	public AddEventPage() {
		disableLink(addEventLink);

		//add our form
		Form form = new Form("form");
		form.add(new EventInputPanel("event", new CompoundPropertyModel<Event>(new Event())));
		form.add(new SubmitLink("submit"));
        add(form);
	}
}
