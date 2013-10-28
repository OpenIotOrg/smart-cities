package edu.kit.cm.ivu.smartmeetings.logic.interfaces;

import java.io.Serializable;

/**
 * This interface defines the methods of an object that can be used as a user.
 * 
 * @author Andreas Eberle
 * 
 */
public interface IUser extends Serializable {

	/**
	 * 
	 * @return Returns the id of this user.
	 */
	String getUserName();
}
