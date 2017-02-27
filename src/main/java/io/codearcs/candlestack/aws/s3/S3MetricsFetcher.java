package io.codearcs.candlestack.aws.s3;

import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.MetricsFetcher;
import io.codearcs.candlestack.MetricsReaderWriter;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.rds.RDSMetricsFetcher;


public class S3MetricsFetcher extends MetricsFetcher {

	private static final Logger LOGGER = LoggerFactory.getLogger( RDSMetricsFetcher.class );

	private Set<S3MetadataMetric> metadataMetrics;

	private AmazonS3 s3Client;

	private S3Location[] s3Locations;

	private MetricsReaderWriter metricsReaderWriter;


	public S3MetricsFetcher() throws CandlestackException {
		super( S3Util.TYPE_NAME, GlobalAWSProperties.getS3MetricsFetcherSleep() );

		metadataMetrics = GlobalAWSProperties.getS3MetadataMetricsToFetch();

		s3Client = AmazonS3ClientBuilder.defaultClient();

		s3Locations = S3Util.getS3Locations();

		metricsReaderWriter = MetricsReaderWriter.getInstance();
	}


	@Override
	public void fetchMetrics() {

		try {

			for ( S3Location s3Location : s3Locations ) {
				for ( S3MetadataMetric metadataMetric : metadataMetrics ) {

					ObjectMetadata metadata = s3Client.getObjectMetadata( s3Location.getBucket(), s3Location.getKey() );
					switch ( metadataMetric ) {
						case LastModified :
							Date lastModified = metadata.getLastModified();
							if ( lastModified == null ) {
								continue;
							}
							metricsReaderWriter.writeMetric( S3Util.TYPE_NAME, s3Location.getId(), new Date(), metadataMetric.name(), System.currentTimeMillis() - lastModified.getTime() );
							break;
						default :
							LOGGER.error( "S3MetricsFetcher encountered an unsupported metadata metric [" + metadataMetric + "] while trying to fetch metrics" );
							break;
					}
				}
			}

		} catch ( SdkClientException | CandlestackException e ) {
			LOGGER.error( "S3MetricsFetcher encountered an error while trying to fetch metrics", e );
		}

	}


	@Override
	public void close() {}

}