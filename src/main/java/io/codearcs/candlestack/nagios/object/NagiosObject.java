package io.codearcs.candlestack.nagios.object;

/**
 * Interface to represent a Nagios Configuration Object,
 * {@link https://assets.nagios.com/downloads/nagioscore/docs/nagioscore/4/en/configobject.html}
 */
public interface NagiosObject {

	/**
	 * Returns the string representation of the NagiosObject
	 * and any nested NagiosObjects as per the specification
	 * for representing Nagios Configuration Objects. Assume
	 * the result of this method call will be saved to a file
	 * for Nagios to consume.
	 *
	 * @return a non-null and non-empty string repsentation
	 *         of the Nagios Configuration Object based off of the Nagios
	 *         specification
	 */
	public String getObjectDefinitions();

}
