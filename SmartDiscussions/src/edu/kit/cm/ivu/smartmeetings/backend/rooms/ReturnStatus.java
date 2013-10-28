package edu.kit.cm.ivu.smartmeetings.backend.rooms;

/**
 * A simple boolean container which can be used to pass the success status of an
 * AppEngine endpoint method.
 * 
 * @author David Kulicke
 * 
 */
public class ReturnStatus {

	private final boolean success;

	/**
	 * Constructs a new {@link ReturnStatus}
	 * 
	 * @param success
	 *            the success value of this {@link ReturnStatus}
	 */
	public ReturnStatus(final boolean success) {
		this.success = success;
	}

	/**
	 * Returns the success value of this {@link ReturnStatus}.
	 * 
	 * @return the success value of this {@link ReturnStatus}
	 */
	public boolean getSuccess() {
		return this.success;
	}

}
