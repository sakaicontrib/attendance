package org.sakaiproject.attendance.tool.util;

import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;

public abstract class ConfirmationLink<T> extends AjaxLink<T>
{
    private static final long serialVersionUID = 1L;
    private final String text;

    public ConfirmationLink( String id, String text )
    {
        super( id );
        this.text = text;
    }

    @Override
    protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
    {
        super.updateAjaxAttributes( attributes );

        AjaxCallListener ajaxCallListener = new AjaxCallListener();
        ajaxCallListener.onPrecondition( "return confirm('" + text + "');" );
        attributes.getAjaxCallListeners().add( ajaxCallListener );
    }
}
