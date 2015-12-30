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
import org.sakaiproject.attendance.model.StatusRecord;
import org.sakaiproject.attendance.tool.models.DetachableStatusRecordModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class StatusRecordProvider extends BaseProvider<StatusRecord> {
    public StatusRecordProvider() {
        super();
    }

    protected List<StatusRecord> getData() {
        if(this.list == null) {
            this.list = new ArrayList<StatusRecord>();
            Collections.reverse(this.list);
        }

        return this.list;
    }

    @Override
    public IModel<StatusRecord> model(StatusRecord object){
        return new DetachableStatusRecordModel(object);
    }
}
