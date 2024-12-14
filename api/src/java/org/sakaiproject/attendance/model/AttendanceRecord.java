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

import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * An AttendanceRecord for a specific user for a specific AttendanceEvent
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AttendanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long            id;
    private AttendanceEvent attendanceEvent;
    private String          userID;
    private Status          status;
    private String          comment;
    private String          lastModifiedBy;
    private Date            lastModifiedDate;

    public AttendanceRecord(AttendanceEvent e, String uId, Status s) {
        this.attendanceEvent    = e;
        this.userID             = uId;
        this.status             = s;
    }

}
