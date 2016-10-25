package io.codearcs.candlestack.aws;

import java.util.HashSet;
import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.GlobalCandlestackProperties;
import io.codearcs.candlestack.aws.elasticbeanstalk.EBCloudWatchMetric;
import io.codearcs.candlestack.aws.elasticbeanstalk.EBGraphiteMetric;
import io.codearcs.candlestack.aws.rds.RDSCloudWatchMetric;
import io.codearcs.candlestack.aws.sqs.SQSCloudWatchMetric;
import io.codearcs.candlestack.aws.sqs.SQSQueueAttribute;


public class GlobalAWSProperties extends GlobalCandlestackProperties {

	// Should always be static
	private GlobalAWSProperties() {}

	/*
	 * ---------------------------------------
	 * General AWS Properties
	 * ---------------------------------------
	 */

	private static final int DEFAULT_METRICS_FETCHER_SLEEP_MIN = 5;

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
	 * Properties related to ElasticBeanstalk
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
			EB_METRICS_FETCHER_SLEEP = "aws.eb.metrics.fetcher.sleep.min",
			EB_GRAPHITE_METRICS = "aws.eb.graphite.metrics",
			EB_CLOUDWATCH_METRICS = "aws.eb.cloudwatch.metrics",
			EB_ENABLED = "aws.eb.enabled";


	private static final String EB_GRAPHITE_METRIC_WARNING_PREFIX = "aws.eb.graphite.metric.warning.",
			EB_GRAPHITE_METRIC_CRITICAL_PREFIX = "aws.eb.graphite.metric.critical.",
			EB_CLOUDWATCH_METRIC_WARNING_PREFIX = "aws.eb.cloudwatch.metric.warning.",
			EB_CLOUDWATCH_METRIC_CRITICAL_PREFIX = "aws.eb.cloudwatch.metric.critical.";


	public static boolean isEBEnabled() throws CandlestackPropertiesException {
		return getBooleanProperty( EB_ENABLED, false );
	}


	public static String getEBEnvrionmentNamePrefix() throws CandlestackPropertiesException {
		return getStringProperty( EB_ENVIRONMENT_NAME_PREFIX, "" ).trim();
	}


	public static int getEBMetricsFetcherSleep() throws CandlestackPropertiesException {
		return getIntProperty( EB_METRICS_FETCHER_SLEEP, DEFAULT_METRICS_FETCHER_SLEEP_MIN );
	}


