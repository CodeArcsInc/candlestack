package io.codearcs.candlestack.aws.elasticbeanstalk;

import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.aws.AWSMetric;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public enum EBGraphiteMetric implements AWSMetric {

	DiskUtilization( "check-disk-utilization", "check-aws-ec2-disk-utilization", "check-aws-eb-disk-utilization-via-es.sh" ),
	FreeMemory( "check-free-memory", "check-aws-ec2-free-memory", "check-aws-eb-free-memory-via-es.sh" );

	private String serviceName, commandName, scriptFileName, logsHost, logsAuthToken;


	private EBGraphiteMetric( String serviceName, String commandName, String scriptFileName ) {

		this.serviceName = serviceName;
		this.commandName = commandName;
		this.scriptFileName = scriptFileName;

		try {
			logsHost = GlobalAWSProperties.getLogsHost();
			logsAuthToken = GlobalAWSProperties.getLogsAuthToken();
		} catch ( CandlestackPropertiesException ignore ) {
			// We will see this error else where if this is the case
		}
	}


	@Override
	public String getServiceName() {
		return serviceName;
	}


	@Override
	public String getCommandName() {
		return commandName;
	}


	@Override
	public String getScriptFileName() {
		return scriptFileName;
	}


	@Override
	public Service getService( String instanceId, Set<String> contactGroups ) throws CandlestackPropertiesException {

		long warning = GlobalAWSProperties.getEBGraphiteMetricWarningLevel( instanceId, this );
		long critical = GlobalAWSProperties.getEBGraphiteMetricCriticalLevel( instanceId, this );

		String command = commandName + "!" + instanceId + "!" + warning + "!" + critical;

		return new Service( serviceName, instanceId, command, contactGroups );

	}


	@Override
	public Command getMonitorCommand( String relativePathToMonitorResource ) {
		return new Command( commandName, relativePathToMonitorResource + scriptFileName + " " + logsHost + " " + logsAuthToken + " $ARG1$ $ARG2$ $ARG3$" );
	}


}
