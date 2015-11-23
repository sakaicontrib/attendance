package org.sakaiproject.attendance.dao;

import java.util.List;

import org.sakaiproject.attendance.model.Thing;

/**
 * DAO interface for our project
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public interface ProjectDao {

	/**
	 * Gets a single Thing from the db
	 * 
	 * @return an item or null if no result
	 */
	public Thing getThing(long id);
	
	/**
	 * Get all Things
	 * @return a list of items, an empty list if no items
	 */
	public List<Thing> getThings();
		
	/**
	 * Add a new Thing record to the database. Only the name property is actually used.
	 * @param t	Thing
	 * @return	true if success, false if not
	 */
	public boolean addThing(Thing t);
}
