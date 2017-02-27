package io.codearcs.candlestack.aws.elasticbeanstalk;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClientBuilder;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.MetricsFetcher;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchAccessor;
import io.codearcs.candlestack.aws.ec2.EC2CloudWatchMetric;
import io.codearcs.candlestack.aws.ec2.EC2Util;


public class EBMetricsFetcher extends MetricsFetcher {

	private static final Logger LOGGER = LoggerFactory.getLogger( EBMetricsFetcher.class );

	private AWSElasticBeanstalk beanstalkClient;

	private AmazonEC2 ec2Client;

	private String environmentNamePrefix, environmentNameRegex;

	private Set<EC2CloudWatchMetric> ec2CloudWatchMetrics;

	private Set<EBCloudWatchMetric> ebCloudWatchMetrics;

	private CloudWatchAccessor cloudWatchAccessor;


	public EBMetricsFetcher() throws CandlestackException {
		super( EBUtil.TYPE_NAME, GlobalAWSProperties.getEBMetricsFetcherSleep() );

		environmentNamePrefix = GlobalAWSProperties.getEBEnvrionmentNamePrefix();
		environmentNameRegex = GlobalAWSProperties.getEBEnvrionmentNameRegex();

		ec2CloudWatchMetrics = GlobalAWSProperties.getEC2CloudwatchMetricsToFetch();
		ebCloudWatchMetrics = GlobalAWSProperties.getEBCloudwatchMetricsToFetch();

		String region = GlobalAWSProperties.getRegion();
		beanstalkClient = AWSElasticBeanstalkClientBuilder.standard().withRegion( region ).build();
		ec2Client = AmazonEC2ClientBuilder.standard().withRegion( region ).build();

		cloudWatchAccessor = CloudWatchAccessor.getInstance();
	}


	@Override
	public void fetchMetrics() {

		try {

			// Go ahead and fetch the EC2 instances in bulk rather than making calls for the individual environments
			Map<String, List<Instance>> environmentInstanceMap = EBUtil.lookupInstances( ec2Client, environmentNamePrefix, environmentNameRegex );

			// Look through the environments for eligible ones
			for ( EnvironmentDescription environment : beanstalkClient.describeEnvironments().getEnvironments() ) {

				// Skip over ineligible environments
				if ( !EBUtil.isEnvironmentEligible( environment, environmentNamePrefix, environmentNameRegex ) ) {
					continue;
				}

				// Retrieve the EB cloud watch metrics
				for ( EBCloudWatchMetric cloudWatchMetric : ebCloudWatchMetrics ) {
					cloudWatchAccessor.lookupAndSaveMetricData( cloudWatchMetric, environment.getEnvironmentName(), EBUtil.TYPE_NAME );
				}

				// For each instance fetch the ec2 cloud watch metrics
				List<Instance> instances = environmentInstanceMap.get( environment.getEnvironmentName() );
				if ( instances != null ) {
					for ( Instance instance : instances ) {
						String instanceId = instance.getInstanceId();
						for ( EC2CloudWatchMetric cloudWatchMetric : ec2CloudWatchMetrics ) {
							cloudWatchAccessor.lookupAndSaveMetricData( cloudWatchMetric, instanceId, EC2Util.TYPE_NAME );
						}
					}
				}

			}

		} catch ( CandlestackException e ) {
			LOGGER.error( "EBMetricsFetcher encountered an error while trying to fetch metrics", e );
		}

	}


	@Override
	public void close() {}


}
