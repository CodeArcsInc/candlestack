package io.codearcs.candlestack.aws;

import java.util.HashSet;
import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.GlobalCandlestackProperties;
import io.codearcs.candlestack.aws.ec2.EC2CloudWatchMetric;
import io.codearcs.candlestack.aws.ec2.EC2MetricbeatMetric;
import io.codearcs.candlestack.aws.elasticbeanstalk.EBCloudWatchMetric;
import io.codearcs.candlestack.aws.rds.RDSCloudWatchMetric;
import io.codearcs.candlestack.aws.s3.S3MetadataMetric;
import io.codearcs.candlestack.aws.sqs.SQSCloudWatchMetric;
import io.codearcs.candlestack.aws.sqs.SQSQueueAttribute;
import io.codearcs.candlestack.nagios.object.timeperiod.TimePeriod;


public class GlobalAWSProperties extends GlobalCandlestackProperties {

	// Should always be static
	private GlobalAWSProperties() {}

	/*
	 * ---------------------------------------
	 * General AWS Properties
	 * ---------------------------------------
	 */

	private static final int DEFAULT_METRICS_FETCHER_SLEEP_MIN = 5,
			DEFAULT_NEW_RESOURCE_MONITOR_DELAY_MIN = 30;

	private static final String DEFAULT_SERVICE_NOTIFICATION_TIME_PERIOD = TimePeriod.getTwentyFourSevenName();

	private static final String REGION_PROPERTY_KEY = "aws.region",
			LOGS_HOST = "aws.logs.host",
			LOGS_AUTH_TOKEN = "aws.logs.authtoken";


	public static String getRegion() throws CandlestackPropertiesException {
		return getStringProperty( REGION_PROPERTY_KEY );
	}


	public static String getLogsHost() throws CandlestackPropertiesException {
		return getStringProperty( LOGS_HOST );
	}


	public static String getLogsAuthToken() throws CandlestackPropertiesException {
		return getStringProperty( LOGS_AUTH_TOKEN );
	}


	/*
	 * ---------------------------------------
	 * Properties related to CloudWatch
	 * ---------------------------------------
	 */
	private static final String CLOUDWATCH_DETAILED_MONITORING_ENABLED = "aws.cloudwatch.detailed.monitoring.enabled";


	public static boolean isCloudWatchDetailedMonitoringEnabled() throws CandlestackPropertiesException {
		return getBooleanProperty( CLOUDWATCH_DETAILED_MONITORING_ENABLED, false );
	}


	/*
	 * ---------------------------------------
	 * Properties related to ElasticBeanstalk
	 * ---------------------------------------
	 */

	private static final String EB_ENVIRONMENT_NAME_PREFIX = "aws.eb.environment.name.prefix",
			EB_ENVIRONMENT_NAME_REGEX = "aws.eb.environment.name.regex",
			EB_METRICS_FETCHER_SLEEP = "aws.eb.metrics.fetcher.sleep.min",
			EB_CLOUDWATCH_METRICS_MONITOR = "aws.eb.cloudwatch.metrics.monitor",
			EB_CLOUDWATCH_METRICS_FETCH = "aws.eb.cloudwatch.metrics.fetch",
			EB_ENABLED = "aws.eb.enabled",
			EB_NEW_RESOURCE_MONITOR_DELAY = "aws.eb.new.resource.monitor.delay.min";


	private static final String EB_CLOUDWATCH_METRIC_WARNING_PREFIX = "aws.eb.cloudwatch.metric.warning.",
			EB_CLOUDWATCH_METRIC_CRITICAL_PREFIX = "aws.eb.cloudwatch.metric.critical.",
			EB_SERVICE_NOTIFICATION_PERIOD_PREFIX = "aws.eb.service.notification.period.";


	public static boolean isEBEnabled() throws CandlestackPropertiesException {
		return getBooleanProperty( EB_ENABLED, false );
	}


	public static String getEBEnvrionmentNamePrefix() throws CandlestackPropertiesException {
		return getStringProperty( EB_ENVIRONMENT_NAME_PREFIX, "" ).trim();
	}


	public static String getEBEnvrionmentNameRegex() throws CandlestackPropertiesException {
		return getStringProperty( EB_ENVIRONMENT_NAME_REGEX, "" ).trim();
	}


