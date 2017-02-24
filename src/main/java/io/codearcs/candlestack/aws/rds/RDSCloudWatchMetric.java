package io.codearcs.candlestack.aws.rds;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.MetricsReaderWriter;
import io.codearcs.candlestack.aws.CloudWatchStatistic;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchMetric;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public enum RDSCloudWatchMetric implements CloudWatchMetric {

	CPUUtilization( CloudWatchStatistic.Average, "check-cpu", "check-aws-rds-cpu", "check-aws-rds-cpu-via-es.sh", new HashSet<>( Arrays.asList( RDSType.AURORA, RDSType.MARIADB ) ), false ),
	DatabaseConnections( CloudWatchStatistic.Maximum, "check-db-connections", "check-aws-rds-db-connections", "check-aws-rds-db-connections-via-es.sh", new HashSet<>( Arrays.asList( RDSType.AURORA, RDSType.MARIADB ) ), false ),
	FreeStorageSpace( CloudWatchStatistic.Minimum, "check-free-storage", "check-aws-rds-free-storage", "check-aws-rds-free-storage-via-es.sh", new HashSet<>( Arrays.asList( RDSType.MARIADB ) ), false ),
	VolumeBytesUsed( CloudWatchStatistic.Maximum, "check-storage-used", "check-aws-rds-storage-used", "check-aws-rds-storage-used-via-es.sh", new HashSet<>( Arrays.asList( RDSType.AURORA ) ), false ),
	AuroraReplicaLag( CloudWatchStatistic.Maximum, "check-replica-lag", "check-aws-rds-replica-lag", "check-aws-rds-replica-lag-via-es.sh", new HashSet<>( Arrays.asList( RDSType.AURORA ) ), true ),
	ActiveTransactions( CloudWatchStatistic.Maximum, "check-active-transactions", "check-aws-rds-active-transactions", "check-aws-rds-active-transactions-via-es.sh", new HashSet<>( Arrays.asList( RDSType.AURORA ) ), false );

	private static final String NAMESPACE = "AWS/RDS",
			DIMENSION_KEY = "DBInstanceIdentifier";

	private String serviceName, commandName, scriptFileName, logsHost, logsAuthToken;

	private CloudWatchStatistic statistic;

	private Set<RDSType> supportedRDSTypes;

	private boolean replicaOnly;


	private RDSCloudWatchMetric( CloudWatchStatistic statistic, String serviceName, String commandName, String scriptFileName, Set<RDSType> supportedRDSTypes, boolean replicaOnly ) {
		this.statistic = statistic;
		this.serviceName = serviceName;
		this.commandName = commandName;
		this.scriptFileName = scriptFileName;
		this.supportedRDSTypes = supportedRDSTypes;
		this.replicaOnly = replicaOnly;

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


	public boolean isRDSTypeSupported( RDSType rdsType ) {
		return supportedRDSTypes.contains( rdsType );
	}


	public boolean isReplicaOnlyMetric() {
		return replicaOnly;
	}


	@Override
	public Service getService( String dbInstanceId, Set<String> contactGroups ) throws CandlestackPropertiesException {

		long warning = GlobalAWSProperties.getRDSCloudWatchMetricWarningLevel( dbInstanceId, this );
		long critical = GlobalAWSProperties.getRDSCloudWatchMetricCriticalLevel( dbInstanceId, this );

		String command = commandName + "!" + MetricsReaderWriter.sanitizeString( dbInstanceId ) + "!" + warning + "!" + critical;

		return new Service( serviceName, dbInstanceId, command, contactGroups );

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
