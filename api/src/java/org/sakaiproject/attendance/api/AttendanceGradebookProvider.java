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

package org.sakaiproject.attendance.api;

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
    void remove(AttendanceSite aS);

    /**
     * Sends an AttendanceGrade, by ID, to the Gradebook
     * @param id
     */
    void sendToGradebook(Long id);

    /**
     * Is Gradebook Defined
     * @param gbUID
     * @return
     */
    boolean isGradebookDefined(String gbUID);

    /**
     * Returns if gradebook has an internal assignment defined with the provided title
     * @param gbUID
     * @param title
     * @return
     */
    boolean isGradebookAssignmentDefined(String gbUID, String title);
}
