package org.sakaiproject.attendance.tool.models;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.model.Event;

public class DetachableEventModel extends LoadableDetachableModel<Event> {

    @SpringBean(name="org.sakaiproject.attendance.logic.AttendanceLogic")
    protected AttendanceLogic attendanceLogic;

    private static final long serialVersionUID = 1L;
    private final long id;

    /**
     * @param t
     */
    public DetachableEventModel(Event t){
        this.id = t.getId();
    }

    /**
     * @param id
     */
    public DetachableEventModel(long id){
        this.id = id;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }

    /**
     * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
     *
     * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(final Object obj){
        if (obj == this){
            return true;
        }
        else if (obj == null){
            return false;
        }
        else if (obj instanceof DetachableEventModel) {
            DetachableEventModel other = (DetachableEventModel)obj;
            return other.id == id;
        }
        return false;
    }

    /**
     * @see org.apache.wicket.model.LoadableDetachableModel#load()
     */
    protected Event load(){
        Injector.get().inject(this);
        // get the thing
        return attendanceLogic.getEvent(id);
    }
}