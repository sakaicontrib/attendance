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

package org.sakaiproject.attendance.tool.dataproviders;

import org.apache.wicket.model.IModel;
import org.sakaiproject.attendance.model.Event;
import org.sakaiproject.attendance.tool.models.DetachableEventModel;

import java.util.Collections;
import java.util.List;

/**
 * @author David Bauer ( dbauer1 at udayton dot edu)
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class EventDataProvider extends BaseProvider<Event> {
    public EventDataProvider() {
        super();
    }

    public EventDataProvider(List<Event> data) {
        super();
        if(data != null && !data.isEmpty()) {
            this.list = data;
        }
    }

    protected List<Event> getData() {
        if(this.list == null) {
            this.list = attendanceLogic.getEventsForCurrentSite();
            Collections.reverse(this.list);
        }
        return this.list;
    }

    @Override
    public IModel<Event> model(Event object){
        return new DetachableEventModel(object);
    }
}
