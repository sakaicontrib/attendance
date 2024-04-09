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

package org.sakaiproject.attendance.api.model.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.sakaiproject.attendance.api.model.AttendanceSite;

import java.util.List;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AttendanceUserStats extends AttendanceStats {
    private static final    long            serialVersionUID    = 1L;

    private                 Long            id;
    private                 String          userID;
    private                 AttendanceSite  attendanceSite;

    public AttendanceUserStats(String userID, AttendanceSite attendanceSite) {
        this.userID = userID;
        this.attendanceSite = attendanceSite;
    }

    public AttendanceUserStats(String userId, AttendanceSite attendanceSite, List<StatusCount> statusCounts) {
        this.userID = userId;
        this.attendanceSite = attendanceSite;
        super.setTotalsFromCounts(statusCounts);
    }

}
