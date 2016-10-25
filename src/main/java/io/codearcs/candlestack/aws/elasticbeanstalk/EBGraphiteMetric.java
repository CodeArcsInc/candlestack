package io.codearcs.candlestack.aws.elasticbeanstalk;

import java.io.InputStream;
import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.aws.AWSMetric;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.resources.AWSResourceFetcher;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public enum EBGraphiteMetric implements AWSMetric {

	DiskUtilization( "check-disk-utilization", "check-aws-ec2-disk-utilization", "check-aws-eb-disk-utilization-via-es.sh" ),
	FreeMemory( "check-free-memory", "check-aws-ec2-free-memory", "check-aws-eb-free-memory-via-es.sh" );

	private String serviceName, commandName, resourceName, logsHost, logsAuthToken;


	private EBGraphiteMetric( String serviceName, String commandName, String resourceName ) {

		this.serviceName = serviceName;
		this.commandName = commandName;
		this.resourceName = resourceName;

		try {
			logsHost = GlobalAWSProperties.getLogsHost();
			logsAuthToken = GlobalAWSProperties.getLogsAuthToken();
		} catch ( CandlestackPropertiesException ignore ) {
			// We will see this error else where if this is the case
		}
	}


	public String getServiceName() {
		return serviceName;
	}


	public String getCommandName() {
		return commandName;
	}


	public String getResourceName() {
		return resourceName;
	}


	public Service getService( String instanceId, Set<String> contactGroups ) throws CandlestackPropertiesException {

		long warning = GlobalAWSProperties.getEBGraphiteMetricWarningLevel( instanceId, this );
		long critical = GlobalAWSProperties.getEBGraphiteMetricCriticalLevel( instanceId, this );

		String command = commandName + "!" + instanceId + "!" + warning + "!" + critical;

		return new Service( serviceName, instanceId, command, contactGroups );

	}


	public Command getMonitorCommand( String relativePathToMonitorResource ) {
		return new Command( commandName, relativePathToMonitorResource + resourceName + " " + logsHost + " " + logsAuthToken + " $ARG1$ $ARG2$ $ARG3$" );
	}


	public InputStream getResourceStream() {
		return AWSResourceFetcher.fetchInputStream( this, resourceName );
	}

}
