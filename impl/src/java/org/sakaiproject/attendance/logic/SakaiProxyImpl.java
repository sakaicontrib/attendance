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

package org.sakaiproject.attendance.logic;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.sakaiproject.authz.api.*;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
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
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
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
	public String getCurrentSiteTitle() {
		try {
			return siteService.getSite(getCurrentSiteId()).getTitle();
		} catch (IdUnusedException e) {
			log.error("getCurrentSiteTitle()", e);
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public User getCurrentUser() {
		return getUser(getCurrentUserId());
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
	public String getCurrentUserRoleInCurrentSite() {
		return getCurrentUserRole(getCurrentSiteId());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentUserRole(String siteId) {
		return authzGroupService.getUserRole(getCurrentUserId(), "/site/" + siteId);
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
			returnList = getUserListForMemberSetHelper(memberSet, maintainRole);
		} catch (GroupNotDefinedException e) {
			log.error("Unable to get site membership " + e);
			e.printStackTrace();
		}
		return returnList;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getGroupMembershipIdsForCurrentSite(String groupId) {
		return getGroupMembershipIds(getCurrentSiteId(), groupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getGroupMembershipIds(String siteId, String groupId) {
		List<String> returnList = new ArrayList<String>();
		for(User user : getGroupMembership(siteId, groupId)) {
			returnList.add(user.getId());
		}
		return returnList;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getGroupMembershipForCurrentSite(String groupId) {
		return getGroupMembership(getCurrentSiteId(), groupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getGroupMembership(String siteId, String groupId) {
		List<User> returnList = new ArrayList<User>();
		try {
			Group group = siteService.getSite(siteId).getGroup(groupId);
			if(group != null) {
				Set<Member> memberSet = group.getMembers();
				String maintainRole = group.getMaintainRole();
				returnList = getUserListForMemberSetHelper(memberSet, maintainRole);
			}
		} catch (IdUnusedException e) {
			log.error("Unable to get group membership " + e);
			e.printStackTrace();
		}
		return returnList;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getAvailableGroupsForSite(String siteId) {
		try {
			List<String> returnList = new ArrayList<String>();
			Site site = siteService.getSite(siteId);
			for(Group group : site.getGroups()) {
				returnList.add(group.getId());
			}
			return returnList;
		} catch (IdUnusedException e) {
			log.error("getAvailableGroupIdsForSite " + siteId + " IdUnusedException");
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getAvailableGroupsForCurrentSite() {
		return getAvailableGroupsForSite(getCurrentSiteId());
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
	 * {@inheritDoc}
	 */
	public String getUserSortName(String userId) {
		User u = getUser(userId);
		if(u != null){
			return u.getSortName();
		}

		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserDisplayId(String userId) {
		User u = getUser(userId);

		if(u != null) {
			return u.getDisplayId();
		}

		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGroupTitleForCurrentSite(String groupId) {
		return getGroupTitle(getCurrentSiteId(), groupId);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGroupTitle(String siteId, String groupId) {
		try {
			if(siteId != null && !siteId.isEmpty() && groupId != null && !groupId.isEmpty()) {
				return siteService.getSite(siteId).getGroup(groupId).getTitle();
			}
		} catch (IdUnusedException e) {
			log.error("Unable to get group title", e);
			e.printStackTrace();
		}
		return "";
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

	private List<User> getUserListForMemberSetHelper(Set<Member> memberSet, String maintainRole) {
		List<User> userList = new ArrayList<User>();
		if(memberSet != null) {
			for(Member member : memberSet) {
				if(maintainRole != null && !maintainRole.equals(member.getRole().getId()) && member.isActive()) {
					try {
						User student = userDirectoryService.getUser(member.getUserId());
						userList.add(student);
					} catch (UserNotDefinedException e) {
						log.error("Unable to get user " + member.getUserId() + " " + e);
						e.printStackTrace();
					}
				}
			}
		}
		return userList;
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
