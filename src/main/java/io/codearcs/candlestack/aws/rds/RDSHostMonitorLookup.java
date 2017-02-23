package io.codearcs.candlestack.aws.rds;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.model.DBInstance;
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

		List<HostGroup> hostGroups = new ArrayList<>();
		HostGroup rdsHostGroup = new HostGroup( RDSUtil.TYPE_NAME, "AWS RDS Instances" );
		hostGroups.add( rdsHostGroup );

		DescribeDBInstancesResult dbInstanceResults = rdsClient.describeDBInstances();
		for ( DBInstance dbInstance : dbInstanceResults.getDBInstances() ) {

			String dbInstanceId = dbInstance.getDBInstanceIdentifier();
			if ( !RDSUtil.isDBInstanceEligible( dbInstanceId, dbInstancePrefix, dbInstanceRegex ) ) {
				continue;
			}

			rdsHostGroup.addHost( createHostForDBInstance( dbInstance ) );
		}

		return hostGroups;

	}


	private Host createHostForDBInstance( DBInstance dbInstance ) throws CandlestackPropertiesException {

		Host host = new Host( dbInstance.getDBInstanceIdentifier(), "", dbInstance.getEndpoint().getAddress(), contactGroups );

		for ( RDSCloudWatchMetric metric : cloudWatchMetrics ) {
			host.addService( metric.getService( dbInstance.getDBInstanceIdentifier(), contactGroups ) );
		}

		return host;

	}

}
