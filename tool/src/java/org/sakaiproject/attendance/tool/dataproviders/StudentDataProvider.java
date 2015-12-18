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

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.logic.SakaiProxy;
import org.sakaiproject.attendance.tool.models.DetachableUserModel;
import org.sakaiproject.user.api.User;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author David Bauer ( dbauer1 at udayton dot edu)
 */
public class StudentDataProvider implements IDataProvider<User> {

    private List<User> list;

    @SpringBean(name="org.sakaiproject.attendance.logic.SakaiProxy")
    protected SakaiProxy sakaiProxy;

    public StudentDataProvider() {
        Injector.get().inject(this);
    }

    private List<User> getData() {
        if(list == null) {
            list = sakaiProxy.getCurrentSiteMembership();
            Collections.reverse(list);
        }
        return list;
    }


    @Override
    public Iterator<User> iterator(long first, long count){
        int f = (int) first; //not ideal but ok for demo
        int c = (int) count; //not ideal but ok for demo
        return getData().subList(f, f + c).iterator();
    }

    @Override
    public long size(){
        return getData().size();
    }

    @Override
    public IModel<User> model(User object){
        return new DetachableUserModel(object);
    }

    @Override
    public void detach(){
        list = null;
    }
}
