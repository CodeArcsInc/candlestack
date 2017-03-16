package io.codearcs.candlestack.aws.ec2;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.Instance;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.ScriptFetcher;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.nagios.HostMonitorLookup;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.hosts.HostGroup;


public class EC2HostMonitorLookup implements HostMonitorLookup {

	private AmazonEC2 ec2Client;

	private Set<String> contactGroups;

	private String namePrefix, nameRegex;

	private Set<EC2CloudWatchMetric> ec2CloudWatchMetrics;

	private Set<EC2GraphiteMetric> ec2GraphiteMetrics;


	public EC2HostMonitorLookup( Set<String> contactGroups ) throws CandlestackPropertiesException {

		this.contactGroups = contactGroups;

		namePrefix = GlobalAWSProperties.getEC2NamePrefix();
		nameRegex = GlobalAWSProperties.getEC2NameRegex();

		ec2CloudWatchMetrics = GlobalAWSProperties.getEC2CloudwatchMetricsToMonitor();
		ec2GraphiteMetrics = GlobalAWSProperties.getEC2GraphiteMetricsToMonitor();

		ec2Client = AmazonEC2ClientBuilder.standard().withRegion( GlobalAWSProperties.getRegion() ).build();

	}


	@Override
	public String getName() {
		return EC2Util.TYPE_NAME;
	}


	@Override
	public Map<String, InputStream> getMonitorResources() throws CandlestackException {

		Map<String, InputStream> resourceMap = new HashMap<>();

		for ( EC2CloudWatchMetric metric : ec2CloudWatchMetrics ) {
			resourceMap.put( metric.getScriptFileName(), ScriptFetcher.fetchInputStream( metric.getScriptFileName() ) );
		}

		for ( EC2GraphiteMetric metric : ec2GraphiteMetrics ) {
			resourceMap.put( metric.getScriptFileName(), ScriptFetcher.fetchInputStream( metric.getScriptFileName() ) );
		}

		return resourceMap;

	}


	@Override
	public List<Command> getMonitorCommands( String relativePathToMonitorResource ) {

		List<Command> commands = new ArrayList<>();

		for ( EC2CloudWatchMetric metric : ec2CloudWatchMetrics ) {
			commands.add( metric.getMonitorCommand( relativePathToMonitorResource ) );
		}

		for ( EC2GraphiteMetric metric : ec2GraphiteMetrics ) {
			commands.add( metric.getMonitorCommand( relativePathToMonitorResource ) );
		}

		return commands;

	}


	@Override
	public List<HostGroup> lookupHostsToMonitor() throws CandlestackException {

		// Define the host group object for stand along EC2 instances
		List<HostGroup> hostGroups = new ArrayList<>();
		HostGroup hostGroup = new HostGroup( EC2Util.TYPE_NAME, "AWS Standalone EC2 Instances" );
		hostGroups.add( hostGroup );

		// Lookup the EC2 instances and define the necessary hosts adding them to the host group
		List<Instance> instances = EC2Util.lookupElligibleInstances( ec2Client, namePrefix, nameRegex );
		for ( Instance instance : instances ) {
			hostGroup.addHost( EC2Util.createHostFromInstance( instance, contactGroups, ec2CloudWatchMetrics, ec2GraphiteMetrics ) );
		}

		return hostGroups;

	}


}
