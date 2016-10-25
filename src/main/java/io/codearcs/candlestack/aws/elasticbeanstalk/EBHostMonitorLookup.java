package io.codearcs.candlestack.aws.elasticbeanstalk;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClientBuilder;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.nagios.HostMonitorLookup;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.hosts.Host;
import io.codearcs.candlestack.nagios.object.hosts.HostGroup;


public class EBHostMonitorLookup implements HostMonitorLookup {

	private AWSElasticBeanstalk beanstalkClient;

	private AmazonEC2 ec2Client;

	private Set<String> contactGroups;

	private String environmentNamePrefix;

	private Set<EBGraphiteMetric> graphiteMetrics;

	private Set<EBCloudWatchMetric> cloudWatchMetrics;


	public EBHostMonitorLookup( Set<String> contactGroups ) throws CandlestackPropertiesException {

		this.contactGroups = contactGroups;

		environmentNamePrefix = GlobalAWSProperties.getEBEnvrionmentNamePrefix();

		graphiteMetrics = GlobalAWSProperties.getEBGraphiteMetrics();

		cloudWatchMetrics = GlobalAWSProperties.getEBCloudwatchMetrics();

		String region = GlobalAWSProperties.getRegion();
		beanstalkClient = AWSElasticBeanstalkClientBuilder.standard().withRegion( region ).build();
		ec2Client = AmazonEC2ClientBuilder.standard().withRegion( region ).build();

	}


	@Override
	public String getName() {
		return EBUtil.TYPE_NAME;
	}


	@Override
	public Map<String, InputStream> getMonitorResources() {

		Map<String, InputStream> resourceMap = new HashMap<>();

		for ( EBGraphiteMetric metric : graphiteMetrics ) {
			resourceMap.put( metric.getResourceName(), metric.getResourceStream() );
		}

		for ( EBCloudWatchMetric metric : cloudWatchMetrics ) {
			resourceMap.put( metric.getResourceName(), metric.getResourceStream() );
		}

		return resourceMap;

	}


	@Override
	public List<Command> getMonitorCommands( String relativePathToMonitorResource ) {

		List<Command> commands = new ArrayList<>();

		for ( EBGraphiteMetric metric : graphiteMetrics ) {
			commands.add( metric.getMonitorCommand( relativePathToMonitorResource ) );
		}

		for ( EBCloudWatchMetric metric : cloudWatchMetrics ) {
			commands.add( metric.getMonitorCommand( relativePathToMonitorResource ) );
		}

		return commands;

	}


	@Override
	public List<HostGroup> lookupHostsToMonitor() throws CandlestackPropertiesException {

		List<HostGroup> hostGroups = new ArrayList<>();

		// Go ahead and fetch the EC2 instances in bulk rather than making calls for the individual environments
		Map<String, List<Instance>> environmentInstanceMap = EBUtil.lookupInstances( ec2Client, environmentNamePrefix );

		// Look through the environments for eligible ones
		for ( EnvironmentDescription environment : beanstalkClient.describeEnvironments().getEnvironments() ) {

			// Skip over ineligible environments
			if ( !EBUtil.isEnvironmentEligible( environment, environmentNamePrefix ) ) {
				continue;
			}

			// Define the host group object for this environment
			HostGroup hostGroup = new HostGroup( environment.getEnvironmentName(), environment.getDescription() );
			hostGroups.add( hostGroup );

			// Add the associated hosts
			List<Instance> instances = environmentInstanceMap.get( environment.getEnvironmentName() );
			if ( instances == null ) {
				// TODO how do we handle environments that have no hosts?
			} else {
				for ( Instance instance : instances ) {
					hostGroup.addHost( createHostFromInstance( instance ) );
				}
			}

		}

		return hostGroups;

	}


	private Host createHostFromInstance( Instance instance ) throws CandlestackPropertiesException {

		// Lookup the alias and create the host object
		String alias = EBUtil.getTagValue( instance, "Name" );
		Host host = new Host( instance.getInstanceId(), alias, instance.getPublicIpAddress(), contactGroups );

		for ( EBGraphiteMetric metric : graphiteMetrics ) {
			host.addService( metric.getService( instance.getInstanceId(), contactGroups ) );
		}

		for ( EBCloudWatchMetric metric : cloudWatchMetrics ) {
			host.addService( metric.getService( instance.getInstanceId(), contactGroups ) );
		}

		return host;

	}

}
