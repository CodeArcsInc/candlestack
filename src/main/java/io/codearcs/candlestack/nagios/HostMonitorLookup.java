package io.codearcs.candlestack.nagios;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.hosts.HostGroup;


/**
 * Interface to define the contract the Nagios
 * code will use when determining what needs to
 * be monitored and the files required to perform the
 * monitoring.
 */
public interface HostMonitorLookup {

	/**
	 * A distinct name that identifies the particular
	 * implementation of the HostMonitorLookup interface.
	 *
	 * @return a distinct name for the given implementation of HostMonitorLookup
	 *         that is file system friendly
	 */
	public String getName();


	/**
	 * This method will be called routinely by the Nagios code
	 * to determine if there are hosts in need of monitoring.
	 *
	 * @return If nothing should be monitored an empty list should be returned,
	 *         otherwise it should be a non-empty list with fully defined HostGroup objects
	 *
	 * @throws CandlestackException
	 *           should throw an exception in the event the implementation
	 *           encountered an error while trying to perform the lookup
	 */
	public List<HostGroup> lookupHostsToMonitor() throws CandlestackException;


	/**
	 * This method is meant to provide the implementation a way of passing
	 * any resources that are required for the monitoring of the hosts returned via
	 * the lookup method. An example of this would be check scripts that will be called
	 * by monitoring commands. This method will only be called if lookupHostsToMonitor()
	 * returned a non-empty list.
	 *
	 * @return an empty map if no resources are needed for monitoring, or a non-empty
	 *         map with the String key being the filename that should be used for representing
	 *         the corresponding InputStream value
	 * @throws CandlestackException
	 *           should throw an exception in the event the implementation
	 *           encountered an error
	 */
	public Map<String, InputStream> getMonitorResources() throws CandlestackException;


	/**
	 * This method is meant to provide the implementation a way of passing any
	 * Commands that are required for the monitoring of the hosts returned via the
	 * lookup method. This method will only be called if lookupHostsToMonitor()
	 * returned a non-empty list.
	 *
	 * @param relativePathToMonitorResource
	 *          the relative path to the directory where the resources
	 *          returned by the getMonitorResources() method have been saved
	 * @return an empty list if no commands are needed for monitoring, or a non-empty
	 *         list with fully defined Command objects
	 * @throws CandlestackException
	 *           should throw an exception in the event the implementation
	 *           encountered an error
	 */
	public List<Command> getMonitorCommands( String relativePathToMonitorResource ) throws CandlestackException;

}
