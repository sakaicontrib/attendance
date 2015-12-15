package org.sakaiproject.attendance.tool.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.Event;
import org.sakaiproject.attendance.tool.dataproviders.EventDataProvider;
import org.sakaiproject.attendance.tool.dataproviders.StudentDataProvider;
import org.sakaiproject.user.api.User;

/**
 * An example page
 * 
 * @author David Bauer
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public class Overview extends BasePage {

	public Overview() {
		disableLink(firstLink);
		
		add(new Label("student-name-header", new ResourceModel("student_name")));
		add(new Label("overview-header", new ResourceModel("overview")));

		add(new DataView<Event>("event-headers", new EventDataProvider()) {
			@Override
			protected void populateItem(Item<Event> item) {
				item.add(new Label("event-name", item.getModelObject().getName()));
			}
		});

		add(new DataView<User>("students", new StudentDataProvider()) {

			@Override
			protected void populateItem(Item<User> item) {
				final User student = item.getModelObject();
				String overviewName = student.getSortName() + " (" + student.getDisplayId() + ")";
				item.add(new Label("student-name", overviewName));
			}
		});
	}
}
