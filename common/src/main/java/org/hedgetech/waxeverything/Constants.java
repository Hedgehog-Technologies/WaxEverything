package org.hedgetech.waxeverything;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constants Utility class
 */
public final class Constants {
	/**
	 * Mod Id - should be all lowercase
	 */
	public static final String MOD_ID = "waxeverything";

	/**
	 * Mod Name - Should be PascalCase
	 */
	public static final String MOD_NAME = "WaxEverything";

	/**
	 * Logger - To make more consistent logging instead of using stdout
	 */
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final String WAXED_TAG_NAME = MOD_ID + ":waxed";

	private Constants() {
		throw new UnsupportedOperationException("Static Utility class, no need to instantiate");
	}
}