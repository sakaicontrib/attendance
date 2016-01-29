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

package org.sakaiproject.attendance.impl;

import lombok.Setter;
import org.apache.log4j.Logger;
import org.sakaiproject.attendance.api.AttendanceGradebookProvider;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.logic.SakaiProxy;
import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;

import java.util.Map;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class AttendanceGradebookProviderImpl implements AttendanceGradebookProvider {
    private static Logger log = Logger.getLogger(AttendanceGradebookProviderImpl.class);

    @Setter private AttendanceLogic attendanceLogic;
    @Setter private SakaiProxy sakaiProxy;
    @Setter private ToolManager toolManager;
    @Setter private GradebookExternalAssessmentService gbExtAssesService;


    public void init() {
        log.info("init()");
    }

    public void sendToGradebook(Long id) {
        if(log.isDebugEnabled()) {
            log.debug("sendToGradebook");
        }

        if(id == null) {
            return;
        }
        AttendanceGrade aG = attendanceLogic.getAttendanceGrade(id);
        AttendanceSite aS = aG.getAttendanceSite();
        String siteID = aS.getSiteID();

        // check if there is a gradebook
        if (gbExtAssesService.isGradebookDefined(siteID)) {
            String aSID = aS.getId().toString();

            Boolean sendToGradebook = aG.getAttendanceSite().getSendToGradebook();
            if(sendToGradebook != null && sendToGradebook) {
                Tool tool = toolManager.getCurrentTool();
                String appName = "sakai.attendance";
                if(tool != null ) {
                    appName = tool.getId();

                }

                if(gbExtAssesService.isExternalAssignmentDefined(siteID, aSID)) {
                    // exists, update current grade
                    gbExtAssesService.updateExternalAssessmentScore(siteID, aSID, aG.getUserID(), aG.getGrade().toString());
                } else {
                    //does not exist, add to GB and add all grades
                    try {
                        gbExtAssesService.addExternalAssessment(siteID, aSID, null, "Attendance", aS.getMaximumGrade(), null, appName, false, null);// add it to the gradebook
                        Map<String, String> scores = attendanceLogic.getAttendanceGradeScores();

                        gbExtAssesService.updateExternalAssessmentScoresString(siteID, aSID, scores);
                    } catch (ConflictingAssignmentNameException e) {
                        log.warn("Attendance sending grades to gradebook error. Name conflicts");
                    }
                }
            }
        }

    }
}
