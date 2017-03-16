package io.codearcs.candlestack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.ec2.EC2HostMonitorLookup;
import io.codearcs.candlestack.aws.ec2.EC2MetricsFetcher;
import io.codearcs.candlestack.aws.elasticbeanstalk.EBHostMonitorLookup;
import io.codearcs.candlestack.aws.elasticbeanstalk.EBMetricsFetcher;
import io.codearcs.candlestack.aws.rds.RDSHostMonitorLookup;
import io.codearcs.candlestack.aws.rds.RDSMetricsFetcher;
import io.codearcs.candlestack.aws.s3.S3HostMonitorLookup;
import io.codearcs.candlestack.aws.s3.S3MetricsFetcher;
import io.codearcs.candlestack.aws.sqs.SQSHostMonitorLookup;
import io.codearcs.candlestack.aws.sqs.SQSMetricsFetcher;
import io.codearcs.candlestack.nagios.GlobalNagiosProperties;
import io.codearcs.candlestack.nagios.HostMonitorLookup;
import io.codearcs.candlestack.nagios.NagiosUpdater;


/**
 * The main Candlestack process responsible for running
 * the various pieces that make up Candlestack
 */
public class Candlestack {

	private static final Logger LOGGER = LoggerFactory.getLogger( Candlestack.class );

	private Set<String> contactGroups;

	private List<MetricsFetcher> metricsFetchers;

	private List<HostMonitorLookup> hostMonitorLookups;

	private NagiosUpdater nagiosUpdater;


	public Candlestack( Properties props ) throws CandlestackException {

		contactGroups = GlobalNagiosProperties.getAllContactGroups().stream().map( group -> group.getName() ).collect( Collectors.toSet() );

		metricsFetchers = new ArrayList<>();
		hostMonitorLookups = new ArrayList<>();

		if ( GlobalAWSProperties.isEBEnabled() ) {
			LOGGER.info( "Candlestack will monitor Elastic Beanstalk" );
			metricsFetchers.add( new EBMetricsFetcher() );
			hostMonitorLookups.add( new EBHostMonitorLookup( contactGroups ) );
		}

		if ( GlobalAWSProperties.isEC2Enabled() ) {
			LOGGER.info( "Candlestack will monitor EC2" );
			metricsFetchers.add( new EC2MetricsFetcher() );
			hostMonitorLookups.add( new EC2HostMonitorLookup( contactGroups ) );
		}


		if ( GlobalAWSProperties.isRDSEnabled() ) {
			LOGGER.info( "Candlestack will monitor RDS" );
			metricsFetchers.add( new RDSMetricsFetcher() );
			hostMonitorLookups.add( new RDSHostMonitorLookup( contactGroups ) );
		}

		if ( GlobalAWSProperties.isSQSEnabled() ) {
			LOGGER.info( "Candlestack will monitor SQS" );
			metricsFetchers.add( new SQSMetricsFetcher() );
			hostMonitorLookups.add( new SQSHostMonitorLookup( contactGroups ) );
		}

		if ( GlobalAWSProperties.isS3Enabled() ) {
			LOGGER.info( "Candlestack will monitor S3" );
			metricsFetchers.add( new S3MetricsFetcher() );
			hostMonitorLookups.add( new S3HostMonitorLookup( contactGroups ) );
		}

		nagiosUpdater = new NagiosUpdater( hostMonitorLookups );

	}


	public void shutdown() {

		for ( MetricsFetcher metricsFetcher : metricsFetchers ) {
			metricsFetcher.shutdown();
			try {
				metricsFetcher.join();
			} catch ( InterruptedException e ) {
				LOGGER.error( "Candlestack got interrupted while waiting for MetricsFetcher [" + metricsFetcher.getName() + "] to join" );
			}
		}

		nagiosUpdater.shutdown();
		try {
			nagiosUpdater.join();
		} catch ( InterruptedException e ) {
			LOGGER.error( "Candlestack got interrupted while waiting for NagiosUpdater to join" );
		}

		MetricsReaderWriter.destroy();

	}


	public void start() {

		// Start the various metrics fetchers
		for ( MetricsFetcher metricsFetcher : metricsFetchers ) {
			metricsFetcher.start();
		}

		// Start the Nagios updater process
		nagiosUpdater.start();

	}

	private static class ShutdownThread extends Thread {

		private Candlestack instance;


		public ShutdownThread( Candlestack instance ) {
			this.instance = instance;
		}


		@Override
		public void run() {
			instance.shutdown();
		}
	}


	/**
	 * Starts the Candlestack process
	 *
	 * @param args
	 *          must provide the location of the ini file for Candlestack to use
	 */
	public static void main( String[] args ) {

		if ( args.length < 1 ) {
			System.out.println( "Must provide location of ini configuration file" );
			LOGGER.error( "Must provide location of ini configuration file" );
			System.exit( -1 );
		}

		File propsFile = new File( args[0] );
		if ( !propsFile.exists() ) {
			System.out.println( "Provided ini file location doesn't exist [" + args[0] + "]" );
			LOGGER.error( "Provided ini file location doesn't exist [" + args[0] + "]" );
			System.exit( -1 );
		}

		Properties props = new Properties();
		try {
			props.load( new FileInputStream( propsFile ) );
		} catch ( IOException e ) {
			System.out.println( "Encountered an error attempting to load properties file [" + propsFile.getAbsolutePath() + "]" );
			e.printStackTrace();
			LOGGER.error( "Encountered an error attempting to load properties file [" + propsFile.getAbsolutePath() + "]", e );
			System.exit( -1 );
		}

		try {

			// Initialize the global properties
			GlobalCandlestackProperties.init( props );

			// Create and start the Candlestack service
			Candlestack candlestack = new Candlestack( props );
			Runtime.getRuntime().addShutdownHook( new ShutdownThread( candlestack ) );
			candlestack.start();

		} catch ( CandlestackException e ) {
			System.out.println( "Encountered an error trying to start up Candlestack processes" );
			e.printStackTrace();
			LOGGER.error( "Encountered an error trying to start up Candlestack processes", e );
		} catch ( Throwable t ) {
			System.out.println( "Encountered an unexpected error trying to start up Candlestack processes" );
			t.printStackTrace();
			LOGGER.error( "Encountered an unexpected error trying to start up Candlestack processes", t );
		}

	}

}
