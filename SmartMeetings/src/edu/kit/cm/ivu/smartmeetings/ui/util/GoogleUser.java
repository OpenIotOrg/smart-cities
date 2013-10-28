package edu.kit.cm.ivu.smartmeetings.ui.util;

import android.accounts.Account;
import edu.kit.cm.ivu.smartmeetings.logic.interfaces.IUser;

/**
 * This represents a google user account.
 * 
 * @author Michael Zangl
 * 
 */
public class GoogleUser implements IUser {

	private static final long serialVersionUID = 1213244522936330279L;
	private final String name;

	/**
	 * Creates a new {@link GoogleUser} object.
	 * 
	 * @param account
	 *            The account.
	 */
	public GoogleUser(final Account account) {
		name = account.name;
	}

	/**
	 * Creates a new {@link GoogleUser} object.
	 * 
	 * @param account
	 *            The account name.
	 */
	public GoogleUser(final String name) {
		super();
		this.name = name;
	}

	@Override
	public String getUserName() {
		return name;
	}

}
