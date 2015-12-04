package org.sakaiproject.attendance.tool.pages;

import java.util.*;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.DefaultItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.*;

import org.sakaiproject.attendance.model.Event;
import org.sakaiproject.attendance.model.Reoccurrence;

/**
 * An example page. This interacts with a list of items from the database
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class AddEventPage extends BasePage {

	private ArrayList<String> daysSelected = new ArrayList<String>();
	private String frequencySelected = "";
	private List<String> frequencyNames = Arrays.asList("Daily", "Weekly", "Monthly", "Yearly");
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

		// Create Event to be Added
		final Event newEvent = new Event();
		newEvent.setIsReoccurring(false);

		// container for settings specific to daily reocurrence events
		final WebMarkupContainer dailyContainer = new WebMarkupContainer("dailyContainer");
		dailyContainer.setOutputMarkupPlaceholderTag(true);
		dailyContainer.setVisible(true);

		// container for settings specific to weekly reocurrence events
		final WebMarkupContainer weeklyContainer = new WebMarkupContainer("weeklyContainer");
		weeklyContainer.setOutputMarkupPlaceholderTag(true);
		weeklyContainer.setVisible(false);
		weeklyContainer.add((new CheckBoxMultipleChoice<String>("reoccurrence-daysOfWeek", new PropertyModel<ArrayList<String>>(this, "daysSelected"), getShortDayInWeekName())).setSuffix(""));


		// container for settings specific to monthly reocurrence events
		final WebMarkupContainer monthlyContainer = new WebMarkupContainer("monthlyContainer");
		monthlyContainer.setOutputMarkupPlaceholderTag(true);
		monthlyContainer.setVisible(false);

		// container for settings specific to yearly reocurrence events
		final WebMarkupContainer yearlyContainer = new WebMarkupContainer("yearlyContainer");
		yearlyContainer.setOutputMarkupPlaceholderTag(true);
		yearlyContainer.setVisible(false);

		// generate Container for all Reoccurrence Settings
		final WebMarkupContainer reoccurrenceContainer = new WebMarkupContainer("reoccurrenceContainer");
		reoccurrenceContainer.setOutputMarkupPlaceholderTag(true);
		reoccurrenceContainer.setOutputMarkupId(true);
		reoccurrenceContainer.setVisible(false);

		//AjaxCheckBox
		AjaxCheckBox isReoccurringAjaxCheckBox = new AjaxCheckBox("isReoccurring", new PropertyModel(newEvent, "isReoccurring")) {
			@Override
			protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
				reoccurrenceContainer.setVisible(newEvent.getIsReoccurring());
				ajaxRequestTarget.add(reoccurrenceContainer);
			}
		};

		final DropDownChoice<String> dropDownChoice = new DropDownChoice<String>("frequency", new PropertyModel<String>(this,"frequencySelected"), frequencyNames);

		final AjaxFormComponentUpdatingBehavior frequencyComponentAjax = new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
				if(frequencySelected.equals(frequencyNames.get(0))){
					dailyContainer.setVisible(true);
					weeklyContainer.setVisible(false);
					monthlyContainer.setVisible(false);
					yearlyContainer.setVisible(false);
				} else if (frequencySelected.equals(frequencyNames.get(1))){
					dailyContainer.setVisible(false);
					weeklyContainer.setVisible(true);
					monthlyContainer.setVisible(false);
					yearlyContainer.setVisible(false);
				} else if (frequencySelected.equals(frequencyNames.get(2))){
					dailyContainer.setVisible(false);
					weeklyContainer.setVisible(false);
					monthlyContainer.setVisible(true);
					yearlyContainer.setVisible(false);
				} else if (frequencySelected.equals(frequencyNames.get(3))){
					dailyContainer.setVisible(false);
					weeklyContainer.setVisible(false);
					monthlyContainer.setVisible(false);
					yearlyContainer.setVisible(true);
				}

				ajaxRequestTarget.add(dailyContainer);
				ajaxRequestTarget.add(weeklyContainer);
				ajaxRequestTarget.add(monthlyContainer);
				ajaxRequestTarget.add(yearlyContainer);
			}
		};

		dropDownChoice.add(frequencyComponentAjax);


        //add our form
		Form form = new Form("form");
		EventForm eventForm = new EventForm("eventForm", newEvent);
		eventForm.add(isReoccurringAjaxCheckBox);
		ReoccurrenceForm reoccurrenceForm = new ReoccurrenceForm("reoccurrenceForm", new Reoccurrence());
		reoccurrenceForm.add(dropDownChoice);
		reoccurrenceForm.add(dailyContainer);
		reoccurrenceForm.add(monthlyContainer);
		reoccurrenceForm.add(weeklyContainer);
		reoccurrenceForm.add(yearlyContainer);

		reoccurrenceContainer.add(reoccurrenceForm);

		form.add(eventForm);
		form.add(reoccurrenceContainer);
        add(form);
        
	}
	
	/**
	 * Form for adding a new Event. It is automatically linked up if the form fields match the object fields
	 */
	private class EventForm extends Form {
	   
		public EventForm(String id, Event event) {
	        super(id, new CompoundPropertyModel<Event>(event));
			add(new TextField("name"));
			add(new DateTimeField("startDateTime"));
			add(new DateTimeField("endDateTime"));
			add(new CheckBox("isRequired"));
			add(new TextField("location"));
			add(new TextField("releasedTo"));
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

	private class ReoccurrenceForm extends Form {
		public ReoccurrenceForm(String id, Reoccurrence reoccurrence) {
			super(id, new CompoundPropertyModel(reoccurrence));
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
