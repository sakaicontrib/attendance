package org.sakaiproject.attendance.logic;

import java.util.Locale;

/**
 * An interface to abstract all Sakai related API calls in a central method that can be injected into our app.
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public interface SakaiProxy {

	/**
	 * Get current siteid
	 * @return
	 */
	String getCurrentSiteId();
	
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
}