	public static String getEBServiceNotificationPeriod( String environmentName ) throws CandlestackPropertiesException {
		return getStringProperty( EB_SERVICE_NOTIFICATION_PERIOD_PREFIX + environmentName, DEFAULT_SERVICE_NOTIFICATION_TIME_PERIOD ).trim();
	}


	public static int getEBMetricsFetcherSleep() throws CandlestackPropertiesException {
		return getIntProperty( EB_METRICS_FETCHER_SLEEP, DEFAULT_METRICS_FETCHER_SLEEP_MIN );
	}


	public static int getEBNewResourceMonitorDelay() throws CandlestackPropertiesException {
		return getIntProperty( EB_NEW_RESOURCE_MONITOR_DELAY, DEFAULT_NEW_RESOURCE_MONITOR_DELAY_MIN );
	}


	public static Set<EBCloudWatchMetric> getEBCloudwatchMetricsToMonitor() throws CandlestackPropertiesException {
		Set<EBCloudWatchMetric> cloudWatchMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( EB_CLOUDWATCH_METRICS_MONITOR, true ) ) {
				cloudWatchMetrics.add( EBCloudWatchMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + EB_CLOUDWATCH_METRICS_MONITOR + "]" );
		}
		return cloudWatchMetrics;
	}


	public static Set<EBCloudWatchMetric> getEBCloudwatchMetricsToFetch() throws CandlestackPropertiesException {
		Set<EBCloudWatchMetric> cloudWatchMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( EB_CLOUDWATCH_METRICS_FETCH, true ) ) {
				cloudWatchMetrics.add( EBCloudWatchMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + EB_CLOUDWATCH_METRICS_FETCH + "]" );
		}
		return cloudWatchMetrics;
	}


	public static long getEBCloudWatchMetricWarningLevel( String environmentName, EBCloudWatchMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( EB_CLOUDWATCH_METRIC_WARNING_PREFIX, metric.name(), environmentName );
	}


	public static long getEBCloudWatchMetricCriticalLevel( String environmentName, EBCloudWatchMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( EB_CLOUDWATCH_METRIC_CRITICAL_PREFIX, metric.name(), environmentName );
	}

	/*
	 * ---------------------------------------
	 * Properties related to EC2
	 * ---------------------------------------
	 */
	private static final String EC2_NAME_PREFIX = "aws.ec2.name.prefix",
			EC2_NAME_REGEX = "aws.ec2.name.regex",
			EC2_METRICS_FETCHER_SLEEP = "aws.ec2.metrics.fetcher.sleep.min",
			EC2_METRICBEAT_METRICS_MONITOR = "aws.ec2.metricbeat.metrics.monitor",
			EC2_CLOUDWATCH_METRICS_MONITOR = "aws.ec2.cloudwatch.metrics.monitor",
			EC2_CLOUDWATCH_METRICS_FETCH = "aws.ec2.cloudwatch.metrics.fetch",
			EC2_ENABLED = "aws.ec2.enabled",
			EC2_NEW_RESOURCE_MONITOR_DELAY = "aws.ec2.new.resource.monitor.delay.min";


	private static final String EC2_METRICBEAT_METRIC_WARNING_PREFIX = "aws.ec2.metricbeat.metric.warning.",
			EC2_METRICBEAT_METRIC_CRITICAL_PREFIX = "aws.ec2.metricbeat.metric.critical.",
			EC2_CLOUDWATCH_METRIC_WARNING_PREFIX = "aws.ec2.cloudwatch.metric.warning.",
			EC2_CLOUDWATCH_METRIC_CRITICAL_PREFIX = "aws.ec2.cloudwatch.metric.critical.",
			EC2_SERVICE_NOTIFICATION_PERIOD_PREFIX = "aws.ec2.service.notification.period.";


	public static boolean isEC2Enabled() throws CandlestackPropertiesException {
		return getBooleanProperty( EC2_ENABLED, false );
	}


	public static String getEC2NamePrefix() throws CandlestackPropertiesException {
		return getStringProperty( EC2_NAME_PREFIX, "" ).trim();
	}


	public static String getEC2NameRegex() throws CandlestackPropertiesException {
		return getStringProperty( EC2_NAME_REGEX, "" ).trim();
	}


	public static String getEC2ServiceNotificationPeriod( String instanceId ) throws CandlestackPropertiesException {
		return getStringProperty( EC2_SERVICE_NOTIFICATION_PERIOD_PREFIX + instanceId, DEFAULT_SERVICE_NOTIFICATION_TIME_PERIOD ).trim();
	}


	public static int getEC2MetricsFetcherSleep() throws CandlestackPropertiesException {
		return getIntProperty( EC2_METRICS_FETCHER_SLEEP, DEFAULT_METRICS_FETCHER_SLEEP_MIN );
	}


	public static int getEC2NewResourceMonitorDelay() throws CandlestackPropertiesException {
		return getIntProperty( EC2_NEW_RESOURCE_MONITOR_DELAY, DEFAULT_NEW_RESOURCE_MONITOR_DELAY_MIN );
	}


	public static Set<EC2MetricbeatMetric> getEC2MetricbeatMetricsToMonitor() throws CandlestackPropertiesException {
		Set<EC2MetricbeatMetric> metricbeatMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( EC2_METRICBEAT_METRICS_MONITOR, true ) ) {
				metricbeatMetrics.add( EC2MetricbeatMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + EC2_METRICBEAT_METRICS_MONITOR + "]" );
		}
		return metricbeatMetrics;
	}


	public static Set<EC2CloudWatchMetric> getEC2CloudwatchMetricsToMonitor() throws CandlestackPropertiesException {
		Set<EC2CloudWatchMetric> cloudWatchMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( EC2_CLOUDWATCH_METRICS_MONITOR, true ) ) {
				cloudWatchMetrics.add( EC2CloudWatchMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + EC2_CLOUDWATCH_METRICS_MONITOR + "]" );
		}
		return cloudWatchMetrics;
	}


	public static Set<EC2CloudWatchMetric> getEC2CloudwatchMetricsToFetch() throws CandlestackPropertiesException {
		Set<EC2CloudWatchMetric> cloudWatchMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( EC2_CLOUDWATCH_METRICS_FETCH, true ) ) {
				cloudWatchMetrics.add( EC2CloudWatchMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + EC2_CLOUDWATCH_METRICS_FETCH + "]" );
		}
		return cloudWatchMetrics;
	}


	public static long getEC2MetricbeatMetricWarningLevel( String instanceId, EC2MetricbeatMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( EC2_METRICBEAT_METRIC_WARNING_PREFIX, metric.name(), instanceId );
	}


	public static long getEC2MetricbeatMetricCriticalLevel( String instanceId, EC2MetricbeatMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( EC2_METRICBEAT_METRIC_CRITICAL_PREFIX, metric.name(), instanceId );
	}


	public static long getEC2CloudWatchMetricWarningLevel( String instanceId, EC2CloudWatchMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( EC2_CLOUDWATCH_METRIC_WARNING_PREFIX, metric.name(), instanceId );
	}


	public static long getEC2CloudWatchMetricCriticalLevel( String instanceId, EC2CloudWatchMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( EC2_CLOUDWATCH_METRIC_CRITICAL_PREFIX, metric.name(), instanceId );
	}

	/*
	 * ---------------------------------------
	 * Properties related to SQS
	 * ---------------------------------------
	 */

	private static final String SQS_QUEUE_NAME_PREFIX = "aws.sqs.queue.name.prefix",
			SQS_QUEUE_NAME_REGEX = "aws.sqs.queue.name.regex",
			SQS_MONITOR_DEAD_LETTER = "aws.sqs.monitor.deadletter",
			SQS_METRICS_FETCHER_SLEEP = "aws.sqs.metrics.fetcher.sleep.min",
			SQS_QUEUE_ATTRIBUTES_MONITOR = "aws.sqs.queue.attributes.monitor",
			SQS_QUEUE_ATTRIBUTES_FETCH = "aws.sqs.queue.attributes.fetch",
			SQS_CLOUDWATCH_METRICS_MONITOR = "aws.sqs.cloudwatch.metrics.monitor",
			SQS_CLOUDWATCH_METRICS_FETCH = "aws.sqs.cloudwatch.metrics.fetch",
			SQS_ENABLED = "aws.sqs.enabled";


	private static final String SQS_QUEUE_ATTRIBUTE_WARNING_PREFIX = "aws.sqs.queue.attribute.warning.",
			SQS_QUEUE_ATTRIBUTE_CRITICAL_PREFIX = "aws.sqs.queue.attribute.critical.",
			SQS_CLOUDWATCH_METRIC_WARNING_PREFIX = "aws.sqs.cloudwatch.metric.warning.",
			SQS_CLOUDWATCH_METRIC_CRITICAL_PREFIX = "aws.sqs.cloudwatch.metric.critical.",
			SQS_SERVICE_NOTIFICATION_PERIOD_PREFIX = "aws.sqs.service.notification.period.";


	public static boolean isSQSEnabled() throws CandlestackPropertiesException {
		return getBooleanProperty( SQS_ENABLED, false );
	}


	public static String getSQSQueueNamePrefix() throws CandlestackPropertiesException {
		return getStringProperty( SQS_QUEUE_NAME_PREFIX, "" ).trim();
	}


	public static String getSQSQueueNameRegex() throws CandlestackPropertiesException {
		return getStringProperty( SQS_QUEUE_NAME_REGEX, "" ).trim();
	}


	public static String getSQSServiceNotificationPeriod( String queueName ) throws CandlestackPropertiesException {
		return getStringProperty( SQS_SERVICE_NOTIFICATION_PERIOD_PREFIX + queueName, DEFAULT_SERVICE_NOTIFICATION_TIME_PERIOD ).trim();
	}


	public static boolean isSQSMonitorDeadLetterEnabled() throws CandlestackPropertiesException {
		return getBooleanProperty( SQS_MONITOR_DEAD_LETTER, true );
	}


	public static int getSQSMetricsFetcherSleep() throws CandlestackPropertiesException {
		return getIntProperty( SQS_METRICS_FETCHER_SLEEP, DEFAULT_METRICS_FETCHER_SLEEP_MIN );
	}


	public static Set<SQSQueueAttribute> getSQSQueueAttributesToMonitor() throws CandlestackPropertiesException {
		Set<SQSQueueAttribute> queueAttributes = new HashSet<>();
		try {
			for ( String attribute : getSetProperty( SQS_QUEUE_ATTRIBUTES_MONITOR, true ) ) {
				queueAttributes.add( SQSQueueAttribute.valueOf( attribute ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + SQS_QUEUE_ATTRIBUTES_MONITOR + "]" );
		}
		return queueAttributes;
	}


	public static Set<SQSQueueAttribute> getSQSQueueAttributesToFetch() throws CandlestackPropertiesException {
		Set<SQSQueueAttribute> queueAttributes = new HashSet<>();
		try {
			for ( String attribute : getSetProperty( SQS_QUEUE_ATTRIBUTES_FETCH, true ) ) {
				queueAttributes.add( SQSQueueAttribute.valueOf( attribute ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + SQS_QUEUE_ATTRIBUTES_FETCH + "]" );
		}
		return queueAttributes;
	}


	public static Set<SQSCloudWatchMetric> getSQSCloudwatchMetricsToMonitor() throws CandlestackPropertiesException {
		Set<SQSCloudWatchMetric> cloudWatchMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( SQS_CLOUDWATCH_METRICS_MONITOR, true ) ) {
				cloudWatchMetrics.add( SQSCloudWatchMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + SQS_CLOUDWATCH_METRICS_MONITOR + "]" );
		}
		return cloudWatchMetrics;
	}


	public static Set<SQSCloudWatchMetric> getSQSCloudwatchMetricsToFetch() throws CandlestackPropertiesException {
		Set<SQSCloudWatchMetric> cloudWatchMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( SQS_CLOUDWATCH_METRICS_FETCH, true ) ) {
				cloudWatchMetrics.add( SQSCloudWatchMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + SQS_CLOUDWATCH_METRICS_FETCH + "]" );
		}
		return cloudWatchMetrics;
	}


	public static long getSQSQueueAttributeWarningLevel( String queueName, SQSQueueAttribute attribute ) throws CandlestackPropertiesException {
		return determineAlertValue( SQS_QUEUE_ATTRIBUTE_WARNING_PREFIX, attribute.name(), queueName );
	}


	public static long getSQSQueueAttributeCriticalLevel( String queueName, SQSQueueAttribute attribute ) throws CandlestackPropertiesException {
		return determineAlertValue( SQS_QUEUE_ATTRIBUTE_CRITICAL_PREFIX, attribute.name(), queueName );
	}


	public static long getSQSCloudWatchMetricWarningLevel( String queueName, SQSCloudWatchMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( SQS_CLOUDWATCH_METRIC_WARNING_PREFIX, metric.name(), queueName );
	}


	public static long getSQSCloudWatchMetricCriticalLevel( String queueName, SQSCloudWatchMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( SQS_CLOUDWATCH_METRIC_CRITICAL_PREFIX, metric.name(), queueName );
	}


	/*
	 * ---------------------------------------
	 * Properties related to RDS
	 * ---------------------------------------
	 */

	private static final String RDS_DBINSTANCE_PREFIX = "aws.rds.dbinstance.prefix",
			RDS_DBINSTANCE_REGEX = "aws.rds.dbinstance.regex",
			RDS_METRICS_FETCHER_SLEEP = "aws.rds.metrics.fetcher.sleep.min",
			RDS_CLOUDWATCH_METRICS_MONITOR = "aws.rds.cloudwatch.metrics.monitor",
			RDS_CLOUDWATCH_METRICS_FETCH = "aws.rds.cloudwatch.metrics.fetch",
			RDS_ENABLED = "aws.rds.enabled",
			RDS_NEW_RESOURCE_MONITOR_DELAY = "aws.eb.new.resource.monitor.delay.min";


	private static final String RDS_CLOUDWATCH_METRIC_WARNING_PREFIX = "aws.rds.cloudwatch.metric.warning.",
			RDS_CLOUDWATCH_METRIC_CRITICAL_PREFIX = "aws.rds.cloudwatch.metric.critical.",
			RDS_SERVICE_NOTIFICATION_PERIOD_PREFIX = "aws.rds.service.notification.period.";


	public static boolean isRDSEnabled() throws CandlestackPropertiesException {
		return getBooleanProperty( RDS_ENABLED, false );
	}


	public static String getRDSDBInstancePrefix() throws CandlestackPropertiesException {
		return getStringProperty( RDS_DBINSTANCE_PREFIX, "" ).trim();
	}


	public static String getRDSDBInstanceRegex() throws CandlestackPropertiesException {
		return getStringProperty( RDS_DBINSTANCE_REGEX, "" ).trim();
	}


	public static String getRDSServiceNotificationPeriod( String dbInstance ) throws CandlestackPropertiesException {
		return getStringProperty( RDS_SERVICE_NOTIFICATION_PERIOD_PREFIX + dbInstance, DEFAULT_SERVICE_NOTIFICATION_TIME_PERIOD ).trim();
	}


	public static int getRDSMetricsFetcherSleep() throws CandlestackPropertiesException {
		return getIntProperty( RDS_METRICS_FETCHER_SLEEP, DEFAULT_METRICS_FETCHER_SLEEP_MIN );
	}


	public static int getRDSNewResourceMonitorDelay() throws CandlestackPropertiesException {
		return getIntProperty( RDS_NEW_RESOURCE_MONITOR_DELAY, DEFAULT_NEW_RESOURCE_MONITOR_DELAY_MIN );
	}


	public static Set<RDSCloudWatchMetric> getRDSCloudwatchMetricsToMonitor() throws CandlestackPropertiesException {
		Set<RDSCloudWatchMetric> cloudWatchMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( RDS_CLOUDWATCH_METRICS_MONITOR, true ) ) {
				cloudWatchMetrics.add( RDSCloudWatchMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + RDS_CLOUDWATCH_METRICS_MONITOR + "]" );
		}
		return cloudWatchMetrics;
	}


	public static Set<RDSCloudWatchMetric> getRDSCloudwatchMetricsToFetch() throws CandlestackPropertiesException {
		Set<RDSCloudWatchMetric> cloudWatchMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( RDS_CLOUDWATCH_METRICS_FETCH, true ) ) {
				cloudWatchMetrics.add( RDSCloudWatchMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + RDS_CLOUDWATCH_METRICS_FETCH + "]" );
		}
		return cloudWatchMetrics;
	}


	public static long getRDSCloudWatchMetricWarningLevel( String dbInstance, RDSCloudWatchMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( RDS_CLOUDWATCH_METRIC_WARNING_PREFIX, metric.name(), dbInstance );
	}


	public static long getRDSCloudWatchMetricCriticalLevel( String dbInstance, RDSCloudWatchMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( RDS_CLOUDWATCH_METRIC_CRITICAL_PREFIX, metric.name(), dbInstance );
	}


	/*
	 * ---------------------------------------
	 * Properties related to S3
	 * ---------------------------------------
	 */

	private static final String S3_LOCATIONS = "aws.s3.locations",
			S3_METRICS_FETCHER_SLEEP = "aws.s3.metrics.fetcher.sleep.min",
			S3_METADATA_METRICS_MONITOR = "aws.s3.metadata.metrics.monitor",
			S3_METADATA_METRICS_FETCH = "aws.s3.metadata.metrics.fetch",
			S3_ENABLED = "aws.s3.enabled";


	private static final String S3_METADATA_METRIC_WARNING_PREFIX = "aws.s3.metadata.metric.warning.",
			S3_METADATA_METRIC_CRITICAL_PREFIX = "aws.s3.metadata.metric.critical.",
			S3_SERVICE_NOTIFICATION_PERIOD_PREFIX = "aws.s3.service.notification.period.";


	public static boolean isS3Enabled() throws CandlestackPropertiesException {
		return getBooleanProperty( S3_ENABLED, false );
	}


	public static int getS3MetricsFetcherSleep() throws CandlestackPropertiesException {
		return getIntProperty( S3_METRICS_FETCHER_SLEEP, DEFAULT_METRICS_FETCHER_SLEEP_MIN );
	}


	public static String getS3Locations() throws CandlestackPropertiesException {
		return getStringProperty( S3_LOCATIONS, "" ).trim();
	}


	public static String getS3ServiceNotificationPeriod( String locationId ) throws CandlestackPropertiesException {
		return getStringProperty( S3_SERVICE_NOTIFICATION_PERIOD_PREFIX + locationId, DEFAULT_SERVICE_NOTIFICATION_TIME_PERIOD ).trim();
	}


	public static Set<S3MetadataMetric> getS3MetadataMetricsToMonitor() throws CandlestackPropertiesException {
		Set<S3MetadataMetric> metadataMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( S3_METADATA_METRICS_MONITOR, true ) ) {
				metadataMetrics.add( S3MetadataMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + S3_METADATA_METRICS_MONITOR + "]" );
		}
		return metadataMetrics;
	}


	public static Set<S3MetadataMetric> getS3MetadataMetricsToFetch() throws CandlestackPropertiesException {
		Set<S3MetadataMetric> metadataMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( S3_METADATA_METRICS_FETCH, true ) ) {
				metadataMetrics.add( S3MetadataMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + S3_METADATA_METRICS_FETCH + "]" );
		}
		return metadataMetrics;
	}


	public static long getS3MetadataMetricWarningLevel( String locationId, S3MetadataMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( S3_METADATA_METRIC_WARNING_PREFIX, metric.name(), locationId );
	}


	public static long getS3MetadataMetricCriticalLevel( String locationId, S3MetadataMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( S3_METADATA_METRIC_CRITICAL_PREFIX, metric.name(), locationId );
	}


	/*
	 * ---------------------------------------
	 * Helper methods
	 * ---------------------------------------
	 */

	private static long determineAlertValue( String propertyKeyPrefix, String propertyKeySuffix, String overrideId ) throws CandlestackPropertiesException {
		long value = getLongProperty( propertyKeyPrefix + overrideId + "." + propertyKeySuffix, (long) -1 );
		if ( value < 0 ) {
			value = getLongProperty( propertyKeyPrefix + "default." + propertyKeySuffix, null );
		}
		return value;
	}

}
