package io.codearcs.candlestack.aws.ec2;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.Instance;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.MetricsFetcher;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchAccessor;


public class EC2MetricsFetcher extends MetricsFetcher {

	private static final Logger LOGGER = LoggerFactory.getLogger( EC2MetricsFetcher.class );

	private AmazonEC2 ec2Client;

	private String namePrefix, nameRegex;

	private Set<EC2CloudWatchMetric> ec2CloudWatchMetrics;

	private CloudWatchAccessor cloudWatchAccessor;


	public EC2MetricsFetcher() throws CandlestackException {
		super( EC2Util.TYPE_NAME, GlobalAWSProperties.getEC2MetricsFetcherSleep() );

		namePrefix = GlobalAWSProperties.getEC2NamePrefix();
		nameRegex = GlobalAWSProperties.getEC2NameRegex();

		ec2CloudWatchMetrics = GlobalAWSProperties.getEC2CloudwatchMetricsToFetch();

		ec2Client = AmazonEC2ClientBuilder.standard().withRegion( GlobalAWSProperties.getRegion() ).build();

		cloudWatchAccessor = CloudWatchAccessor.getInstance();
	}


	@Override
	public void fetchMetrics() {

		try {

			// For each instance fetch the ec2 cloud watch metrics
			List<Instance> instances = EC2Util.lookupElligibleInstances( ec2Client, namePrefix, nameRegex );
			for ( Instance instance : instances ) {

				String instanceId = instance.getInstanceId();
				
				EC2CloudWatchDimensions dimensions = new EC2CloudWatchDimensions();
				dimensions.setInstanceIdDimension( instanceId );
				
				for ( EC2CloudWatchMetric cloudWatchMetric : ec2CloudWatchMetrics ) {
					cloudWatchAccessor.lookupAndSaveMetricData( cloudWatchMetric, dimensions, instanceId, EC2Util.TYPE_NAME );
				}

			}

		} catch ( CandlestackException e ) {
			LOGGER.error( "EC2MetricsFetcher encountered an error while trying to fetch metrics", e );
		}

	}


	@Override
	public void close() {}

}
