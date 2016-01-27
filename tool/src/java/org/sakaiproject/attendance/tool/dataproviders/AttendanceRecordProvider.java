/*
 *  Copyright (c) 2016, The Apereo Foundation
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
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.tool.models.DetachableAttendanceRecordModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class AttendanceRecordProvider extends BaseProvider<AttendanceRecord> {

    public AttendanceRecordProvider() {
        super();
    }

    public AttendanceRecordProvider(String id) {
        List<AttendanceRecord> records = attendanceLogic.getAttendanceRecordsForUser(id);
        if(!records.isEmpty()) {
            // don't think records will ever be empty
            this.list = records;
        }
    }

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

    public AttendanceRecordProvider(AttendanceEvent aE, String groupId) {
        super();
        if(aE != null) {
            aE = attendanceLogic.getAttendanceEvent(aE.getId());
            List<String> currentStudentIds;
            if(groupId == null) {
                currentStudentIds = sakaiProxy.getCurrentSiteMembershipIds();
            } else {
                currentStudentIds = sakaiProxy.getGroupMembershipIdsForCurrentSite(groupId);
            }
            this.list = new ArrayList<AttendanceRecord>();
            for(AttendanceRecord record: aE.getRecords()) {
                if(currentStudentIds.contains(record.getUserID())) {
                    this.list.add(record);
                }
            }
        }
    }

    public AttendanceRecordProvider(Set<AttendanceRecord> data) {
        super();
        if(data != null && !data.isEmpty()) {
            this.list = new ArrayList<AttendanceRecord>(data);
        }
    }

    protected List<AttendanceRecord> getData() {
        if(this.list == null) {
            this.list = new ArrayList<AttendanceRecord>();
            Collections.reverse(this.list);
        }

        return this.list;
    }

    @Override
    public IModel<AttendanceRecord> model(AttendanceRecord object){
        return new DetachableAttendanceRecordModel(object);
    }
}
