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

package org.sakaiproject.attendance.logic;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.sakaiproject.authz.api.*;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.*;
import org.sakaiproject.util.ResourceLoader;

import java.util.*;

/**
 * Implementation of our SakaiProxy API
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public class SakaiProxyImpl implements SakaiProxy {

	private static final Logger log = Logger.getLogger(SakaiProxyImpl.class);
    
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentSiteId(){
		return toolManager.getCurrentPlacement().getContext();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentUserDisplayName() {
	   return userDirectoryService.getCurrentUser().getDisplayName();
	}

	/**
	 * {@inheritDoc}
	 */
	public Locale getCurrentUserLocale() {
		Locale loc = null;
		try{
			// check if locale is requested for specific user
			String userId = getCurrentUserId();
			if(userId != null){
				Preferences prefs = getCurrentUserPreferences();
				ResourceProperties locProps = prefs.getProperties(ResourceLoader.APPLICATION_ID);
				String localeString = locProps.getProperty(ResourceLoader.LOCALE_KEY);
				// Parse user locale preference if set
				if(localeString != null){
					String[] locValues = localeString.split("_");
					if(locValues.length > 1)
						// language, country
						loc = new Locale(locValues[0], locValues[1]);
					else if(locValues.length == 1)
						// language
						loc = new Locale(locValues[0]);
				}
				if(loc == null) {
					loc = Locale.getDefault();
				}
			}else{
				loc = (Locale) getCurrentSession().getAttribute(ResourceLoader.LOCALE_KEY + getCurrentUserId());
			}
		}catch(NullPointerException e){
			loc = Locale.getDefault();
		}
		return loc;

	}

	/**
 	* {@inheritDoc}
 	*/
	public boolean isSuperUser() {
		return securityService.isSuperUser();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public void postEvent(String event,String reference,boolean modify) {
		eventTrackingService.post(eventTrackingService.newEvent(event,reference,modify));
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean getConfigParam(String param, boolean dflt) {
		return serverConfigurationService.getBoolean(param, dflt);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getConfigParam(String param, String dflt) {
		return serverConfigurationService.getString(param, dflt);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getCurrentSiteMembershipIds() {
		List<User> members = getCurrentSiteMembership();
		List<String> studentIds = new ArrayList<String>();
		for(User user : members) {
			studentIds.add(user.getId());
		}
		return studentIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getCurrentSiteMembership() {
		List<User> returnList = new ArrayList<User>();
		try {
			AuthzGroup membership = authzGroupService.getAuthzGroup("/site/" + getCurrentSiteId());
			Set<Member> memberSet = membership.getMembers();
			String maintainRole = membership.getMaintainRole();

			for(Member member : memberSet) {
				if(!maintainRole.equals(member.getRole().getId())) {
					try {
						User student = userDirectoryService.getUser(member.getUserId());
						returnList.add(student);
					} catch (UserNotDefinedException e) {
						log.error("Unable to get user " + member.getUserId() + " " + e);
						e.printStackTrace();
					}
				}
			}
		} catch (GroupNotDefinedException e) {
			log.error("Unable to get site membership " + e);
			e.printStackTrace();
		}
		return returnList;
	}

	/**
	 * {@inheritDoc}
	 */
	public User getUser(String userId) {
		try {
			return userDirectoryService.getUser(userId);
		} catch (UserNotDefinedException e) {
			log.error("Unable to get user " + userId + " " + e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		log.info("init");
	}

	private Preferences getCurrentUserPreferences() {
		return preferencesService.getPreferences(getCurrentUserId());
	}

	private Session getCurrentSession() {
		return sessionManager.getCurrentSession();
	}
	
	@Getter @Setter
	private ToolManager toolManager;
	
	@Getter @Setter
	private SessionManager sessionManager;
	
	@Getter @Setter
	private UserDirectoryService userDirectoryService;
	
	@Getter @Setter
	private SecurityService securityService;
	
	@Getter @Setter
	private EventTrackingService eventTrackingService;
	
	@Getter @Setter
	private ServerConfigurationService serverConfigurationService;
	
	@Getter @Setter
	private SiteService siteService;

	@Getter @Setter
	private PreferencesService preferencesService;

	@Getter @Setter
	private AuthzGroupService authzGroupService;
}
