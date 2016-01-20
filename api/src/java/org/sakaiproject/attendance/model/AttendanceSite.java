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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]au)
 *
 */
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSite implements Serializable {
	private static final 	long 		serialVersionUID = 1L;

	@Getter	@Setter	private 				Long 					id;
	@Getter	@Setter	private 				String 					siteID;
	@Getter	@Setter	private 				Status 					defaultStatus;
	@Getter	@Setter	private 				Set<AttendanceStatus>	activeStatuses		= new HashSet<AttendanceStatus>(0);

	public AttendanceSite(String siteID){
		this.siteID = siteID;
		this.defaultStatus = Status.UNKNOWN;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AttendanceSite that = (AttendanceSite) o;
		return Objects.equals(getId(), that.getId()) &&
				Objects.equals(getSiteID(), that.getSiteID()) &&
				getDefaultStatus() == that.getDefaultStatus() &&
				Objects.equals(getActiveStatuses(), that.getActiveStatuses());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
