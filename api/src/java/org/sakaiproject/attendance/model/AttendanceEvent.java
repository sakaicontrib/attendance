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
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]au)
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEvent implements Serializable {
	private static final 	long 		serialVersionUID = 1L;

	private 				Long 					id;
	private 				String 					name;
	private 				Date 					startDateTime;
	private 				Date	 				endDateTime;
	private 				Boolean 				isReoccurring;
	private 				Long 					reoccurringID;
	private 				Boolean 				isRequired;
	private 				String					releasedTo;
	private 				AttendanceSite 			attendanceSite;
	private 				String 					location;
	private 				Set<AttendanceRecord> 	records = new HashSet<AttendanceRecord>(0);

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

	public Map<Status, Integer> getStats() {
		Map<Status, Integer> results = new HashMap<Status, Integer>();

		if(this.records.isEmpty()){
			for(Status s : Status.values()){
				generateStatsHelper(results, s, 0);
			}

			return results;
		}

		for(AttendanceRecord r : this.records) {
			for(Status s : Status.values()){
				if(r.getStatus() == s) {
					generateStatsHelper(results, s, 1);
				}
			}
		}

		return results;
	}

	private void generateStatsHelper(Map<Status, Integer> m, Status s, int base) {
		if(m.containsKey(s)) {
			m.put(s, m.get(s) + 1);
		} else {
			m.put(s, base);
		}
	}
}