	public static Set<EBGraphiteMetric> getEBGraphiteMetrics() throws CandlestackPropertiesException {
		Set<EBGraphiteMetric> graphiteMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( EB_GRAPHITE_METRICS, true ) ) {
				graphiteMetrics.add( EBGraphiteMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + EB_GRAPHITE_METRICS + "]" );
		}
		return graphiteMetrics;
	}


	public static Set<EBCloudWatchMetric> getEBCloudwatchMetrics() throws CandlestackPropertiesException {
		Set<EBCloudWatchMetric> cloudWatchMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( EB_CLOUDWATCH_METRICS, true ) ) {
				cloudWatchMetrics.add( EBCloudWatchMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + EB_CLOUDWATCH_METRICS + "]" );
		}
		return cloudWatchMetrics;
	}


	public static long getEBGraphiteMetricWarningLevel( String queueName, EBGraphiteMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( EB_GRAPHITE_METRIC_WARNING_PREFIX, metric.name(), queueName );
	}


	public static long getEBGraphiteMetricCriticalLevel( String queueName, EBGraphiteMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( EB_GRAPHITE_METRIC_CRITICAL_PREFIX, metric.name(), queueName );
	}


	public static long getEBCloudWatchMetricWarningLevel( String queueName, EBCloudWatchMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( EB_CLOUDWATCH_METRIC_WARNING_PREFIX, metric.name(), queueName );
	}


	public static long getEBCloudWatchMetricCriticalLevel( String queueName, EBCloudWatchMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( EB_CLOUDWATCH_METRIC_CRITICAL_PREFIX, metric.name(), queueName );
	}

	/*
	 * ---------------------------------------
	 * Properties related to EC2
	 * ---------------------------------------
	 */


	/*
	 * ---------------------------------------
	 * Properties related to SQS
	 * ---------------------------------------
	 */

	private static final String SQS_QUEUE_NAME_PREFIX = "aws.sqs.queue.name.prefix",
			SQS_MONITOR_DEAD_LETTER = "aws.sqs.monitor.deadletter",
			SQS_METRICS_FETCHER_SLEEP = "aws.sqs.metrics.fetcher.sleep.min",
			SQS_QUEUE_ATTRIBUTES = "aws.sqs.queue.attributes",
			SQS_CLOUDWATCH_METRICS = "aws.sqs.cloudwatch.metrics",
			SQS_ENABLED = "aws.sqs.enabled";


	private static final String SQS_QUEUE_ATTRIBUTE_WARNING_PREFIX = "aws.sqs.queue.attribute.warning.",
			SQS_QUEUE_ATTRIBUTE_CRITICAL_PREFIX = "aws.sqs.queue.attribute.critical.",
			SQS_CLOUDWATCH_METRIC_WARNING_PREFIX = "aws.sqs.cloudwatch.metric.warning.",
			SQS_CLOUDWATCH_METRIC_CRITICAL_PREFIX = "aws.sqs.cloudwatch.metric.critical.";


	public static boolean isSQSEnabled() throws CandlestackPropertiesException {
		return getBooleanProperty( SQS_ENABLED, false );
	}


	public static String getSQSQueueNamePrefix() throws CandlestackPropertiesException {
		return getStringProperty( SQS_QUEUE_NAME_PREFIX, "" ).trim();
	}


	public static boolean isSQSMonitorDeadLetterEnabled() throws CandlestackPropertiesException {
		return getBooleanProperty( SQS_MONITOR_DEAD_LETTER, true );
	}


	public static int getSQSMetricsFetcherSleep() throws CandlestackPropertiesException {
		return getIntProperty( SQS_METRICS_FETCHER_SLEEP, DEFAULT_METRICS_FETCHER_SLEEP_MIN );
	}


	public static Set<SQSQueueAttribute> getSQSQueueAttributes() throws CandlestackPropertiesException {
		Set<SQSQueueAttribute> queueAttributes = new HashSet<>();
		try {
			for ( String attribute : getSetProperty( SQS_QUEUE_ATTRIBUTES, true ) ) {
				queueAttributes.add( SQSQueueAttribute.valueOf( attribute ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + SQS_QUEUE_ATTRIBUTES + "]" );
		}
		return queueAttributes;
	}


	public static Set<SQSCloudWatchMetric> getSQSCloudwatchMetrics() throws CandlestackPropertiesException {
		Set<SQSCloudWatchMetric> cloudWatchMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( SQS_CLOUDWATCH_METRICS, true ) ) {
				cloudWatchMetrics.add( SQSCloudWatchMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + SQS_CLOUDWATCH_METRICS + "]" );
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
			RDS_METRICS_FETCHER_SLEEP = "aws.rds.metrics.fetcher.sleep.min",
			RDS_CLOUDWATCH_METRICS = "aws.rds.cloudwatch.metrics",
			RDS_ENABLED = "aws.rds.enabled";


	private static final String RDS_CLOUDWATCH_METRIC_WARNING_PREFIX = "aws.rds.cloudwatch.metric.warning.",
			RDS_CLOUDWATCH_METRIC_CRITICAL_PREFIX = "aws.rds.cloudwatch.metric.critical.";


	public static boolean isRDSEnabled() throws CandlestackPropertiesException {
		return getBooleanProperty( RDS_ENABLED, false );
	}


	public static String getRDSDBInstancePrefix() throws CandlestackPropertiesException {
		return getStringProperty( RDS_DBINSTANCE_PREFIX, "" ).trim();
	}


	public static int getRDSMetricsFetcherSleep() throws CandlestackPropertiesException {
		return getIntProperty( RDS_METRICS_FETCHER_SLEEP, DEFAULT_METRICS_FETCHER_SLEEP_MIN );
	}


	public static Set<RDSCloudWatchMetric> getRDSCloudwatchMetrics() throws CandlestackPropertiesException {
		Set<RDSCloudWatchMetric> cloudWatchMetrics = new HashSet<>();
		try {
			for ( String metric : getSetProperty( RDS_CLOUDWATCH_METRICS, true ) ) {
				cloudWatchMetrics.add( RDSCloudWatchMetric.valueOf( metric ) );
			}
		} catch ( IllegalArgumentException e ) {
			throw new CandlestackPropertiesException( "GlobalAWSProperties was detected an invalid value for property key [" + RDS_CLOUDWATCH_METRICS + "]" );
		}
		return cloudWatchMetrics;
	}


	public static long getRDSCloudWatchMetricWarningLevel( String queueName, RDSCloudWatchMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( RDS_CLOUDWATCH_METRIC_WARNING_PREFIX, metric.name(), queueName );
	}


	public static long getRDSCloudWatchMetricCriticalLevel( String queueName, RDSCloudWatchMetric metric ) throws CandlestackPropertiesException {
		return determineAlertValue( RDS_CLOUDWATCH_METRIC_CRITICAL_PREFIX, metric.name(), queueName );
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
