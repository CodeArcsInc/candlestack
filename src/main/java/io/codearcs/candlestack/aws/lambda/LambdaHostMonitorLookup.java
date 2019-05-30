package io.codearcs.candlestack.aws.lambda;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.lambda.AWSLambda;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.ScriptFetcher;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.nagios.HostMonitorLookup;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.hosts.Host;
import io.codearcs.candlestack.nagios.object.hosts.HostGroup;

/**
 * Nagios / Lambda Integration class.
 * 
 * @author Amanda
 *
 */
public class LambdaHostMonitorLookup implements HostMonitorLookup {

	private Set<LambdaCloudWatchMetric> lambdaCloudWatchMetrics;

	private AWSLambda lambdaClient;
	
	private Set<String> contactGroups;

	
	public LambdaHostMonitorLookup( Set<String> contactGroups ) throws CandlestackPropertiesException {
		
		this.contactGroups = contactGroups;

		lambdaCloudWatchMetrics = GlobalAWSProperties.getLambdaCloudwatchMetricsToMonitor();

	}
	
	@Override
	public String getName() {
		return LambdaUtil.TYPE_NAME;
	}


	@Override
	public Map<String, InputStream> getMonitorResources() throws CandlestackException {
		
		Map<String, InputStream> resourceMap = new HashMap<>();
		for ( LambdaCloudWatchMetric metric : lambdaCloudWatchMetrics ) {
			resourceMap.put( metric.getScriptFileName(), ScriptFetcher.fetchInputStream( metric.getScriptFileName() ) );
		}
		return resourceMap;
	}

	@Override
	public List<Command> getMonitorCommands(String relativePathToMonitorResource) throws CandlestackException {
		
		List<Command> commands = new ArrayList<>();
		for ( LambdaCloudWatchMetric metric : lambdaCloudWatchMetrics ) {
			commands.add( metric.getMonitorCommand( relativePathToMonitorResource ) );
		}
		return commands;
	}

	@Override
	public List<HostGroup> lookupHostsToMonitor() throws CandlestackException {
		
		// Define the host group object for stand along EC2 instances
		List<HostGroup> hostGroups = new ArrayList<>();
		HostGroup hostGroup = new HostGroup( LambdaUtil.TYPE_NAME, "Lambda Functions" );

		// Lookup the Functions and define the necessary hosts adding them to the host group
		List<LambdaFunctionSpec> functions = LambdaUtil.lookupFunctions( lambdaClient );
		for ( LambdaFunctionSpec function : functions ) {
				hostGroup.addHost( createHostFromFunction( function ) );
		}

		if ( !hostGroup.getHosts().isEmpty() ) {
			hostGroups.add( hostGroup );
		}

		return hostGroups;
	}

	/**
	 * Creates a Host that represents a Lambda Function.
	 * 
	 * @param function
	 * @return
	 * @throws CandlestackPropertiesException
	 */
	private Host createHostFromFunction( LambdaFunctionSpec function ) throws CandlestackPropertiesException {
		
		Host host = new Host( function.getName(), function.getName(), function.getArn(), contactGroups );

		for ( LambdaCloudWatchMetric metric : lambdaCloudWatchMetrics ) {
			host.addService( metric.getService( function.getArn(), contactGroups ) );
		}
		
		return host;
	}
}
