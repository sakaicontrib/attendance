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

package org.sakaiproject.attendance.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * An AttendanceStatus is a wrapper around the Status enum type defining meta information on individual Statuses.
 *
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AttendanceStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter @Setter	private 				Long 					id;
    @Getter @Setter private 				Boolean 				isActive;
    @Getter @Setter private 				Status  				status;
    @Getter @Setter private 				int      	 			sortOrder;
    @Getter @Setter private 				AttendanceSite 			attendanceSite;

    // Create a copy constructor
    public AttendanceStatus(AttendanceStatus attendanceStatus) {
        this.isActive       = attendanceStatus.getIsActive();
        this.status         = attendanceStatus.getStatus();
        this.sortOrder      = attendanceStatus.getSortOrder();
        this.attendanceSite = attendanceStatus.getAttendanceSite();
    }

}
