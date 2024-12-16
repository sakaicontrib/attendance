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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.sakaiproject.attendance.util.AttendanceConstants;

import java.io.Serializable;
import java.util.*;

/**
 * An AttendanceSite represents all the Attendance related data for a specific Sakai Site.
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude="attendanceStatuses")
public class AttendanceSite implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;
	private String siteID;
	private Status defaultStatus;
	private Double maximumGrade;
	private Boolean isGradeShown;
	private Boolean sendToGradebook;
	private Integer gradingMethod;
	private String gradebookItemName;
	private Boolean showCommentsToStudents;
	private Boolean isSyncing;
	private Date syncTime;
	private Set<AttendanceStatus>	attendanceStatuses	= new HashSet<>(0);

	public AttendanceSite(String siteID){
		this.siteID = siteID;
		this.defaultStatus = Status.UNKNOWN;
		this.isGradeShown = false;
		this.sendToGradebook = false;
		this.gradingMethod = 0;
		this.gradebookItemName = AttendanceConstants.GRADEBOOK_ITEM_NAME;
		this.showCommentsToStudents = false;
		this.isSyncing = false;
	}

	public Boolean getSendToGradebook() {
		if(this.sendToGradebook == null) {
			return false;
		}

		return this.sendToGradebook;
	}

	public Boolean getIsSyncing() {
		if(this.isSyncing == null) {
			return false;
		}

		return this.isSyncing;
	}

	public Boolean getUseAutoGrading() {
        return this.gradingMethod != null;
    }

}
