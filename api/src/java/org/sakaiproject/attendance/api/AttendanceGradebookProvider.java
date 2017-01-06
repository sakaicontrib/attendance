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

package org.sakaiproject.attendance.api;

import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.AttendanceSite;

/**
 * A Provider which sends grades to the Gradebook
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public interface AttendanceGradebookProvider {
    void init();

    /**
     * Create a new External Assessment in the Gradebook
     * @param aS, the AttendanceSite
     */
    boolean create(AttendanceSite aS);

    /**
     * Remove an External Assessment from the Gradebook
     * @param aS, the AttendanceSite to remove
     */
    boolean remove(AttendanceSite aS);

    /**
     * Updates the external Assessment in the Gradebook
     * @param aS
     */
    boolean update(AttendanceSite aS);

    /**
     * Sends an AttendanceGrade to the Gradebook
     * @param ag, the AttendanceGrade to update
     * @return success of operation
     */
    boolean sendToGradebook(AttendanceGrade ag);

    /**
     * Returns if a Gradebook has been Defined in a site
     * @param gbUID
     * @return
     */
    boolean isGradebookDefined(String gbUID);

    /**
     * Returns if gradebook has an assignment defined with the provided title
     * @param gbUID
     * @param title
     * @return
     */
    boolean isGradebookAssignmentDefined(String gbUID, String title);

    /**
     * Returns if External Assessment is defined in gradebook
     * @param gbUID, the gradebookUID (typically siteID)
     * @param aSID, AttendanceSite ID
     * @return
     */
    boolean isAssessmentDefined(String gbUID, Long aSID);
}
