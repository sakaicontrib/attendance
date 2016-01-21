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

package org.sakaiproject.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStatus {
    private static final long serialVersionUID = 1L;

    @Getter @Setter	private 				Long 					id;
    @Getter @Setter private 				Boolean 				isActive;
    @Getter @Setter private 				Status  				status;
    @Getter @Setter private 				int      	 			sortOrder;
    @Getter @Setter private 				AttendanceSite 			attendanceSite;

    // Create a copy constructor
    public AttendanceStatus(AttendanceStatus attendanceStatus) {
        this.isActive = attendanceStatus.getIsActive();
        this.status = attendanceStatus.getStatus();
        this.sortOrder = attendanceStatus.getSortOrder();
        this.attendanceSite = attendanceStatus.getAttendanceSite();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceStatus that = (AttendanceStatus) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getIsActive(), that.getIsActive()) &&
                getStatus() == that.getStatus() &&
                getSortOrder() == that.getSortOrder() &&
                Objects.equals(getAttendanceSite(), that.getAttendanceSite());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
