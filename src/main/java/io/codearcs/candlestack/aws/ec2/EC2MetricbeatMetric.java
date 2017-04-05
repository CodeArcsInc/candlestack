package io.codearcs.candlestack.aws.ec2;

import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.aws.AWSMetric;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public enum EC2MetricbeatMetric implements AWSMetric {

			"check-aws-ec2-cpu-via-es-mb.sh",
			"Checks to see if the EC2 instance is experiencing heavy CPU load. In the event an alert is triggered check the EC2 instance for processing consuming large amount of CPU or potentially a noisy neighbor stealing resources." ),

			"check-aws-ec2-network-in-via-es-mb.sh",
			"Checks to see if the EC2 instance has network traffice flowing into the system. In the event an alert is triggered check the EC2 instance for network issues that would prevent other systems from connecting." ),

			"check-aws-ec2-network-out-via-es-mb.sh",
			"Checks to see if the EC2 instance has network traffice flowing out of the system. In the event an alert is triggered check the EC2 instance for network issues that would prevent it from sending out data." ),
			"check-aws-ec2-disk-utilization-via-es-mb.sh",
			"Checks to see if the EC2 instance is consuming a large amount of disk space. In the event an alert is triggered check the EC2 instance for potential issues resulting in large disk consumption." ),

			"check-aws-ec2-free-memory-via-es-mb.sh",
			"Checks to see if the EC2 instance is consuming a large amount of memory. In the event an alert is triggered check the EC2 instances for potential memory leaks." );

	private String serviceName, commandName, scriptFileName, notes, logsHost, logsAuthToken;


	private EC2MetricbeatMetric( String serviceName, String commandName, String scriptFileName, String notes ) {
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
		return getService( "", GlobalAWSProperties.getEC2ServiceNotificationPeriod( instanceId ), instanceId, contactGroups );
	}


	public Service getService( String commanNameSuffix, String notificationPeriod, String instanceId, Set<String> contactGroups ) throws CandlestackPropertiesException {
		long warning = GlobalAWSProperties.getEC2MetricbeatMetricWarningLevel( instanceId, this );
		long critical = GlobalAWSProperties.getEC2MetricbeatMetricCriticalLevel( instanceId, this );

		String command = commandName + "!" + instanceId + "!" + warning + "!" + critical;

		return new Service( serviceName, instanceId, command + commanNameSuffix, notes, notificationPeriod, contactGroups );
	}


	@Override
	public Command getMonitorCommand( String relativePathToMonitorResource ) {
		return getMonitorCommand( "", relativePathToMonitorResource );
	}


	public Command getMonitorCommand( String commandNameSuffix, String relativePathToMonitorResource ) {
		return new Command( commandName + commandNameSuffix, relativePathToMonitorResource + scriptFileName + " " + logsHost + " " + logsAuthToken + " $ARG1$ $ARG2$ $ARG3$" );
	}


}
