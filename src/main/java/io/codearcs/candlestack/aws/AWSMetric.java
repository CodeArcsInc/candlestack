package io.codearcs.candlestack.aws;

import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public interface AWSMetric {

	public String getServiceName();


	public String getCommandName();


	public String getScriptFileName();


	// TODO consider using default methods for these following methods

	public Service getService( String dbInstanceId, Set<String> contactGroups ) throws CandlestackPropertiesException;


	public Command getMonitorCommand( String relativePathToMonitorResource );

}
