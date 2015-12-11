package org.sakaiproject.attendance.tool.pages;

import java.util.*;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.DefaultItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.*;

import org.sakaiproject.attendance.model.Event;
import org.sakaiproject.attendance.tool.pages.panels.EventInputPanel;

/**
 * An example page. This interacts with a list of items from the database
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class AddEventPage extends BasePage {
	EventsDataProvider provider;
	
	public AddEventPage() {
		disableLink(addEventLink);
		
		//get list of items from db, wrapped in a dataprovider
		provider = new EventsDataProvider();
		
		//present the data in a table
		final DataView<Event> dataView = new DataView<Event>("simple", provider) {

			@Override
			public void populateItem(final Item item) {
                final Event event = (Event) item.getModelObject();
                item.add(new Label("name", event.getName()));
				item.add(new Label("startDateTime", event.getStartDateTime()));
				item.add(new Label("endDateTime", event.getEndDateTime()));
				item.add(new Label("isReoccurring", event.getIsReoccurring()));
				item.add(new Label("isRequired", event.getIsRequired()));
				item.add(new Label("location", event.getLocation()));
				item.add(new Label("releasedTo", event.getReleasedTo()));
            }
        };
        dataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
        dataView.setItemsPerPage(5);
        add(dataView);

        //add a pager to our table, only visible if we have more than 5 items
        add(new PagingNavigator("navigator", dataView) {
        	
        	@Override
        	public boolean isVisible() {
        		if(provider.size() > 5) {
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public void onBeforeRender() {
        		super.onBeforeRender();
        		
        		//clear the feedback panel messages
        		clearFeedback(feedbackPanel);
        	}
        });

		//add our form
		Form form = new Form("form");
		form.add(new EventInputPanel("event", new CompoundPropertyModel<Event>(new Event())));
		form.add(new SubmitLink(("submit")){
			public void onSubmit() {
				int x = 4+4;
			}
		});
        add(form);
        
	}
	
	/**
	 * Form for adding a new Event. It is automatically linked up if the form fields match the object fields
	 */
	private class EventForm extends Form<Event> {
	   
		public EventForm(String id, Event event) {
	        super(id, new CompoundPropertyModel<Event>(event));

	    }
		
		@Override
        public void onSubmit(){
			Event t = (Event)getDefaultModelObject();

			if(attendanceLogic.addEvent(t)){
				info("Item added");
			} else {
				error("Error adding item");
			}
        }
	}

	
	/**
	 * DataProvider to manage our list
	 * 
	 */
	private class EventsDataProvider implements IDataProvider<Event> {
	   
		private List<Event> list;
		
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
	
	/**
	 * Detachable model to wrap a Event
	 * 
	 */
	private class DetachableEventModel extends LoadableDetachableModel<Event>{

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
			
			// get the thing
			return attendanceLogic.getEvent(id);
		}
	}

}
