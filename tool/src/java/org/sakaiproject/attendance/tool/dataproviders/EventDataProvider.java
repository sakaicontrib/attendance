package org.sakaiproject.attendance.tool.dataproviders;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.model.Event;
import org.sakaiproject.attendance.tool.models.DetachableEventModel;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class EventDataProvider implements IDataProvider<Event>, Serializable {

    private static final long serialVersionUID = 1L;
    private List<Event> list;

    @SpringBean(name="org.sakaiproject.attendance.logic.AttendanceLogic")
    protected AttendanceLogic attendanceLogic;

    public EventDataProvider() {
        Injector.get().inject(this);
    }

    private List<Event> getData() {
        if(list == null) {
            list = attendanceLogic.getEvents();
            Collections.reverse(list);
        }
        return list;
    }

    @Override
    public Iterator<Event> iterator(long first, long count){
        int f = (int) first; //not ideal but ok for demo
        int c = (int) count; //not ideal but ok for demo
        return getData().subList(f, f + c).iterator();
    }

    @Override
    public long size(){
        return getData().size();
    }

    @Override
    public IModel<Event> model(Event object){
        return new DetachableEventModel(object);
    }

    @Override
    public void detach(){
        list = null;
    }
}