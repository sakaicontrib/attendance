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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Slf4j
public class AttendanceStats implements Serializable {
    private static final    long            serialVersionUID    = 1L;

    @Getter
    private                 int             present             = 0;
    @Getter
    private                 int             unexcused           = 0;
    @Getter
    private                 int             excused             = 0;
    @Getter
    private                 int             late                = 0;
    @Getter
    private                 int             leftEarly           = 0;

    public void setPresent(int present) {
        if(present < 0) {
            present = 0;
            logDebugSetNegative();
        }
        this.present = present;
    }

    public void setUnexcused(int unexcused) {
        if(unexcused < 0) {
            unexcused = 0;
            logDebugSetNegative();
        }
        this.unexcused = unexcused;
    }

    public void setExcused(int excused) {
        if(excused < 0) {
            excused = 0;
            logDebugSetNegative();
        }
        this.excused = excused;
    }

    public void setLate(int late) {
        if(late < 0) {
            late = 0;
            logDebugSetNegative();
        }
        this.late = late;
    }

    public void setLeftEarly(int leftEarly) {
        if(leftEarly < 0) {
            leftEarly = 0;
            logDebugSetNegative();
        }
        this.leftEarly = leftEarly;
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj == null) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        if(obj.getClass() != getClass()) {
            return false;
        }

        final AttendanceStats other = (AttendanceStats) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.present, other.present)
                .append(this.unexcused, other.unexcused)
                .append(this.excused, other.excused)
                .append(this.late, other.late)
                .append(this.leftEarly, other.leftEarly)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.present)
                .append(this.unexcused)
                .append(this.excused)
                .append(this.late)
                .append(this.leftEarly)
                .toHashCode();
    }

    private void logDebugSetNegative() {
        log.debug("AttendanceStats are a counter. Negative values do not make sense. Attempting to set stats value to negative. Bad.");
    }
}
