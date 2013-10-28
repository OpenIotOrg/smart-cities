package edu.kit.cm.ivu.smartmeetings.logic.datatypes;

import edu.kit.cm.ivu.smartmeetings.logic.interfaces.IUser;

/**
 * This class implements the {@link IUser} interface and is a container for the
 * data of a user.
 * 
 * @author Andreas Eberle
 * 
 */
public class User implements IUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4800001471620438226L;
	private final String userId;

	public User(final String userId) {
		this.userId = userId;
	}

	@Override
	public String getUserName() {
		return userId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

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
		final User other = (User) obj;
		if (userId == null) {
			if (other.userId != null) {
				return false;
			}
		} else if (!userId.equals(other.userId)) {
			return false;
		}
		return true;
	}

}
