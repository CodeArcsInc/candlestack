package io.codearcs.candlestack.aws.rds;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.MetricsReaderWriter;
import io.codearcs.candlestack.aws.CloudWatchStatistic;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchMetric;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.services.Service;


public enum RDSCloudWatchMetric implements CloudWatchMetric {

	CPUUtilization( CloudWatchStatistic.Average,
			"check-cpu",
			"check-aws-rds-cpu",
			"check-aws-rds-cpu-via-es.sh",
			"Checks to see if the RDS instance is experiencing heavy CPU load. In the event an alert is triggered check the RDS instance for potential query issues causing the heavy CPU load.",
			new HashSet<>( Arrays.asList( RDSType.AURORA, RDSType.MARIADB ) ),
			false,
			false ),

	DatabaseConnections( CloudWatchStatistic.Maximum,
			"check-db-connections",
			"check-aws-rds-db-connections",
			"check-aws-rds-db-connections-via-es.sh",
			"Checks to see if the RDS instance is experiencing high number of database connections. In the event an alert is triggered check the users of the RDS instance for potential connection leaks.",
			new HashSet<>( Arrays.asList( RDSType.AURORA, RDSType.MARIADB ) ),
			false,
			false ),

	FreeStorageSpace( CloudWatchStatistic.Minimum,
			"check-free-storage",
			"check-aws-rds-free-storage",
			"check-aws-rds-free-storage-via-es.sh",
			"Checks to see if the RDS instance is running low on available storage space. In the event an alert is triggered check the RDS instance for potential issues causing a spike in data usage.",
			new HashSet<>( Arrays.asList( RDSType.MARIADB ) ),
			false,
			false ),

	VolumeBytesUsed( CloudWatchStatistic.Average,
			"check-storage-used",
			"check-aws-rds-storage-used",
			"check-aws-rds-storage-used-via-es.sh",
			"Checks to see if the RDS instance has used more storage space than has been specified. In the event an alert is triggered check the RDS instance for potential issues causing a spike in data usage.",
			new HashSet<>( Arrays.asList( RDSType.AURORA ) ),
			false,
			true ),

	AuroraReplicaLag( CloudWatchStatistic.Maximum,
			"check-replica-lag",
			"check-aws-rds-replica-lag",
			"check-aws-rds-replica-lag-via-es.sh",
			"Checks to see if the Aurora read replica is experiencing a high replication lag. In the event an alert is triggered check the Aurora cluster for potential issues causing the lag.",
			new HashSet<>( Arrays.asList( RDSType.AURORA ) ),
			true,
			false ),

	ActiveTransactions( CloudWatchStatistic.Maximum,
			"check-active-transactions",
			"check-aws-rds-active-transactions",
			"check-aws-rds-active-transactions-via-es.sh",
			"Checks to see if the RDS instance is experiencing a large number of active transactions. In the event an alert is triggered check the RDS instance for potential query issues causing the transaction build up.",
			new HashSet<>( Arrays.asList( RDSType.AURORA ) ),
			false,
			false );


	private static final String NAMESPACE = "AWS/RDS";

	private String serviceName, commandName, scriptFileName, notes, logsHost, logsAuthToken;

	private CloudWatchStatistic statistic;

	private Set<RDSType> supportedRDSTypes;

	private boolean replicaOnly, clusterOnly;


	private RDSCloudWatchMetric( CloudWatchStatistic statistic, String serviceName, String commandName, String scriptFileName, String notes, Set<RDSType> supportedRDSTypes, boolean replicaOnly, boolean clusterOnly ) {
		this.statistic = statistic;
		this.serviceName = serviceName;
		this.commandName = commandName;
		this.scriptFileName = scriptFileName;
		this.notes = notes;
		this.supportedRDSTypes = supportedRDSTypes;
		this.replicaOnly = replicaOnly;
		this.clusterOnly = clusterOnly;

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


	public boolean isClusterOnlyMetric() {
		return clusterOnly;
	}


	@Override
	public Service getService( String dbInstanceId, Set<String> contactGroups ) throws CandlestackPropertiesException {
		long warning = GlobalAWSProperties.getRDSCloudWatchMetricWarningLevel( dbInstanceId, this );
		long critical = GlobalAWSProperties.getRDSCloudWatchMetricCriticalLevel( dbInstanceId, this );
		String command = commandName + "!" + MetricsReaderWriter.sanitizeString( dbInstanceId ) + "!" + warning + "!" + critical;

		String notificationPeriod = GlobalAWSProperties.getRDSServiceNotificationPeriod( dbInstanceId );
		return new Service( serviceName, dbInstanceId, command, notes, notificationPeriod, contactGroups );
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
	public String getName() {
		return name();
	}

}
