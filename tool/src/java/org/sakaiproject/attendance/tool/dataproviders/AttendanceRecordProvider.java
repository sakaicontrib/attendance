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

package org.sakaiproject.attendance.tool.dataproviders;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.tool.models.DetachableAttendanceRecordModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An AttendanceRecord Provider
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class AttendanceRecordProvider extends BaseProvider<AttendanceRecord> {

    public AttendanceRecordProvider() {
        super();
    }

    /**
     * A Constructor for a User's AttendanceRecords in the Current Site
     *
     * @param id, the User ID
     */
    public AttendanceRecordProvider(String id) {
        List<AttendanceRecord> records = attendanceLogic.getAttendanceRecordsForUser(id);
        if(!records.isEmpty()) {
            // don't think records will ever be empty
            this.list = records;
        }
    }

    /**
     * A Construct for AttendanceRecords from the provided AttendanceEvent
     *
     * @param aE, the AttendanceEvent
     */
    public AttendanceRecordProvider(AttendanceEvent aE) {
        super();
        if(aE != null) {
            aE = attendanceLogic.getAttendanceEvent(aE.getId());
            List<String> currentStudentIds = sakaiProxy.getCurrentSiteMembershipIds();
            this.list = new ArrayList<AttendanceRecord>();
            for(AttendanceRecord record: aE.getRecords()) {
                if(currentStudentIds.contains(record.getUserID())) {
                    this.list.add(record);
                }
            }
        }
    }

    /**
     * Constructor for AttendanceRecords for an AttendanceEvent for a specific group
     *
     * @param aE, the Attendance Event
     * @param groupId, the Group ID
     */
    public AttendanceRecordProvider(AttendanceEvent aE, String groupId) {
        super();
        if(aE != null) {
            List<String> currentStudentIds;
            if(groupId == null) {
                currentStudentIds = sakaiProxy.getCurrentSiteMembershipIds();
            } else {
                currentStudentIds = sakaiProxy.getGroupMembershipIdsForCurrentSite(groupId);
            }
            this.list = new ArrayList<>();
            for(AttendanceRecord record: aE.getRecords()) {
                if(currentStudentIds.contains(record.getUserID())) {
                    this.list.add(record);
                }
            }
        }
    }

    /**
     * Constructor with provided AttendanceRecord data
     *
     * @param data, set of AttendanceRecords
     */
    public AttendanceRecordProvider(Set<AttendanceRecord> data) {
        super();
        if(data != null && !data.isEmpty()) {
            this.list = new ArrayList<>(data);
        }
    }

    protected List<AttendanceRecord> getData() {
        if(this.list == null) {
            this.list = new ArrayList<>();
            Collections.reverse(this.list);
        }

        return this.list;
    }

    @Override
    public IModel<AttendanceRecord> model(AttendanceRecord object){
        if(object.getId() == null) {
            return new Model<>(object);
        }
        return Model.of(object);
    }
}
