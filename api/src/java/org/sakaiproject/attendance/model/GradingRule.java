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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author David P. Bauer [dbauer1 (at) udayton (dot) edu]
 */
@NoArgsConstructor
@AllArgsConstructor
public class GradingRule implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter @Setter private Long id;
    @Getter @Setter private AttendanceSite attendanceSite;
    @Getter @Setter private Status status;
    @Getter @Setter private Integer startRange;
    @Getter @Setter private Integer endRange;
    @Getter @Setter private Double points;

    public GradingRule(AttendanceSite attendanceSite) {
        this.attendanceSite = attendanceSite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GradingRule)) return false;

        GradingRule that = (GradingRule) o;

        return getAttendanceSite().equals(that.getAttendanceSite()) &&
                getStatus() == that.getStatus() &&
                getStartRange().equals(that.getStartRange()) &&
                (getEndRange() != null ? getEndRange().equals(that.getEndRange()) : that.getEndRange() == null && getPoints().equals(that.getPoints()));
    }

    @Override
    public int hashCode() {
        int result = getAttendanceSite().hashCode();
        result = 31 * result + getStatus().hashCode();
        result = 31 * result + getStartRange().hashCode();
        result = 31 * result + (getEndRange() != null ? getEndRange().hashCode() : 0);
        result = 31 * result + getPoints().hashCode();
        return result;
    }
}
