package io.codearcs.candlestack.aws.rds;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.DBClusterMember;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBClustersResult;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.MetricsFetcher;
import io.codearcs.candlestack.aws.CandlestackAWSException;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchAccessor;


public class RDSMetricsFetcher extends MetricsFetcher {

	private static final Logger LOGGER = LoggerFactory.getLogger( RDSMetricsFetcher.class );

	private Set<RDSCloudWatchMetric> cloudWatchMetrics;

	private AmazonRDS rdsClient;

	private String dbClusterPrefix, dbClusterRegex, dbInstancePrefix, dbInstanceRegex;

	private CloudWatchAccessor cloudWatchAccessor;


	public RDSMetricsFetcher() throws CandlestackException {
		super( RDSUtil.TYPE_NAME, GlobalAWSProperties.getRDSMetricsFetcherSleep() );

		dbClusterPrefix = GlobalAWSProperties.getRDSDBClusterPrefix();
		dbClusterRegex = GlobalAWSProperties.getRDSDBClusterRegex();
		dbInstancePrefix = GlobalAWSProperties.getRDSDBInstancePrefix();
		dbInstanceRegex = GlobalAWSProperties.getRDSDBInstanceRegex();

		cloudWatchMetrics = GlobalAWSProperties.getRDSCloudwatchMetricsToFetch();

		rdsClient = AmazonRDSClientBuilder.standard().withRegion( GlobalAWSProperties.getRegion() ).build();

		cloudWatchAccessor = CloudWatchAccessor.getInstance();
	}


	@Override
	public void fetchMetrics() {

		try {
			fetchClusterMetrics();
			fetchInstanceMetrics();
		} catch ( CandlestackException e ) {
			LOGGER.error( "RDSMetricsFetcher encountered an error while trying to fetch metrics", e );
		}

	}


	private void fetchClusterMetrics() throws CandlestackAWSException, CandlestackException {
		DescribeDBClustersResult dbClusterResults = rdsClient.describeDBClusters();
		for ( DBCluster dbCluster : dbClusterResults.getDBClusters() ) {

			// Do some initial validation to ensure we should be fetching metrics for this cluster
			String dbClusterId = dbCluster.getDBClusterIdentifier();
			RDSType rdsType = RDSType.getTypeFromEngine( dbCluster.getEngine() );
			if ( !RDSUtil.isDBClusterEligible( dbCluster, dbClusterPrefix, dbClusterRegex, rdsType ) ) {
				continue;
			}

			// Lookup the various metrics for the cluster and its members
			for ( RDSCloudWatchMetric cloudWatchMetric : cloudWatchMetrics ) {

				if ( cloudWatchMetric.isRDSTypeSupported( rdsType ) && cloudWatchMetric.isClusterOnlyMetric() ) { // Cluster level metric

					RDSCloudWatchDimensions clusterDimension = new RDSCloudWatchDimensions();
					clusterDimension.setClusterDimension( dbClusterId );
					if ( cloudWatchMetric == RDSCloudWatchMetric.VolumeBytesUsed ) {
						clusterDimension.setEngineDimension( rdsType.name().toLowerCase() );
					}
					cloudWatchAccessor.lookupAndSaveMetricData( cloudWatchMetric, clusterDimension, dbClusterId, RDSUtil.TYPE_NAME );


				} else if ( cloudWatchMetric.isRDSTypeSupported( rdsType ) ) { // Instance level metric

					for ( DBClusterMember clusterMember : dbCluster.getDBClusterMembers() ) {

						if ( !cloudWatchMetric.isReplicaOnlyMetric() || !clusterMember.isClusterWriter() ) {
							RDSCloudWatchDimensions clusterMemberDimension = new RDSCloudWatchDimensions();
							clusterMemberDimension.setInstanceDimension( clusterMember.getDBInstanceIdentifier() );
							cloudWatchAccessor.lookupAndSaveMetricData( cloudWatchMetric, clusterMemberDimension, clusterMember.getDBInstanceIdentifier(), RDSUtil.TYPE_NAME );
						}

					}

				}

			}

		}
	}


	private void fetchInstanceMetrics() throws CandlestackAWSException, CandlestackException {

		DescribeDBInstancesResult dbInstanceResults = rdsClient.describeDBInstances();
		for ( DBInstance dbInstance : dbInstanceResults.getDBInstances() ) {

			String dbInstanceId = dbInstance.getDBInstanceIdentifier();
			RDSType rdsType = RDSType.getTypeFromEngine( dbInstance.getEngine() );
			if ( !RDSUtil.isDBInstanceEligible( dbInstance, dbInstancePrefix, dbInstanceRegex, rdsType ) ) {
				continue;
			}

			RDSCloudWatchDimensions dimensions = new RDSCloudWatchDimensions();
			dimensions.setInstanceDimension( dbInstanceId );

			for ( RDSCloudWatchMetric cloudWatchMetric : cloudWatchMetrics ) {
				if ( cloudWatchMetric.isRDSTypeSupported( rdsType ) && !cloudWatchMetric.isClusterOnlyMetric() && !cloudWatchMetric.isReplicaOnlyMetric() ) {
					cloudWatchAccessor.lookupAndSaveMetricData( cloudWatchMetric, dimensions, dbInstanceId, RDSUtil.TYPE_NAME );
				}
			}

		}
	}


	@Override
	public void close() {}

}
