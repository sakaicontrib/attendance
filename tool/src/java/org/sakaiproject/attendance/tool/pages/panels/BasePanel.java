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

package org.sakaiproject.attendance.tool.pages.panels;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.attendance.api.AttendanceGradebookProvider;
import org.sakaiproject.attendance.export.PDFEventExporter;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.logic.SakaiProxy;
import org.sakaiproject.attendance.model.Status;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class BasePanel extends Panel {
    @SpringBean(name="org.sakaiproject.attendance.logic.SakaiProxy")
    protected SakaiProxy sakaiProxy;

    @SpringBean(name="org.sakaiproject.attendance.logic.AttendanceLogic")
    protected AttendanceLogic attendanceLogic;

    @SpringBean(name="org.sakaiproject.attendance.export.PDFEventExporter")
    protected PDFEventExporter pdfExporter;

    @SpringBean(name="org.sakaiproject.attendance.api.AttendanceGradebookProvider")
    protected AttendanceGradebookProvider attendanceGradebookProvider;

    protected static final Logger log = Logger.getLogger(BasePanel.class);

    protected String role;

    public BasePanel(String id) {
        super(id);
        init();
    }

    public BasePanel(String id, IModel<?> i){
        super(id, i);
        init();
    }

    protected String getStatusString(Status s) {
        switch (s)
        {
            case UNKNOWN: return new ResourceModel("attendance.status.unknown").getObject();
            case PRESENT: return new ResourceModel("attendance.status.present").getObject();
            case EXCUSED_ABSENCE: return new ResourceModel("attendance.status.excused").getObject();
            case UNEXCUSED_ABSENCE: return new ResourceModel("attendance.status.absent").getObject();
            case LATE: return new ResourceModel("attendance.status.late").getObject();
            case LEFT_EARLY: return new ResourceModel("attendance.status.left.early").getObject();
            default: return new ResourceModel("attendance.status.unknown").getObject();
        }
    }

    private void init(){
        this.role = sakaiProxy.getCurrentUserRoleInCurrentSite();
    }
}
