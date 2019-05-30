package io.codearcs.candlestack.aws.rds;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

	private String dbClusterPrefix, dbClusterRegex, dbInstancePrefix, dbInstanceRegex;

	private long newResourceMonitorDelayMillis;


	public RDSHostMonitorLookup( Set<String> contactGroups ) throws CandlestackPropertiesException {

		this.contactGroups = contactGroups;

		dbClusterPrefix = GlobalAWSProperties.getRDSDBClusterPrefix();
		dbClusterRegex = GlobalAWSProperties.getRDSDBClusterRegex();
		dbInstancePrefix = GlobalAWSProperties.getRDSDBInstancePrefix();
		dbInstanceRegex = GlobalAWSProperties.getRDSDBInstanceRegex();

		newResourceMonitorDelayMillis = TimeUnit.MINUTES.toMillis( GlobalAWSProperties.getRDSNewResourceMonitorDelay() );

		cloudWatchMetrics = GlobalAWSProperties.getRDSCloudwatchMetricsToMonitor();

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

		// Check for clusters and create host groups for them
		hostGroups.addAll( lookupClusterGroups() );

		// Figure out the minimum launch age for the instance to monitored
		Date minLaunchAge = new Date( System.currentTimeMillis() - newResourceMonitorDelayMillis );

		// Get the DB instances and see if any of them are not part of a cluster but should be monitored
		HostGroup nonClusterHostGroup = new HostGroup( "aws_rds_non_cluster", "AWS RDS Non-Clustered Instances" );
		DescribeDBInstancesResult dbInstanceResults = rdsClient.describeDBInstances();
		for ( DBInstance dbInstance : dbInstanceResults.getDBInstances() ) {

			// Make sure the DB instance is eligible
			RDSType rdsType = RDSType.getTypeFromEngine( dbInstance.getEngine() );
			if ( !RDSUtil.isDBInstanceEligible( dbInstance, dbInstancePrefix, dbInstanceRegex, rdsType ) ) {
				continue;
			}

			// Make sure the DB instance is old enough to be monitored
			if ( minLaunchAge.before( dbInstance.getInstanceCreateTime() ) ) {
				continue;
			}

			// Add the instance to the host group
			nonClusterHostGroup.addHost( createHostForDBInstance( dbInstance, rdsType ) );

		}

		// Check to see if the non cluster group has any instances
		if ( !nonClusterHostGroup.getHosts().isEmpty() ) {
			hostGroups.add( nonClusterHostGroup );
		}

		return hostGroups;

	}


	private List<HostGroup> lookupClusterGroups() throws CandlestackPropertiesException {

		DescribeDBClustersResult dbClusterResults = rdsClient.describeDBClusters();
		List<HostGroup> clusterGroups = new ArrayList<>();
		for ( DBCluster dbCluster : dbClusterResults.getDBClusters() ) {

			// Validate the cluster is eligible for monitoring
			RDSType rdsType = RDSType.getTypeFromEngine( dbCluster.getEngine() );
			if ( !RDSUtil.isDBClusterEligible( dbCluster, dbClusterPrefix, dbClusterRegex, rdsType ) ) {
				continue;
			}

			// Create the host group for the cluster
			HostGroup clusterHostGroup = new HostGroup( dbCluster.getDBClusterIdentifier(), dbCluster.getDatabaseName() );

			// Now create a cluster host that will perform cluster level checks
			Host clusterHost = new Host( dbCluster.getDBClusterIdentifier(), "", "", contactGroups );
			for ( RDSCloudWatchMetric metric : cloudWatchMetrics ) {
				if ( metric.isRDSTypeSupported( rdsType ) && metric.isClusterOnlyMetric() ) {
					clusterHost.addService( metric.getService( dbCluster.getDBClusterIdentifier(), contactGroups ) );
				}
			}

			// Check to see if we have any cluster level checks and thus whether or not the cluster host should be added to the cluster host group
			if ( !clusterHost.getServices().isEmpty() ) {
				clusterHostGroup.addHost( clusterHost );
			}

			// Now add the cluster members to the host group
			for ( DBClusterMember clusterMember : dbCluster.getDBClusterMembers() ) {
				clusterHostGroup.addHost( createHostForDBClusterMember( clusterMember, rdsType ) );
			}

			// Add the list of cluster groups
			clusterGroups.add( clusterHostGroup );

		}

		return clusterGroups;

	}


	private Host createHostForDBInstance( DBInstance dbInstance, RDSType rdsType ) throws CandlestackPropertiesException {

		Host host = new Host( dbInstance.getDBInstanceIdentifier(), "", dbInstance.getEndpoint().getAddress(), contactGroups );

		for ( RDSCloudWatchMetric metric : cloudWatchMetrics ) {
			if ( metric.isRDSTypeSupported( rdsType ) && !metric.isClusterOnlyMetric() && !metric.isReplicaOnlyMetric() ) {
				host.addService( metric.getService( dbInstance.getDBInstanceIdentifier(), contactGroups ) );
			}
		}

		return host;

	}


	private Host createHostForDBClusterMember( DBClusterMember dbClusterMember, RDSType rdsType ) throws CandlestackPropertiesException {

		Host host = new Host( dbClusterMember.getDBInstanceIdentifier(), "", "", contactGroups );

		for ( RDSCloudWatchMetric metric : cloudWatchMetrics ) {
			if ( metric.isRDSTypeSupported( rdsType ) && !metric.isClusterOnlyMetric() && ( !metric.isReplicaOnlyMetric() || !dbClusterMember.isClusterWriter() ) ) {
				host.addService( metric.getService( dbClusterMember.getDBInstanceIdentifier(), contactGroups ) );
			}
		}

		return host;

	}

}
