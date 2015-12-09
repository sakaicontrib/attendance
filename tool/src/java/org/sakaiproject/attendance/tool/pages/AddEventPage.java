package org.sakaiproject.attendance.tool.pages;

import java.util.*;

import com.google.ical.values.*;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.yui.calendar.DateField;
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
	private List<Integer> minorFrequency = makeSequence(1,30);
	private Integer minorFrequencySelected = 0;
	private List<String> monthlyRepeatBy = Arrays.asList("day of the month", "day of the week");
	private String monthlyRepeatBySelection = "";
	private Integer endOccurrenceInput;
	private Date endDate;
	private Event newEvent = new Event();
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
		//final Event newEvent = new Event();
		newEvent.setIsReoccurring(false);

		// weekly items
		final CheckBoxMultipleChoice<String> daysOfWeek = new CheckBoxMultipleChoice<String>("daysOfWeek", new PropertyModel<ArrayList<String>>(this, "daysSelected"), getShortDayInWeekName());
		daysOfWeek.setSuffix("");

		final Label daysOfWeekLabel = new Label("labelDaysOfWeek", new ResourceModel("attendance.add.label.daysOfWeek"));

		// container for settings specific to weekly reocurrence events
		final WebMarkupContainer weeklyContainer = new WebMarkupContainer("weeklyContainer");
		weeklyContainer.setOutputMarkupPlaceholderTag(true);
		weeklyContainer.setVisible(false);
		weeklyContainer.add(daysOfWeekLabel);
		weeklyContainer.add(daysOfWeek);

		Label monthlyRepeatByLabel = new Label("labelMonthlyRepeatBy", new ResourceModel("attendance.add.label.monthlyRepeatBy"));
		RadioChoice<String> monthlyRadioChoice = new RadioChoice<String>("monthlyRepeatBy", new PropertyModel<String>(this, "monthlyRepeatBySelection"), monthlyRepeatBy);
		monthlyRadioChoice.setSuffix("");

		// container for settings specific to monthly reocurrence events
		final WebMarkupContainer monthlyContainer = new WebMarkupContainer("monthlyContainer");
		monthlyContainer.setOutputMarkupPlaceholderTag(true);
		monthlyContainer.setVisible(false);
		monthlyContainer.add(monthlyRepeatByLabel);
		monthlyContainer.add(monthlyRadioChoice);


		Label endOccurrencePrefixLabel = new Label("labelEndOccurrencePrefix", new ResourceModel("attendance.add.label.endOccurrencePrefix"));
		Label endOccurrenceSuffixLabel = new Label("labelEndOccurrenceSuffix", new ResourceModel("attendance.add.label.endOccurrenceSuffix"));
		NumberTextField<Integer> endOccurrences = new NumberTextField<Integer>("endOccurrence", new PropertyModel<Integer>(this, "endOccurrenceInput"));
		endOccurrences.setMinimum(1);
		endOccurrences.setMaximum(365);

		Radio endOccurrenceRadio = new Radio("endOccurrenceRadio");
		endOccurrenceRadio.add(endOccurrencePrefixLabel);
		endOccurrenceRadio.add(endOccurrences);
		endOccurrenceRadio.add(endOccurrenceSuffixLabel);

		Label endDateLabel = new Label("labelEndDate", new ResourceModel("attendance.add.label.endDate"));
		DateField endDate = new DateField("endDate", new PropertyModel<Date>(this, "endDate"));

		Radio endDateRadio = new Radio("endDateRadio");
		endDateRadio.add(endDateLabel);
		endDateRadio.add(endDate);

		RadioGroup<Radio> endCriteria = new RadioGroup<Radio>("endCriteriaGroup");
		endCriteria.add(endOccurrenceRadio);
		endCriteria.add(endDateRadio);

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

		final Label nameLabel = new Label("labelName", new ResourceModel("attendance.add.label.name"));
		final Label startDateTimeLabel = new Label("labelStartDateTime", new ResourceModel("attendance.add.label.startDateTime"));
		final Label endDateTimeLabel = new Label("labelEndDateTime", new ResourceModel("attendance.add.label.endDateTime"));
		final Label isRequiredLabel = new Label("labelIsRequired", new ResourceModel("attendance.add.label.isRequired"));
		final Label locationLabel = new Label("labelLocation", new ResourceModel("attendance.add.label.location"));
		final Label releasedToLabel = new Label("labelReleasedTo", new ResourceModel("attendance.add.label.releasedTo"));
		final Label isReoccurringLabel = new Label("labelIsReoccurring", new ResourceModel("attendance.add.label.isReoccurring"));

		final DropDownChoice<String> frequencyDropDownChoice = new DropDownChoice<String>("frequency", new PropertyModel<String>(this,"frequencySelected"), frequencyNames);

		final AjaxFormComponentUpdatingBehavior frequencyComponentAjax = new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
				if(frequencySelected.equals(frequencyNames.get(0))){
					weeklyContainer.setVisible(false);
					monthlyContainer.setVisible(false);
				} else if (frequencySelected.equals(frequencyNames.get(1))){
					weeklyContainer.setVisible(true);
					monthlyContainer.setVisible(false);
				} else if (frequencySelected.equals(frequencyNames.get(2))){
					weeklyContainer.setVisible(false);
					monthlyContainer.setVisible(true);
				} else if (frequencySelected.equals(frequencyNames.get(3))){
					weeklyContainer.setVisible(false);
					monthlyContainer.setVisible(false);
				}

				ajaxRequestTarget.add(weeklyContainer);
				ajaxRequestTarget.add(monthlyContainer);
			}
		};

		frequencyDropDownChoice.add(frequencyComponentAjax);

		final Label frequencyLabel = new Label("labelFrequency", new ResourceModel("attendance.add.label.frequency"));

		final DropDownChoice<Integer> minorFrequencyDropDownChoice = new DropDownChoice<Integer>("minorFrequency", new PropertyModel(this, "minorFrequencySelected"), minorFrequency);

		final Label minorFrequencyLabel = new Label("labelMinorFrequency", new ResourceModel("attendance.add.label.minorFrequency"));

		Label endCriteriaLabel = new Label("labelEndCriteria", new ResourceModel("attendance.add.label.end"));

        //add our form
		Form form = new Form("form");
		EventForm eventForm = new EventForm("eventForm", newEvent);
		eventForm.add(isReoccurringAjaxCheckBox);
		eventForm.add(nameLabel);
		eventForm.add(startDateTimeLabel);
		eventForm.add(endDateTimeLabel);
		eventForm.add(isRequiredLabel);
		eventForm.add(locationLabel);
		eventForm.add(releasedToLabel);
		eventForm.add(isReoccurringLabel);

		ReoccurrenceForm reoccurrenceForm = new ReoccurrenceForm("reoccurrenceForm");
		reoccurrenceForm.add(frequencyLabel);
		reoccurrenceForm.add(frequencyDropDownChoice);
		reoccurrenceForm.add(minorFrequencyLabel);
		reoccurrenceForm.add(minorFrequencyDropDownChoice);
		reoccurrenceForm.add(monthlyContainer);
		reoccurrenceForm.add(weeklyContainer);
		reoccurrenceForm.add(endCriteriaLabel);
		reoccurrenceForm.add(endCriteria);

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
		public ReoccurrenceForm(String id) {
			super(id);
		}

		public RRule generateReoccurenceString(){
			RRule rRule = new RRule();

			rRule.setInterval(minorFrequencySelected);
			if(frequencySelected.equals("Daily")){
				rRule.setFreq(Frequency.DAILY);

			} else if (frequencySelected.equals("Weekly")) {
				rRule.setFreq(Frequency.WEEKLY);

				List<String> weekDayNames = getShortDayInWeekName();
				List<WeekdayNum> byDay = new ArrayList<WeekdayNum>();
				for(String item : daysSelected){
					int dayPosition = weekDayNames.indexOf(item);
					if(dayPosition == 0) {
						byDay.add(new WeekdayNum(0, Weekday.SU));
					} else if (dayPosition == 1) {
						byDay.add(new WeekdayNum(0, Weekday.MO));
					} else if (dayPosition == 2) {
						byDay.add(new WeekdayNum(0, Weekday.TU));
					} else if (dayPosition == 3) {
						byDay.add(new WeekdayNum(0, Weekday.WE));
					} else if (dayPosition == 4) {
						byDay.add(new WeekdayNum(0, Weekday.TH));
					} else if (dayPosition == 5) {
						byDay.add(new WeekdayNum(0, Weekday.FR));
					} else if (dayPosition == 6) {
						byDay.add(new WeekdayNum(0, Weekday.SA));
					}
				}
				rRule.setByDay(byDay);
			} else if (frequencySelected.equals("Monthly")) {
				rRule.setFreq(Frequency.MONTHLY);

				if(monthlyRepeatBySelection.equals("day of the month")){
					rRule.setByMonthDay(new int[]{newEvent.getStartDateTime().getDay()});
				} else {
					Weekday weekDay = null;

					Calendar c = Calendar.getInstance();
					c.setTime(newEvent.getStartDateTime());
					int dayPosition = c.get(Calendar.DAY_OF_WEEK);

					if(dayPosition == 0) {
						weekDay = Weekday.SU;
					} else if (dayPosition == 1) {
						weekDay = Weekday.MO;
					} else if (dayPosition == 2) {
						weekDay = Weekday.TU;
					} else if (dayPosition == 3) {
						weekDay = Weekday.WE;
					} else if (dayPosition == 4) {
						weekDay = Weekday.TH;
					} else if (dayPosition == 5) {
						weekDay = Weekday.FR;
					} else if (dayPosition == 6) {
						weekDay = Weekday.SA;
					}

					List<WeekdayNum> byDay = new ArrayList<WeekdayNum>();

					byDay.add(new WeekdayNum(0, weekDay));
					rRule.setByDay(byDay);
				}
			} else if(frequencySelected.equals("Yearly")) {
				rRule.setFreq(Frequency.YEARLY);
			}

			if(endOccurrenceInput == null){
				rRule.setUntil(new DateValueImpl(endDate.getYear(), endDate.getMonth(), endDate.getDay()));
			} else {
				rRule.setCount(endOccurrenceInput);
			}
			return rRule;
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

	protected List<Integer> makeSequence(int begin, int end) {
		List<Integer> ret = new ArrayList(end - begin + 1);

		for(int i = begin; i <= end; ret.add(i++));

		return ret;
	}
}
