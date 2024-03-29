package io.codearcs.candlestack.aws.ec2;


import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.MetricsReaderWriter;
import io.codearcs.candlestack.aws.CloudWatchStatistic;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchMetric;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public enum EC2CloudWatchMetric implements CloudWatchMetric {

	CPUUtilization( CloudWatchStatistic.Average,
			"check-cpu-cw",
			"check-aws-ec2-cpu-cw",
			"check-aws-ec2-cpu-via-es-cw.sh",
			"Checks to see if the EC2 instance is experiencing heavy CPU load. In the event an alert is triggered check the EC2 instance for processing consuming large amount of CPU or potentially a noisy neighbor stealing resources." ),

	NetworkIn( CloudWatchStatistic.Maximum,
			"check-network-in-cw",
			"check-aws-ec2-network-in-cw",
			"check-aws-ec2-network-in-via-es-cw.sh",
			"Checks to see if the EC2 instance has network traffice flowing into the system. In the event an alert is triggered check the EC2 instance for network issues that would prevent other systems from connecting." ),

	NetworkOut( CloudWatchStatistic.Maximum,
			"check-network-out-cw",
			"check-aws-ec2-network-out-cw",
			"check-aws-ec2-network-out-via-es-cw.sh",
			"Checks to see if the EC2 instance has network traffice flowing out of the system. In the event an alert is triggered check the EC2 instance for network issues that would prevent it from sending out data." );

	private static final String NAMESPACE = "AWS/EC2";

	private String serviceName, commandName, scriptFileName, notes, logsHost, logsAuthToken;

	private CloudWatchStatistic statistic;


	private EC2CloudWatchMetric( CloudWatchStatistic statistic, String serviceName, String commandName, String scriptFileName, String notes ) {
		this.statistic = statistic;
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
	public CloudWatchStatistic getStatistic() {
		return statistic;
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
		long warning = GlobalAWSProperties.getEC2CloudWatchMetricWarningLevel( instanceId, this );
		long critical = GlobalAWSProperties.getEC2CloudWatchMetricCriticalLevel( instanceId, this );

		String command = commandName + commanNameSuffix + "!" + MetricsReaderWriter.sanitizeString( instanceId ) + "!" + warning + "!" + critical;

		return new Service( serviceName, instanceId, command, notes, notificationPeriod, contactGroups );
	}


	@Override
	public Command getMonitorCommand( String relativePathToMonitorResource ) {
		return getMonitorCommand( "", relativePathToMonitorResource );
	}


	public Command getMonitorCommand( String commandNameSuffix, String relativePathToMonitorResource ) {
		return new Command( commandName + commandNameSuffix, relativePathToMonitorResource + scriptFileName + " " + logsHost + " " + logsAuthToken + " $ARG1$ $ARG2$ $ARG3$" );
	}


	@Override
	public String getNamespace() {
		return NAMESPACE;
	}


	@Override
	public String getName() {
		return name();
	}


}
