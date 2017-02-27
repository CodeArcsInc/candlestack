package io.codearcs.candlestack.aws.sqs;

import java.util.Set;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.MetricsReaderWriter;
import io.codearcs.candlestack.aws.CloudWatchStatistic;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchMetric;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public enum SQSCloudWatchMetric implements CloudWatchMetric {

	ApproximateAgeOfOldestMessage( CloudWatchStatistic.Maximum,
			"check-queue-message-age",
			"check-aws-sqs-queue-message-age",
			"check-aws-sqs-queue-message-age-via-es.sh",
			"Checks to see if the queue has pending messages that have been sitting on the queue for long period of time. In the event an alert is triggered ensure messages on the queue are being consumed and the relevant message consumers are healthy." );


	private static final String NAMESPACE = "AWS/SQS",
			DIMENSION_KEY = "QueueName";

	private String serviceName, commandName, scriptFileName, notes, logsHost, logsAuthToken;

	private CloudWatchStatistic statistic;


	private SQSCloudWatchMetric( CloudWatchStatistic statistic, String serviceName, String commandName, String scriptFileName, String notes ) {
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
	public Service getService( String queueName, Set<String> contactGroups ) throws CandlestackPropertiesException {

		long warning = GlobalAWSProperties.getSQSCloudWatchMetricWarningLevel( queueName, this );
		long critical = GlobalAWSProperties.getSQSCloudWatchMetricCriticalLevel( queueName, this );

		String command = commandName + "!" + MetricsReaderWriter.sanitizeString( queueName ) + "!" + warning + "!" + critical;

		return new Service( serviceName, queueName, command, notes, contactGroups );

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
