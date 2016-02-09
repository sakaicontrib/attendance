/*
 *  Copyright (c) 2016, University of Dayton
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

package org.sakaiproject.attendance.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.logic.SakaiProxy;
import org.sakaiproject.attendance.model.Status;

import javax.servlet.http.HttpServletRequest;


/**
 * This is our base page for our Sakai app. It sets up the containing markup and top navigation.
 * All top level pages should extend from this page so as to keep the same navigation. The content for those pages will
 * be rendered in the main area below the top nav.
 * 
 * <p>It also allows us to setup the API injection and any other common methods, which are then made available in the other pages.
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class BasePage extends WebPage implements IHeaderContributor {
	protected static final Logger log = Logger.getLogger(BasePage.class);
	
	@SpringBean(name="org.sakaiproject.attendance.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.attendance.logic.AttendanceLogic")
	protected AttendanceLogic attendanceLogic;

	protected String role;
	
	Link<Void> firstLink;
	Link<Void> settingsLink;
	Link<Void> addEventLink;
	Link<Void> studentOverviewLink;
	
	FeedbackPanel feedbackPanel;
	
	public BasePage() {
		
		log.debug("BasePage()");

		this.role = sakaiProxy.getCurrentUserRoleInCurrentSite();

    	//Take Attendance Overview link
		firstLink = new Link<Void>("firstLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				
				setResponsePage(new Overview());
			}
		};
		firstLink.add(new Label("firstLinkLabel",new ResourceModel("link.first")).setRenderBodyOnly(true));
		firstLink.add(new AttributeModifier("title", new ResourceModel("link.first.tooltip")));
		add(firstLink);
		
		//Attendance Items link
		addEventLink = new Link<Void>("addEventLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new AddEventPage());
			}
		};
		addEventLink.add(new Label("thirdLinkLabel", new ResourceModel("link.third")).setRenderBodyOnly(true));
		addEventLink.add(new AttributeModifier("title", new ResourceModel("link.third.tooltip")));
		add(addEventLink);

		//student Overview Link
		studentOverviewLink = new Link<Void>("student-overview-link") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new StudentOverview());
			}
		};
		studentOverviewLink.add(new Label("student-overview-label", new ResourceModel("attendance.link.student.overview")).setRenderBodyOnly(true));
		studentOverviewLink.add(new AttributeModifier("title", new ResourceModel("attendance.link.student.overview.tooltip")));
		add(studentOverviewLink);

		settingsLink = new Link<Void>("settings-link") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new SettingsPage());
			}
		};
		settingsLink.add(new Label("settings-link-label", new ResourceModel("settings.link.label")).setRenderBodyOnly(true));
		settingsLink.add(new AttributeModifier("title", new ResourceModel("settings.link.tooltip")));
		add(settingsLink);
		
		// Add a FeedbackPanel for displaying our messages
        feedbackPanel = new FeedbackPanel("feedback"){
        	
        	@Override
        	protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
        		final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

        		if(message.getLevel() == FeedbackMessage.ERROR ||
        			message.getLevel() == FeedbackMessage.DEBUG ||
        			message.getLevel() == FeedbackMessage.FATAL ||
        			message.getLevel() == FeedbackMessage.WARNING){
        			add(AttributeModifier.replace("class", "alertMessage"));
        		} else if(message.getLevel() == FeedbackMessage.INFO){
        			add(AttributeModifier.replace("class", "success"));        			
        		} 

        		return newMessageDisplayComponent;
        	}
        };
		feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
		
    }
	
	/**
	 * Helper to clear the feedbackpanel display.
	 * @param f	FeedBackPanel
	 */
	public void clearFeedback(FeedbackPanel f) {
		if(!f.hasFeedbackMessage()) {
			f.add(AttributeModifier.replace("class", ""));
		}
	}
	
	/**
	 * This block adds the isRequired wrapper markup to style it like a Sakai tool.
	 * Add to this any additional CSS or JS references that you need.
	 * 
	 */
	public void renderHead(IHeaderResponse response) {
		//get the Sakai skin header fragment from the request attribute
		HttpServletRequest request = (HttpServletRequest)getRequest().getContainerRequest();
		
		response.render(StringHeaderItem.forString((String)request.getAttribute("sakai.html.head")));
		response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));
		
		
		//Tool additions (at end so we can override if isRequired)
		response.render(StringHeaderItem.forString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"));
		//response.renderCSSReference("css/my_tool_styles.css");
		//response.renderJavascriptReference("js/my_tool_javascript.js");
	}
	
	/** 
	 * Helper to disable a link. Add the Sakai class 'current'.
	 */
	protected void disableLink(Link<Void> l) {
		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
		l.setEnabled(false);
	}

	/**
	 * Helper to disable the Link Headers
	 */
	protected void hideNavigationLink(Link<Void> l) {
		l.setVisible(false);
	}

	protected String getStatusString(Status s) {
		if(s == null) {
			return new ResourceModel("attendance.status.unknown").getObject();
		}
		switch (s)
		{
			case UNKNOWN: return new ResourceModel("attendance.status.unknown").getObject();
			case PRESENT: return new ResourceModel("attendance.status.present").getObject();
			case EXCUSED_ABSENCE: return new ResourceModel("attendance.status.excused").getObject();
			case UNEXCUSED_ABSENCE: return new ResourceModel("attendance.status.absent").getObject();
			case LATE: return new ResourceModel("attendance.status.late").getObject();
			case LEFT_EARLY: return new ResourceModel("attendance.status.left.early").getObject();
			default: return new ResourceModel("attendance.status.unknown").getObject();
		}
	}

	public static final String OVERVIEW_PAGE = "overview";
	public static final String ITEMS_PAGE = "items";
	public static final String STUDENT_PAGE = "student_view";
	public static final String STUDENT_OVERVIEW_PAGE = "student_overview";
}
