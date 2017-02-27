package io.codearcs.candlestack.aws.elasticbeanstalk;

import java.util.Set;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.MetricsReaderWriter;
import io.codearcs.candlestack.aws.CloudWatchStatistic;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchMetric;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public enum EBCloudWatchMetric implements CloudWatchMetric {

	EnvironmentHealth( CloudWatchStatistic.Maximum,
			"check-environment-health",
			"check-aws-eb-environment-health",
			"check-aws-eb-environment-health-via-es.sh",
			"Checks to see if the Elastic Beanstalk environemnt is unhealthy. In the event an alert is triggered check the Elastic Beanstalk environment for issues causing the instances to be considered unhealthy." );

	private static final String NAMESPACE = "AWS/ElasticBeanstalk",
			DIMENSION_KEY = "EnvironmentName";

	private String serviceName, commandName, scriptFileName, notes, logsHost, logsAuthToken;

	private CloudWatchStatistic statistic;


	private EBCloudWatchMetric( CloudWatchStatistic statistic, String serviceName, String commandName, String scriptFileName, String notes ) {
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

		long warning = GlobalAWSProperties.getEBCloudWatchMetricWarningLevel( instanceId, this );
		long critical = GlobalAWSProperties.getEBCloudWatchMetricCriticalLevel( instanceId, this );

		String command = commandName + "!" + MetricsReaderWriter.sanitizeString( instanceId ) + "!" + warning + "!" + critical;

		return new Service( serviceName, instanceId, command, notes, contactGroups );

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
