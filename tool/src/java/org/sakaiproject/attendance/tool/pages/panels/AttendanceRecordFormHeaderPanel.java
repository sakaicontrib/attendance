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

package org.sakaiproject.attendance.tool.pages.panels;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class AttendanceRecordFormHeaderPanel extends BasePanel {

    public AttendanceRecordFormHeaderPanel(String id) {
        super(id);

        add(createStatusHeader());
    }

    private WebMarkupContainer createStatusHeader() {
        WebMarkupContainer status = new WebMarkupContainer("status");

        status.add(new Label("status-present", 		new ResourceModel("attendance.overview.header.status.present")));
        status.add(new Label("status-late", 		    new ResourceModel("attendance.overview.header.status.late")));
        status.add(new Label("status-left-early", 	new ResourceModel("attendance.overview.header.status.left.early")));
        status.add(new Label("status-excused", 		new ResourceModel("attendance.overview.header.status.excused")));
        status.add(new Label("status-unexcused", 	    new ResourceModel("attendance.overview.header.status.unexcused")));

        return status;
    }
}
