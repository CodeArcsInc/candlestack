package io.codearcs.candlestack.aws.sqs;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.ScriptFetcher;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.nagios.HostMonitorLookup;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.hosts.Host;
import io.codearcs.candlestack.nagios.object.hosts.HostGroup;


public class SQSHostMonitorLookup implements HostMonitorLookup {

	private AmazonSQS sqsClient;

	private Set<String> contactGroups;

	private String queueNamePrefix;

	private boolean monitorDeadLetter;

	private Set<SQSCloudWatchMetric> cloudWatchMetrics;

	private Set<SQSQueueAttribute> queueAttributes;


	public SQSHostMonitorLookup( Set<String> contactGroups ) throws CandlestackPropertiesException {

		this.contactGroups = contactGroups;

		queueNamePrefix = GlobalAWSProperties.getSQSQueueNamePrefix();

		monitorDeadLetter = GlobalAWSProperties.isSQSMonitorDeadLetterEnabled();

		cloudWatchMetrics = GlobalAWSProperties.getSQSCloudwatchMetrics();

		queueAttributes = GlobalAWSProperties.getSQSQueueAttributes();

		sqsClient = AmazonSQSClientBuilder.standard().withRegion( GlobalAWSProperties.getRegion() ).build();

	}


	@Override
	public String getName() {
		return SQSUtil.TYPE_NAME;
	}


	@Override
	public Map<String, InputStream> getMonitorResources() throws CandlestackException {
		Map<String, InputStream> resourceMap = new HashMap<>();

		for ( SQSQueueAttribute attribute : queueAttributes ) {
			resourceMap.put( attribute.getScriptFileName(), ScriptFetcher.fetchInputStream( attribute.getScriptFileName() ) );
		}

		for ( SQSCloudWatchMetric metric : cloudWatchMetrics ) {
			resourceMap.put( metric.getScriptFileName(), ScriptFetcher.fetchInputStream( metric.getScriptFileName() ) );
		}

		return resourceMap;
	}


	@Override
	public List<Command> getMonitorCommands( String relativePathToMonitorResource ) {
		List<Command> commands = new ArrayList<>();

		for ( SQSQueueAttribute attribute : queueAttributes ) {
			commands.add( attribute.getMonitorCommand( relativePathToMonitorResource ) );
		}

		for ( SQSCloudWatchMetric metric : cloudWatchMetrics ) {
			commands.add( metric.getMonitorCommand( relativePathToMonitorResource ) );
		}

		return commands;
	}


	@Override
	public List<HostGroup> lookupHostsToMonitor() throws CandlestackPropertiesException {

		List<HostGroup> hostGroups = new ArrayList<>();
		HostGroup sqsHostGroup = new HostGroup( SQSUtil.TYPE_NAME, "AWS SQS Queues" );
		hostGroups.add( sqsHostGroup );

		// First fetch the list of queue URLs
		List<String> queueUrls = sqsClient.listQueues().getQueueUrls();

		for ( String sqsQueueUrl : queueUrls ) {

			// Extract the queue name from the URL and see if we want to monitor it
			String queueName = SQSUtil.getQueueNameFromURL( sqsQueueUrl );
			if ( !SQSUtil.isQueueEligible( queueName, queueNamePrefix, monitorDeadLetter ) ) {
				continue;
			}

			sqsHostGroup.addHost( createHostForQueue( sqsQueueUrl, queueName ) );

		}

		return hostGroups;

	}


	private Host createHostForQueue( String sqsQueueUrl, String queueName ) throws CandlestackPropertiesException {

		Host host = new Host( queueName, "", sqsQueueUrl, contactGroups );

		for ( SQSQueueAttribute queueAttribute : queueAttributes ) {
			host.addService( queueAttribute.getService( queueName, contactGroups ) );
		}

		for ( SQSCloudWatchMetric metric : cloudWatchMetrics ) {
			host.addService( metric.getService( queueName, contactGroups ) );
		}

		return host;

	}
}
