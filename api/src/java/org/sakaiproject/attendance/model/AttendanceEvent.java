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

import lombok.*;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]au)
 *
 */
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEvent implements Serializable {
	private static final 	long 		serialVersionUID = 1L;

	@Getter	@Setter	private 				Long 					id;
	@Getter @Setter private 				String 					name;
	@Getter @Setter private 				Date 					startDateTime;
	@Getter @Setter private 				Date	 				endDateTime;
	@Getter @Setter private 				Boolean 				isReoccurring;
	@Getter @Setter private 				Long 					reoccurringID;
	@Getter @Setter private 				Boolean 				isRequired;
	@Getter @Setter private 				String					releasedTo;
	@Getter @Setter private 				AttendanceSite 			attendanceSite;
	@Getter @Setter private 				String 					location;
	@Getter @Setter private 				Set<AttendanceRecord> 	records = new HashSet<AttendanceRecord>(0);

	// Create a copy constructor
	public AttendanceEvent(AttendanceEvent attendanceEvent){
		this.name 			= attendanceEvent.name;
		this.startDateTime 	= attendanceEvent.startDateTime;
		this.endDateTime 	= attendanceEvent.endDateTime;
		this.isReoccurring 	= attendanceEvent.isReoccurring;
		this.reoccurringID 	= attendanceEvent.reoccurringID;
		this.isRequired 	= attendanceEvent.isRequired;
		this.releasedTo 	= attendanceEvent.releasedTo;
		this.attendanceSite = attendanceEvent.attendanceSite;
		this.location 		= attendanceEvent.location;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AttendanceEvent that = (AttendanceEvent) o;
		return Objects.equals(id, that.id) &&
				Objects.equals(name, that.name) &&
				Objects.equals(startDateTime, that.startDateTime) &&
				Objects.equals(endDateTime, that.endDateTime) &&
				Objects.equals(isReoccurring, that.isReoccurring) &&
				Objects.equals(reoccurringID, that.reoccurringID) &&
				Objects.equals(isRequired, that.isRequired) &&
				Objects.equals(releasedTo, that.releasedTo) &&
				Objects.equals(attendanceSite, that.attendanceSite) &&
				Objects.equals(location, that.location) &&
				Objects.equals(records, that.records);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
