package edu.kit.cm.ivu.smartmeetings.backend.rooms;

import java.util.Collections;
import java.util.List;

/**
 * A simple container class which can be used for passing a list of
 * RoomProperties to an AppEngine API method.
 * 
 * @author David Kulicke
 * 
 */
public class RoomSearchCriteria {

	private List<RoomProperty> criteria;

	/**
	 * Constructs a new {@link RoomSearchCriteria} object with the given
	 * property list
	 * 
	 * @param criteria
	 *            the RoomProperties this {@link RoomSearchCriteria} will store.
	 */
	public RoomSearchCriteria(final List<RoomProperty> criteria) {
		this.criteria = criteria;
	}
	
	public RoomSearchCriteria() {
		//dummy constructor for JSON
		this.criteria = Collections.emptyList();
	}

	/**
	 * Returns the list of RoomProperties stored in this
	 * {@link RoomSearchCriteria} object.
	 * 
	 * @return the list of RoomProperties stored in this
	 *         {@link RoomSearchCriteria} object
	 */
	public List<RoomProperty> getCriteria() {
		return this.criteria;
	}
	
	/**
	 * Sets the criteria list to the given one.
	 * @param criteria the new criteria list
	 */
	public void setCriteria(final List<RoomProperty> criteria) {
		this.criteria = criteria;
	}
}
