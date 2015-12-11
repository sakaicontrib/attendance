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
