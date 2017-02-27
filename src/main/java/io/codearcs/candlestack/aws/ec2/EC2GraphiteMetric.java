package io.codearcs.candlestack.aws.ec2;

import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.aws.AWSMetric;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public enum EC2GraphiteMetric implements AWSMetric {

	DiskUtilization( "check-disk-utilization",
			"check-aws-ec2-disk-utilization",
			"check-aws-ec2-disk-utilization-via-es.sh",
			"Checks to see if the EC2 instance is consuming a large amount of disk space. In the event an alert is triggered check the EC2 instance for potential issues resulting in large disk consumption." ),

	FreeMemory( "check-free-memory",
			"check-aws-ec2-free-memory",
			"check-aws-ec2-free-memory-via-es.sh",
			"Checks to see if the EC2 instance is consuming a large amount of memory. In the event an alert is triggered check the EC2 instances for potential memory leaks." );

	private String serviceName, commandName, scriptFileName, notes, logsHost, logsAuthToken;


	private EC2GraphiteMetric( String serviceName, String commandName, String scriptFileName, String notes ) {
		this.serviceName = serviceName;
		this.commandName = commandName;
		this.scriptFileName = scriptFileName;
		this.notes = notes;

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

		long warning = GlobalAWSProperties.getEC2GraphiteMetricWarningLevel( instanceId, this );
		long critical = GlobalAWSProperties.getEC2GraphiteMetricCriticalLevel( instanceId, this );

		String command = commandName + "!" + instanceId + "!" + warning + "!" + critical;

		return new Service( serviceName, instanceId, command, notes, contactGroups );

	}


	@Override
	public Command getMonitorCommand( String relativePathToMonitorResource ) {
		return new Command( commandName, relativePathToMonitorResource + scriptFileName + " " + logsHost + " " + logsAuthToken + " $ARG1$ $ARG2$ $ARG3$" );
	}

}