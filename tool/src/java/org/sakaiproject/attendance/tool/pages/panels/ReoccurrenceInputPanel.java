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

package org.sakaiproject.attendance.tool.pages.panels;

import com.google.ical.values.*;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.yui.calendar.DateField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.*;
import org.apache.wicket.util.string.Strings;
import org.sakaiproject.attendance.model.Event;
import org.sakaiproject.attendance.tool.pages.panels.models.RRuleInputModel;

import java.text.DateFormatSymbols;
import java.util.*;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class ReoccurrenceInputPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private IModel<RRuleInputModel> rRuleIIModel;
    private IModel<Event> event;
    private WebMarkupContainer weekly;
    private WebMarkupContainer monthly;

    private List<String> monthlyRepeatBy = Arrays.asList("day of the month", "day of the week");
    private List<String> frequencyNames = Arrays.asList("Daily", "Weekly", "Monthly", "Yearly");
    private List<Integer> minorFrequency = makeSequence(1,30);

    public  ReoccurrenceInputPanel(String id, IModel<Event> event, IModel<RRuleInputModel> rrule) {
        super(id, rrule);
        this.rRuleIIModel = rrule;
        this.event = event;
        setOutputMarkupPlaceholderTag(true);
        setVisible(false);

        add(createRRuleInputForm());
    }

    public RRule generateRRule(){
        RRule rRule = new RRule();
        RRuleInputModel rModel = this.rRuleIIModel.getObject();
        String frequencySelected = rModel.getFrequency();

        rRule.setInterval(rModel.getMinorFrequency());
        if(frequencySelected.equals("Daily")){
            rRule.setFreq(Frequency.DAILY);

        } else if (frequencySelected.equals("Weekly")) {
            rRule.setFreq(Frequency.WEEKLY);

            List<String> weekDayNames = getShortDayInWeekName();
            List<WeekdayNum> byDay = new ArrayList<WeekdayNum>();
            for(String item : rModel.getDaysOfWeek()){
                int dayPosition = weekDayNames.indexOf(item);
                byDay.add(new WeekdayNum(0, getDayOfWeek(dayPosition)));

            }
            rRule.setByDay(byDay);
        } else if (frequencySelected.equals("Monthly")) {
            rRule.setFreq(Frequency.MONTHLY);
            Event thisEvent = this.event.getObject();

            if(rModel.getMonthlyRepeatBy().equals("day of the month")){
                rRule.setByMonthDay(new int[]{thisEvent.getStartDateTime().getDay()});
            } else {
                List<WeekdayNum> byDay = new ArrayList<WeekdayNum>();

                byDay.add(new WeekdayNum(0, getDayOfWeek(thisEvent.getStartDateTime())));
                rRule.setByDay(byDay);
            }
        } else if (frequencySelected.equals("Yearly")) {
            rRule.setFreq(Frequency.YEARLY);
        }

        if(rModel.getEndCriteriaGroup().equals("endOccurrenceRadio")){
            Calendar c = Calendar.getInstance();
            c.setTime(rModel.getEndDate());
            int year = c.get(Calendar.YEAR);
            // c.get(Calendar.MONTH) is 0(Jan) to 11 (Dec). DateValueImpl expects 1-12
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            rRule.setUntil(new DateValueImpl(year, month, day));
        } else {
            rRule.setCount(rModel.getEndOccurrence());
        }
        return rRule;
    }

    private Form<RRuleInputModel> createRRuleInputForm() {
        Form<RRuleInputModel> rrule = new Form<RRuleInputModel>("reoccurrence", this.rRuleIIModel);

        createMainLabels(rrule);
        createWeekly(rrule);
        createMonthly(rrule);
        createEndCriteria(rrule);
        createMainValues(rrule);

        return rrule;
    }

    private void createMainLabels(Form<RRuleInputModel> rRule) {
        final Label frequencyLabel = new Label("labelFrequency", new ResourceModel("attendance.add.label.frequency"));
        final Label minorFrequencyLabel = new Label("labelMinorFrequency", new ResourceModel("attendance.add.label.minorFrequency"));
        final Label endCriteriaLabel = new Label("labelEndCriteria", new ResourceModel("attendance.add.label.end"));

        rRule.add(frequencyLabel);
        rRule.add(minorFrequencyLabel);
        rRule.add(endCriteriaLabel);
    }

    private void createMainValues(Form<RRuleInputModel> rRule) {

        final DropDownChoice<String> frequencyDropDownChoice = new DropDownChoice<String>("frequency", frequencyNames);
        frequencyDropDownChoice.setRequired(true);

        final AjaxFormComponentUpdatingBehavior frequencyComponentAjax = new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
                RRuleInputModel rmodel = rRuleIIModel.getObject();

                if(rmodel.getFrequency().equals(frequencyNames.get(0))){
                    weekly.setVisible(false);
                    monthly.setVisible(false);
                } else if (rmodel.getFrequency().equals(frequencyNames.get(1))){
                    weekly.setVisible(true);
                    monthly.setVisible(false);
                } else if (rmodel.getFrequency().equals(frequencyNames.get(2))){
                    weekly.setVisible(false);
                    monthly.setVisible(true);
                } else if (rmodel.getFrequency().equals(frequencyNames.get(3))){
                    weekly.setVisible(false);
                    monthly.setVisible(false);
                }

                ajaxRequestTarget.add(weekly);
                ajaxRequestTarget.add(monthly);
            }
        };

        frequencyDropDownChoice.add(frequencyComponentAjax);

        final DropDownChoice<Integer> minorFrequencyDropDownChoice = new DropDownChoice<Integer>("minorFrequency",  minorFrequency);
        minorFrequencyDropDownChoice.setRequired(true);

        rRule.add(frequencyDropDownChoice);
        rRule.add(minorFrequencyDropDownChoice);
    }

    // generate elements which are unique to the "weekly" frequency
    private void createWeekly(Form<RRuleInputModel> rRule) {
        // weekly items
        final CheckBoxMultipleChoice<String> daysOfWeek = new CheckBoxMultipleChoice<String>("daysOfWeek", getShortDayInWeekName());
        daysOfWeek.setSuffix("");
        daysOfWeek.setRequired(true);

        final Label daysOfWeekLabel = new Label("labelDaysOfWeek", new ResourceModel("attendance.add.label.daysOfWeek"));

        // container for settings specific to weekly reocurrence events
        this.weekly = new WebMarkupContainer("weekly");
        this.weekly.setOutputMarkupPlaceholderTag(true);
        this.weekly.setVisible(false);
        this.weekly.add(daysOfWeekLabel);
        this.weekly.add(daysOfWeek);

        rRule.add(this.weekly);
    }

    // unique elements to the monthly frequency
    private void createMonthly(Form<RRuleInputModel> rRule) {
        final Label monthlyRepeatByLabel = new Label("labelMonthlyRepeatBy", new ResourceModel("attendance.add.label.monthlyRepeatBy"));
        final RadioChoice<String> monthlyRadioChoice = new RadioChoice<String>("monthlyRepeatBy", monthlyRepeatBy);
        monthlyRadioChoice.setSuffix("");
        monthlyRadioChoice.setRequired(true);

        // container for settings specific to monthly reocurrence events
        this.monthly = new WebMarkupContainer("monthly");
        this.monthly.setOutputMarkupPlaceholderTag(true);
        this.monthly.setVisible(false);
        this.monthly.add(monthlyRepeatByLabel);
        this.monthly.add(monthlyRadioChoice);

        rRule.add(this.monthly);
    }

    private void createEndCriteria(Form<RRuleInputModel> rRule) {
        final Label endOccurrencePrefixLabel = new Label("labelEndOccurrencePrefix", new ResourceModel("attendance.add.label.endOccurrencePrefix"));
        final Label endOccurrenceSuffixLabel = new Label("labelEndOccurrenceSuffix", new ResourceModel("attendance.add.label.endOccurrenceSuffix"));
        final Radio endOccurrenceRadio = new Radio("endOccurrenceRadio", new Model<String>("endOccurrenceRadio"));
        endOccurrenceRadio.add(endOccurrencePrefixLabel);
        endOccurrenceRadio.add(endOccurrenceSuffixLabel);

        final Label endDateLabel = new Label("labelEndDate", new ResourceModel("attendance.add.label.endDate"));
        final Radio endDateRadio = new Radio("endDateRadio", new Model<String>("endDateRadio"));
        endDateRadio.add(endDateLabel);

        final RadioGroup<String> endCriteria = new RadioGroup<String>("endCriteriaGroup");
        endCriteria.setRequired(true);
        endCriteria.add(endOccurrenceRadio);
        endCriteria.add(endDateRadio);

        final DateField endDate = new DateField("endDate") {
            @Override
            public boolean isRequired() {
                return Strings.isEqual(endCriteria.getInput(), endDateRadio.getValue());
            }
        };
        endDateRadio.add(endDate);

        final NumberTextField<Integer> endOccurrences = new NumberTextField<Integer>("endOccurrence") {
            @Override
            public boolean isRequired(){
                return Strings.isEqual(endCriteria.getInput(), endOccurrenceRadio.getValue());
          }
        };
        endOccurrences.setMinimum(1);
        endOccurrences.setMaximum(31);
        endOccurrenceRadio.add(endOccurrences);

        rRule.add(endCriteria);
    }

    //  Returns a list of the names of the days of the week
    private List<String> getShortDayInWeekName() {
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(sakaiProxy.getCurrentUserLocale());
        ArrayList<String> shortWeekDays = new ArrayList(Arrays.asList(dateFormatSymbols.getShortWeekdays()));
        shortWeekDays.remove(0); // remove first element which is empty
        return shortWeekDays;
    }

    /*
     * Make a sequence of Integers
     * begin, the starting number (inclusive)
     * end, the ending number (inclusive)
     * List<Integer>
     */
    private List<Integer> makeSequence(int begin, int end) {
        List<Integer> ret = new ArrayList(end - begin + 1);

        for(int i = begin; i <= end; ret.add(i++));

        return ret;
    }

    /*
     * Get's the com.google.ical.values.Weekday day of the week
     * @param date
     * @return Weekday
     */
    private Weekday getDayOfWeek(Date date) {
        Weekday weekDay = null;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayPosition = c.get(Calendar.DAY_OF_WEEK);

        if(dayPosition == Weekday.SU.javaDayNum) {
            weekDay = Weekday.SU;
        } else if (dayPosition == Weekday.MO.javaDayNum) {
            weekDay = Weekday.MO;
        } else if (dayPosition == Weekday.TU.javaDayNum) {
            weekDay = Weekday.TU;
        } else if (dayPosition == Weekday.WE.javaDayNum) {
            weekDay = Weekday.WE;
        } else if (dayPosition == Weekday.TH.javaDayNum) {
            weekDay = Weekday.TH;
        } else if (dayPosition == Weekday.FR.javaDayNum) {
            weekDay = Weekday.FR;
        } else if (dayPosition == Weekday.SA.javaDayNum) {
            weekDay = Weekday.SA;
        }

        return weekDay;
    }

    /*
     *
     * @param dayPosition, 0 based index of the day of the week. 0 = Sunday
     * @return com.google.ical.values.Weekday
     */
    private Weekday getDayOfWeek(int dayPosition) {
        Weekday weekday = null;

        if(dayPosition == 0) {
            weekday = Weekday.SU;
        } else if (dayPosition == 1) {
            weekday = Weekday.MO;
        } else if (dayPosition == 2) {
            weekday = Weekday.TU;
        } else if (dayPosition == 3) {
            weekday = Weekday.WE;
        } else if (dayPosition == 4) {
            weekday = Weekday.TH;
        } else if (dayPosition == 5) {
            weekday = Weekday.FR;
        } else if (dayPosition == 6) {
            weekday = Weekday.SA;
        }

        return weekday;
    }
}

