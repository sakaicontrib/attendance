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

import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.*;

import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.tool.pages.panels.EventInputPanel;

/**
 * A simple page which allows for events to be added.
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public class AddEventPage extends BasePage {
	
	public AddEventPage() {
		disableLink(addEventLink);

        add(createForm(null));
	}

	public AddEventPage(AttendanceEvent aE) {
		disableLink(addEventLink);

		add(createForm(aE));
	}

	private Form createForm(AttendanceEvent aE) {
		boolean isEdit = true;
		if(aE == null) {
			aE = new AttendanceEvent();
			isEdit = false;
		}

		Form form = new Form("form");
		form.add(new EventInputPanel("event", new CompoundPropertyModel<AttendanceEvent>(aE)));
		form.add(new SubmitLink("submit"));

		if(isEdit) {
			ResourceModel temp = new ResourceModel("attendance.delete");
			final String text = temp.getObject();
			AjaxButton deleteButton = new AjaxButton("delete") {
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					AjaxCallListener ajaxCallListener = new AjaxCallListener();
					ajaxCallListener.onPrecondition( "return confirm('" + text + "');" );
					attributes.getAjaxCallListeners().add( ajaxCallListener );
				}
			};

			form.add(deleteButton);
		} else{
			Button hidden = new Button("delete");
			hidden.setVisible(false);
			form.add(hidden);
		}
		return form;
	}
}
