package edu.kit.cm.ivu.smartmeetings.backend.rooms;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a single property of a room.
 * 
 * @author Bjoern Juergens
 * @author Valentin Zickner
 * @author David Kulicke
 */
public class RoomProperty implements Serializable {
	private static final long serialVersionUID = 2838229551014010645L;

	/**
	 * Name of the RoomProperty
	 */
	private final String name;

	/**
	 * Id of the RoomProperty
	 */
	private final String id;

	/**
	 * Child properties
	 */
	private List<RoomProperty> children;

	/**
	 * Constructs a new {@link RoomProperty} with the given name.
	 * 
	 * @param name
	 *            the name of the RoomProperty.
	 */
	public RoomProperty(final String id, final String name) {
		this.name = name;
		this.id = id;
		this.children = Collections.emptyList();
	}

	/**
	 * Constructs a new {@link RoomProperty} without name or id.
	 */
	public RoomProperty() {
		this.name = null;
		this.id = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * Compares this RoomProperty with another object.
	 * 
	 * @param Object
	 *            obj Object to compare with
	 * @return boolean Result of comparison.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final RoomProperty other = (RoomProperty) obj;
		if (toString() == null) {
			if (other.toString() != null) {
				return false;
			}
		} else if (!toString().equals(other.toString())) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the name of this property
	 * 
	 * @return String Property name
	 */

	public String getName() {
		return name;
	}

	/**
	 * Returns the {@link RoomProperty}'s children
	 * 
	 * @return the children
	 */
	public List<RoomProperty> getChildren() {
		return children;
	}

	/**
	 * Sets the {@link RoomProperty}'s children to the given list.
	 * 
	 * @param children
	 *            the children to set
	 */
	public void setChildren(final List<RoomProperty> children) {
		this.children = children;
	}

	/**
	 * Get id of this RoomProperty
	 * 
	 * @return the unique id for this property as an URI.
	 * @author Valentin Zickner
	 */

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return getId();
	}
}
