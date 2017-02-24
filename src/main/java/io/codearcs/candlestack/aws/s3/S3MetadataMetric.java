package io.codearcs.candlestack.aws.s3;

import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.aws.AWSMetric;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public enum S3MetadataMetric implements AWSMetric {

	LastModified( "check-last-modified", "check-aws-s3-last-modified", "check-aws-s3-last-modified-via-es.sh" );

	private String serviceName, commandName, scriptFileName, logsHost, logsAuthToken;


	private S3MetadataMetric( String serviceName, String commandName, String scriptFileName ) {

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

		long warning = GlobalAWSProperties.getS3MetadataMetricWarningLevel( instanceId, this );
		long critical = GlobalAWSProperties.getS3MetadataMetricCriticalLevel( instanceId, this );

		String command = commandName + "!" + instanceId + "!" + warning + "!" + critical;

		return new Service( serviceName, instanceId, command, contactGroups );

	}


	@Override
	public Command getMonitorCommand( String relativePathToMonitorResource ) {
		return new Command( commandName, relativePathToMonitorResource + scriptFileName + " " + logsHost + " " + logsAuthToken + " $ARG1$ $ARG2$ $ARG3$" );
	}

}
