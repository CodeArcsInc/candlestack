package io.codearcs.candlestack.aws.sqs;

import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.MetricsReaderWriter;
import io.codearcs.candlestack.aws.AWSMetric;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public enum SQSQueueAttribute implements AWSMetric {

	ApproximateNumberOfMessages( "check-queue-size",
			"check-aws-sqs-queue-size",
			"check-aws-sqs-queue-size-via-es.sh",
			"Checks to see if the queue has grown larger than a specified threshold. In the event an alert is triggered ensure messages on the queue are being consumed and the relevant message consumers are healthy." ),

	LastModifiedTimestamp( "check-queue-last-modified",
			"check-aws-sqs-queue-last-modified",
			"check-aws-sqs-queue-last-modified-via-es.sh",
			"Checks to see if the queue has been recently modified. In the event an alert is triggered ensure messages are being put on the queue and the relevant message producers are healthy." );


	private String serviceName, commandName, scriptFileName, notes, logsHost, logsAuthToken;


	private SQSQueueAttribute( String serviceName, String commandName, String scriptFileName, String notes ) {
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
	public Service getService( String queueName, Set<String> contactGroups ) throws CandlestackPropertiesException {

		long warning = GlobalAWSProperties.getSQSQueueAttributeWarningLevel( queueName, this );
		long critical = GlobalAWSProperties.getSQSQueueAttributeCriticalLevel( queueName, this );

		String command = commandName + "!" + MetricsReaderWriter.sanitizeString( queueName ) + "!" + warning + "!" + critical;

		return new Service( serviceName, queueName, command, notes, contactGroups );

	}


	@Override
	public Command getMonitorCommand( String relativePathToMonitorResource ) {
		return new Command( commandName, relativePathToMonitorResource + scriptFileName + " " + logsHost + " " + logsAuthToken + " $ARG1$ $ARG2$ $ARG3$" );
	}


}
