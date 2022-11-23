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

package org.sakaiproject.attendance.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Holds the records that are being changed by the Import function.
 *
 * Created by james on 6/9/17.
 */
@Data
@EqualsAndHashCode(of = {"id","attendanceEvent","userID","status","comment","oldComment","oldStatus"})
@NoArgsConstructor
@AllArgsConstructor
public class ImportConfirmList implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private AttendanceEvent attendanceEvent;
    private AttendanceRecord attendanceRecord;
    private AttendanceSite attendanceSite;
    private String userID;
    private Status status;
    private Status oldStatus;
    private String comment;
    private String oldComment;
    private String eventName;
    private String eventDate;
}
