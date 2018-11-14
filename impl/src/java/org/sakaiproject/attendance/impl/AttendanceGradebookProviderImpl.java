/*
 *  Copyright (c) 2017, University of Dayton
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
import org.sakaiproject.attendance.util.AttendanceConstants;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

import java.util.Map;

/**
 * Implementation of AttendanceGradebookProvider, {@link org.sakaiproject.attendance.api.AttendanceGradebookProvider}
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class AttendanceGradebookProviderImpl implements AttendanceGradebookProvider {
    private static Logger log = Logger.getLogger(AttendanceGradebookProviderImpl.class);

    @Setter private AttendanceLogic                     attendanceLogic;
    @Setter private SakaiProxy                          sakaiProxy;
    @Setter private ToolManager                         toolManager;
    @Setter private GradebookExternalAssessmentService  gbExtAssesService;

    /**
     * {@inheritDoc}
     */
    public void init() {
        log.debug("AttendanceGradebookProviderImpl init()");
    }

    /**
     * {@inheritDoc}
     */
    public boolean create(AttendanceSite aS) {
        if(log.isDebugEnabled()) {
            log.debug("create Gradebook");
        }

        boolean returnVal = false;

        String siteID = aS.getSiteID();

        if(isGradebookDefined(siteID)) {
            Tool tool = toolManager.getCurrentTool();
            String appName = AttendanceConstants.TOOL_NAME;
            if(tool != null ) {
                appName = tool.getTitle();

            }

            String aSUID = getAttendanceUID(aS);
            try {
                gbExtAssesService.addExternalAssessment(siteID, aSUID, null, aS.getGradebookItemName(), aS.getMaximumGrade(), null, appName, null, false, null);// add it to the gradebook

                Map<String, String> scores = attendanceLogic.getAttendanceGradeScores();
                gbExtAssesService.updateExternalAssessmentScoresString(siteID, aSUID, scores);
                returnVal = true;
            } catch (Exception e) {
                log.warn("Error creating external GB", e);
            }
        }

        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    public boolean remove(AttendanceSite aS) {
        if(log.isDebugEnabled()) {
            log.debug("remove GB for AS " + aS.getSiteID());
        }

        if(isGradebookDefined(aS.getSiteID())) {
            try {
                gbExtAssesService.removeExternalAssessment(aS.getSiteID(), getAttendanceUID(aS));
                return true;
            } catch (AssessmentNotFoundException e) {
                log.warn("Attempted to remove AttendanceSite " + aS.getSiteID() + " from GB failed. Assessment not found", e);
                return false;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean update(AttendanceSite aS) {
        if(log.isDebugEnabled()) {
            log.debug("Updating GB for AS " + aS.getSiteID());
        }

        String siteID = aS.getSiteID();
        if(isGradebookDefined(siteID)) {
            String aUID = getAttendanceUID(aS);
            if(isAssessmentDefined(siteID, aUID)){
                try {
                    gbExtAssesService.updateExternalAssessment(siteID, aUID, null, null, aS.getGradebookItemName(), aS.getMaximumGrade(), null, false);
                    return true;
                } catch (ConflictingAssignmentNameException e) {
                    log.warn("Failed to update AttendanceSite for site " + siteID + " in Gradebook", e);
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean sendToGradebook(final AttendanceGrade aG) {
        if(aG == null) {
            return false;
        }

        final AttendanceSite aS = aG.getAttendanceSite();
        final String siteID = aS.getSiteID();

        // check if there is a gradebook. siteID ~= gradebookUID
        if (isGradebookDefined(siteID)) {
            String aSUID = getAttendanceUID(aS);

            final Boolean sendToGradebook = aS.getSendToGradebook();
            if(sendToGradebook != null && sendToGradebook) {
                if(isAssessmentDefined(siteID, aSUID)) {
                    // exists, update current grade
                    String grade = aG.getGrade() == null ? null : aG.getGrade().toString();
                    gbExtAssesService.updateExternalAssessmentScore(siteID, aSUID, aG.getUserID(), grade);
                    return true;
                } else {
                    //does not exist, add to GB and add all grades
                    return create(aS);
                }
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isGradebookDefined(String gbUID) {
        // siteID should be equivalent to GradebookUID
        return gbExtAssesService.isGradebookDefined(gbUID);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isGradebookAssignmentDefined(String gbUID, String title) {
        return gbExtAssesService.isAssignmentDefined(gbUID, title);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAssessmentDefined(String gbUID, Long aSID) {
        return isAssessmentDefined(gbUID, getAttendanceUID(aSID));
    }

    private boolean isAssessmentDefined(String gbUID, String id) {
        return gbExtAssesService.isExternalAssignmentDefined(gbUID, id);
    }

    //this is hacky
    private String getAttendanceUID(AttendanceSite aS) {
        return getAttendanceUID(aS.getId());
    }

    private String getAttendanceUID(Long id) {
        return AttendanceConstants.SAKAI_TOOL_NAME + "." + id.toString();
    }
}
