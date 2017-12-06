package io.codearcs.candlestack.aws.lambda;

import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.MetricsReaderWriter;
import io.codearcs.candlestack.aws.CloudWatchStatistic;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchMetric;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;

/**
 * Defines the set of Lambda CloudWatch Metrics to collect.
 * 
 * @author Amanda
 *
 */
public enum LambdaCloudWatchMetric implements CloudWatchMetric {

	Invocations ( CloudWatchStatistic.Maximum,
			"check-lambda-invocations",
			"check-aws-lambda-invocations",
			"check-aws-lambda-invocations-via-es-cw.sh",
			"Checks to see if the Lambda function has been invoked at least a minimum number of times." ),

	Errors ( CloudWatchStatistic.Maximum,
			"check-lambda-errors",
			"check-aws-lambda-errors",
			"check-aws-lambda-errors-via-es-cw.sh",
			"Checks to se if the Lambda function has generated more errors than expected" ),

	Duration ( CloudWatchStatistic.Maximum,
			"check-lambda-duration",
			"check-aws-lambda-duration",
			"check-aws-lambda-duration-via-es-cw.sh",
			"Checks that the Lambda function has not taken longer than expected to execute. " );

	
	// Lambda Namespace
	private static final String NAMESPACE = "AWS/Lambda";
	

	private String serviceName, commandName, scriptFileName, notes, logsHost, logsAuthToken;

	private CloudWatchStatistic statistic;

	
	private LambdaCloudWatchMetric( CloudWatchStatistic statistic, String serviceName, String commandName, String scriptFileName, String notes ) {
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
	public Service getService(String instanceId, Set<String> contactGroups) throws CandlestackPropertiesException {
		
		long warning = GlobalAWSProperties.getLambdaCloudWatchMetricWarningLevel( instanceId, this );
		long critical = GlobalAWSProperties.getLambdaCloudWatchMetricCriticalLevel( instanceId, this );
		String command = commandName + "!" + MetricsReaderWriter.sanitizeString( instanceId ) + "!" + warning + "!" + critical;

		String notificationPeriod = GlobalAWSProperties.getLambdaServiceNotificationPeriod( instanceId );

		return new Service( serviceName, instanceId, command, notes, notificationPeriod, contactGroups );
	}

	@Override
	public Command getMonitorCommand(String relativePathToMonitorResource) {
		return new Command( commandName, relativePathToMonitorResource + scriptFileName + " " + logsHost + " " + logsAuthToken + " $ARG1$ $ARG2$ $ARG3$" );
	}

	@Override
	public CloudWatchStatistic getStatistic() {
		return statistic;
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
