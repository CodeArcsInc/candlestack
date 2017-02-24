package io.codearcs.candlestack.aws.rds;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.DBClusterMember;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBClustersResult;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.ScriptFetcher;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.nagios.HostMonitorLookup;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.hosts.Host;
import io.codearcs.candlestack.nagios.object.hosts.HostGroup;


public class RDSHostMonitorLookup implements HostMonitorLookup {

	private Set<String> contactGroups;

	private Set<RDSCloudWatchMetric> cloudWatchMetrics;

	private AmazonRDS rdsClient;

	private String dbInstancePrefix, dbInstanceRegex;


	public RDSHostMonitorLookup( Set<String> contactGroups ) throws CandlestackPropertiesException {

		this.contactGroups = contactGroups;

		dbInstancePrefix = GlobalAWSProperties.getRDSDBInstancePrefix();
		dbInstanceRegex = GlobalAWSProperties.getRDSDBInstanceRegex();

		cloudWatchMetrics = GlobalAWSProperties.getRDSCloudwatchMetrics();

		rdsClient = AmazonRDSClientBuilder.standard().withRegion( GlobalAWSProperties.getRegion() ).build();

	}


	@Override
	public String getName() {
		return RDSUtil.TYPE_NAME;
	}


	@Override
	public Map<String, InputStream> getMonitorResources() throws CandlestackException {
		Map<String, InputStream> resourceMap = new HashMap<>();
		for ( RDSCloudWatchMetric metric : cloudWatchMetrics ) {
			resourceMap.put( metric.getScriptFileName(), ScriptFetcher.fetchInputStream( metric.getScriptFileName() ) );
		}
		return resourceMap;
	}


	@Override
	public List<Command> getMonitorCommands( String relativePathToMonitorResource ) throws CandlestackException {
		List<Command> commands = new ArrayList<>();
		for ( RDSCloudWatchMetric metric : cloudWatchMetrics ) {
			commands.add( metric.getMonitorCommand( relativePathToMonitorResource ) );
		}
		return commands;
	}


	@Override
	public List<HostGroup> lookupHostsToMonitor() throws CandlestackException {

		// Initialize the host groups array
		List<HostGroup> hostGroups = new ArrayList<>();

		// Check for clusters and create a hostgroup for them
		DescribeDBClustersResult dbClusterResults = rdsClient.describeDBClusters();
		Map<String, HostGroup> clusterGroups = new HashMap<>();
		for ( DBCluster cluster : dbClusterResults.getDBClusters() ) {
			HostGroup clusterHostGroup = new HostGroup( cluster.getDBClusterIdentifier(), cluster.getDatabaseName() );
			for ( DBClusterMember member : cluster.getDBClusterMembers() ) {
				clusterGroups.put( member.getDBInstanceIdentifier(), clusterHostGroup );
			}
		}

		// Create a host group for non clustered instances
		HostGroup nonClusterHostGroup = new HostGroup( "aws_rds_non_cluster", "AWS RDS Non-Clustered Instances" );

		// Get the DB instances and add them to the correct host group
		Set<String> replicaInstances = RDSUtil.getReplicaInstances( rdsClient );
		DescribeDBInstancesResult dbInstanceResults = rdsClient.describeDBInstances();
		for ( DBInstance dbInstance : dbInstanceResults.getDBInstances() ) {

			// Make sure the DB instance is eligible
			String dbInstanceId = dbInstance.getDBInstanceIdentifier();
			RDSType rdsType = RDSType.getTypeFromEngine( dbInstance.getEngine() );
			if ( !RDSUtil.isDBInstanceEligible( dbInstanceId, dbInstancePrefix, dbInstanceRegex, rdsType ) ) {
				continue;
			}

			// Figure out the correct host group
			HostGroup applicableHostGroup = clusterGroups.get( dbInstanceId );
			if ( applicableHostGroup == null ) {
				applicableHostGroup = nonClusterHostGroup;
			}

			// Add the instance to the host group
			applicableHostGroup.addHost( createHostForDBInstance( dbInstance, rdsType, replicaInstances.contains( dbInstance.getDBInstanceIdentifier() ) ) );

		}

		// Check to see if the non cluster group has any instances
		if ( !nonClusterHostGroup.getHosts().isEmpty() ) {
			hostGroups.add( nonClusterHostGroup );
		}

		// Check the cluster groups to see if it has any instances
		for ( HostGroup clusterGroup : clusterGroups.values() ) {
			if ( !clusterGroup.getHosts().isEmpty() ) {
				hostGroups.add( clusterGroup );
			}
		}

		return hostGroups;

	}


	private Host createHostForDBInstance( DBInstance dbInstance, RDSType rdsType, boolean isReplica ) throws CandlestackPropertiesException {

		Host host = new Host( dbInstance.getDBInstanceIdentifier(), "", dbInstance.getEndpoint().getAddress(), contactGroups );

		for ( RDSCloudWatchMetric metric : cloudWatchMetrics ) {
			if ( metric.isRDSTypeSupported( rdsType ) && ( !metric.isReplicaOnlyMetric() || isReplica ) ) {
				host.addService( metric.getService( dbInstance.getDBInstanceIdentifier(), contactGroups ) );
			}
		}

		return host;

	}

}
