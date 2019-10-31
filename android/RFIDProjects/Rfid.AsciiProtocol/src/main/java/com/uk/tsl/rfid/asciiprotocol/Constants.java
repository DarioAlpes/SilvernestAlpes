package com.uk.tsl.rfid.asciiprotocol;

import java.util.Locale;

public class Constants
{
	/** The Locale used for interpreting command line parameters
	 */
	public static final Locale COMMAND_LOCALE = Locale.US;

	/** The Locale used for error reporting
	 */
	public static final Locale ERROR_LOCALE = Locale.US;

	/**
	 * The escape character used in barcode responses
	 */
	public static final String BARCODE_ESCAPE_CHARACTER = "\u001b";

}
