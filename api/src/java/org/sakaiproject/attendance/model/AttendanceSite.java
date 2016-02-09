/*
 *  Copyright (c) 2016, University of Dayton
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
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSite implements Serializable {
	private static final 					long 					serialVersionUID 	= 1L;

	@Getter	@Setter	private 				Long 					id;
	@Getter	@Setter	private 				String 					siteID;
	@Getter	@Setter	private 				Status 					defaultStatus;
	@Getter @Setter private					Double					maximumGrade;
	@Getter @Setter private					Boolean					isGradeShown;
	@Getter @Setter private					Boolean					sendToGradebook;
	@Getter @Setter private					String					gradebookItemName;
	@Getter @Setter private					Boolean					showCommentsToStudents;
	@Getter	@Setter	private 				Set<AttendanceStatus>	attendanceStatuses	= new HashSet<AttendanceStatus>(0);

	public AttendanceSite(String siteID){
		this.siteID 				= siteID;
		this.defaultStatus 			= Status.UNKNOWN;
		this.isGradeShown 			= false;
		this.sendToGradebook 		= false;
		this.gradebookItemName 		= AttendanceConstants.GRADEBOOK_ITEM_NAME;
		this.showCommentsToStudents = false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AttendanceSite that = (AttendanceSite) o;
		return Objects.equals(getId(), that.getId()) &&
				Objects.equals(getSiteID(), that.getSiteID()) &&
				getDefaultStatus() == that.getDefaultStatus() &&
				Objects.equals(getMaximumGrade(), that.getMaximumGrade()) &&
				Objects.equals(getIsGradeShown(), that.getIsGradeShown()) &&
				Objects.equals(getSendToGradebook(), that.getSendToGradebook()) &&
				Objects.equals(getGradebookItemName(), that.getGradebookItemName()) &&
				Objects.equals(getShowCommentsToStudents(), that.getShowCommentsToStudents()) &&
				Objects.equals(getAttendanceStatuses(), that.getAttendanceStatuses());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
