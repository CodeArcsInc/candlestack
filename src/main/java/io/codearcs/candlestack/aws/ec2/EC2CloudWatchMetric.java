package io.codearcs.candlestack.aws.ec2;

import java.util.Set;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.MetricsReaderWriter;
import io.codearcs.candlestack.aws.CloudWatchStatistic;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchMetric;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public enum EC2CloudWatchMetric implements CloudWatchMetric {

	CPUUtilization( CloudWatchStatistic.Average, "check-cpu", "check-aws-ec2-cpu", "check-aws-ec2-cpu-via-es.sh" );

	private static final String NAMESPACE = "AWS/EC2",
			DIMENSION_KEY = "InstanceId";

	private String serviceName, commandName, scriptFileName, logsHost, logsAuthToken;

	private CloudWatchStatistic statistic;


	private EC2CloudWatchMetric( CloudWatchStatistic statistic, String serviceName, String commandName, String scriptFileName ) {
		this.statistic = statistic;
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

		long warning = GlobalAWSProperties.getEC2CloudWatchMetricWarningLevel( instanceId, this );
		long critical = GlobalAWSProperties.getEC2CloudWatchMetricCriticalLevel( instanceId, this );

		String command = commandName + "!" + MetricsReaderWriter.sanitizeString( instanceId ) + "!" + warning + "!" + critical;

		return new Service( serviceName, instanceId, command, contactGroups );

	}


	@Override
	public Command getMonitorCommand( String relativePathToMonitorResource ) {
		return new Command( commandName, relativePathToMonitorResource + scriptFileName + " " + logsHost + " " + logsAuthToken + " $ARG1$ $ARG2$ $ARG3$" );
	}


	@Override
	public String getNamespace() {
		return NAMESPACE;
	}


	@Override
	public Dimension getDimension( String dimensionValue ) {
		return new Dimension().withName( DIMENSION_KEY ).withValue( dimensionValue );
	}


	@Override
	public String getName() {
		return name();
	}

}
