package io.codearcs.candlestack.aws.sqs;

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.MetricsFetcher;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchAccessor;


public class SQSMetricsFetcher extends MetricsFetcher {

	private static final Logger LOGGER = LoggerFactory.getLogger( SQSMetricsFetcher.class );

	private AmazonSQS sqsClient;

	private String queueNamePrefix, queueNameRegex;

	private boolean monitorDeadLetter;

	private List<String> queueAttributes;

	private Set<SQSCloudWatchMetric> cloudWatchMetrics;

	private CloudWatchAccessor cloudWatchAccessor;


	public SQSMetricsFetcher() throws CandlestackException {
		super( SQSUtil.TYPE_NAME, GlobalAWSProperties.getSQSMetricsFetcherSleep() );

		queueNamePrefix = GlobalAWSProperties.getSQSQueueNamePrefix();
		queueNameRegex = GlobalAWSProperties.getSQSQueueNameRegex();

		monitorDeadLetter = GlobalAWSProperties.isSQSMonitorDeadLetterEnabled();

		queueAttributes = GlobalAWSProperties.getSQSQueueAttributesToFetch().stream().map( attribute -> attribute.name() ).collect( Collectors.toList() );

		cloudWatchMetrics = GlobalAWSProperties.getSQSCloudwatchMetricsToFetch();

		sqsClient = AmazonSQSClientBuilder.standard().withRegion( GlobalAWSProperties.getRegion() ).build();

		cloudWatchAccessor = CloudWatchAccessor.getInstance();
	}


	@Override
	public void fetchMetrics() {

		try {

			// First fetch the list of queue URLs
			List<String> queueUrls = sqsClient.listQueues().getQueueUrls();

			for ( String sqsQueueUrl : queueUrls ) {

				// Extract the queue name from the URL and see if we want to monitor it
				String queueName = SQSUtil.getQueueNameFromURL( sqsQueueUrl );
				if ( !SQSUtil.isQueueEligible( queueName, queueNamePrefix, queueNameRegex, monitorDeadLetter ) ) {
					continue;
				}

				// Check to see if we should fetch some queue attribute metrics
				if ( !queueAttributes.isEmpty() ) {

					Date now = new Date();
					for ( Entry<String, String> queueAttribute : sqsClient.getQueueAttributes( sqsQueueUrl, queueAttributes ).getAttributes().entrySet() ) {
						if ( NumberUtils.isNumber( queueAttribute.getValue() ) ) {
							metricsReaderWriter.writeMetric( SQSUtil.TYPE_NAME, queueName, now, queueAttribute.getKey(), NumberUtils.createNumber( queueAttribute.getValue() ) );
						} else {
							LOGGER.error( "SQSMetricsFetcher was unable to handle queue attribute [" + queueAttribute.getKey() + "] for queue [" + queueName + "] due to it having a non-number value [" + queueAttribute.getValue() + "]" );
						}
					}

				}
				
				// Check to see if we should fetch some CloudWatch metrics
				if ( !cloudWatchMetrics.isEmpty() ) {
					
					// Create the Dimensions Required
					SQSCloudWatchDimensions dimensions = new SQSCloudWatchDimensions();
					dimensions.setQueueNameDimension( queueName );
					
					for ( SQSCloudWatchMetric cloudWatchMetric : cloudWatchMetrics ) {
						cloudWatchAccessor.lookupAndSaveMetricData( cloudWatchMetric, dimensions, queueName, SQSUtil.TYPE_NAME );
					}
				}
			}

		} catch ( CandlestackException e ) {
			LOGGER.error( "SQSMetricsFetcher encountered an error while trying to fetch metrics", e );
		}

	}


	@Override
	public void close() {}


}
