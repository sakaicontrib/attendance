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

import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;

import java.util.List;
import java.util.Locale;

/**
 * An interface to abstract all Sakai related API calls in a central method that can be injected into our app.
 * 
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 *
 */
public interface SakaiProxy {

	/**
	 * Get current siteid
	 * @return
	 */
	String getCurrentSiteId();

	/**
	 * Get current User
	 * @return
     */
	User getCurrentUser();

	/**
	 * Get current user id
	 * @return
	 */
	String getCurrentUserId();
	
	/**
	 * Get current user display name
	 * @return
	 */
	String getCurrentUserDisplayName();

	/**
	 * Get Current User's Locale
	 * @return
	 */
	Locale getCurrentUserLocale();

	/**
	 * Get's the current user's role in the current site
	 * @return
     */
	String getCurrentUserRoleInCurrentSite();

	/**
	 * Get current user's role in site.
	 * @return
     */
	String getCurrentUserRole(String siteId);

	/**
	 * Is the current user a superUser? (anyone in admin realm)
	 * @return
	 */
	boolean isSuperUser();
	
	/**
	 * Post an event to Sakai
	 * 
	 * @param event			name of event
	 * @param reference		reference
	 * @param modify		true if something changed, false if just access
	 * 
	 */
	void postEvent(String event, String reference, boolean modify);
		
	/**
	 * Get a configuration parameter as a boolean
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return
	 */
	boolean getConfigParam(String param, boolean dflt);
	
	/**
	 * Get a configuration parameter as a String
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return
	 */
	String getConfigParam(String param, String dflt);

	/**
	 *
	 * @return List of userIds in the current site
	 */
	List<String> getCurrentSiteMembershipIds();

	/**
	 *
	 * @return List of Users in the current site
	 */
	List<User> getCurrentSiteMembership();

	/**
	 * get user
	 * @param userId
	 * @return
     */
	User getUser(String userId);

	/**
	 * get's a user sort name
	 * @param userId
	 * @return
     */
	String getUserSortName(String userId);

	/**
	 * Get a user's display id (username) ex. jdoe1
	 * @param userId
	 * @return
     */
	String getUserDisplayId(String userId);
}
