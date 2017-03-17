package io.codearcs.candlestack.aws;

import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public interface AWSMetric {

	public String getServiceName();


	public String getCommandName();


	public String getScriptFileName();


	public Service getService( String instanceId, Set<String> contactGroups ) throws CandlestackPropertiesException;


	public Command getMonitorCommand( String relativePathToMonitorResource );

}
