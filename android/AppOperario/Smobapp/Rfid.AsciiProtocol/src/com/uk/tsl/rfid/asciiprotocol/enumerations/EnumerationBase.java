package com.uk.tsl.rfid.asciiprotocol.enumerations;


/**
 * Base class for parameters that are constrained to a set of permitted values
 */
public abstract class EnumerationBase
{
	/**
	 * Initializes a new instance of the EnumerationBase class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	protected EnumerationBase( String argument, String description )
	{
		this.setArgument(argument);
		this.setDescription(description);
	}


	/// Accessors for the parameter as output to the command line
	public String getArgument() { return mArgument; }
	private void setArgument(String argument) {	mArgument = argument; }
	private String mArgument;

	/// Accessors for the human-readable description of the parameter value
	public String getDescription() { return mDescription; }
	private void setDescription(String description) { mDescription = description; }
	private String mDescription;

	@Override
	public String toString() {	return this.getClass().getName() + ": " + mArgument + " (" + mDescription + ")"; }

}
