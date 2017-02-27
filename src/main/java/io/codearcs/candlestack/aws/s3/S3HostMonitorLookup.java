package io.codearcs.candlestack.aws.s3;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.ScriptFetcher;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.nagios.HostMonitorLookup;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.hosts.Host;
import io.codearcs.candlestack.nagios.object.hosts.HostGroup;


public class S3HostMonitorLookup implements HostMonitorLookup {

	private Set<String> contactGroups;

	private Set<S3MetadataMetric> metadataMetrics;

	private S3Location[] s3Locations;


	public S3HostMonitorLookup( Set<String> contactGroups ) throws CandlestackPropertiesException {

		this.contactGroups = contactGroups;

		s3Locations = S3Util.getS3Locations();

		metadataMetrics = GlobalAWSProperties.getS3MetadataMetricsToMonitor();

	}


	@Override
	public String getName() {
		return S3Util.TYPE_NAME;
	}


	@Override
	public Map<String, InputStream> getMonitorResources() throws CandlestackException {
		Map<String, InputStream> resourceMap = new HashMap<>();
		for ( S3MetadataMetric metric : metadataMetrics ) {
			resourceMap.put( metric.getScriptFileName(), ScriptFetcher.fetchInputStream( metric.getScriptFileName() ) );
		}
		return resourceMap;
	}


	@Override
	public List<Command> getMonitorCommands( String relativePathToMonitorResource ) throws CandlestackException {
		List<Command> commands = new ArrayList<>();
		for ( S3MetadataMetric metric : metadataMetrics ) {
			commands.add( metric.getMonitorCommand( relativePathToMonitorResource ) );
		}
		return commands;
	}


	@Override
	public List<HostGroup> lookupHostsToMonitor() throws CandlestackException {

		// Initialize the host groups array
		List<HostGroup> hostGroups = new ArrayList<>();

		if ( s3Locations.length > 0 ) {

			// Initialize the host group
			HostGroup s3HostGroup = new HostGroup( S3Util.TYPE_NAME, "AWS S3 Locations" );
			hostGroups.add( s3HostGroup );

			// Add each location to the host group
			for ( S3Location s3Location : s3Locations ) {

				Host host = new Host( s3Location.getId(), s3Location.getName(), "s3://" + s3Location.getBucket() + "/" + s3Location.getKey(), contactGroups );
				for ( S3MetadataMetric metric : metadataMetrics ) {
					host.addService( metric.getService( s3Location.getId(), contactGroups ) );
				}
				s3HostGroup.addHost( host );

			}
		}

		return hostGroups;

	}

}
