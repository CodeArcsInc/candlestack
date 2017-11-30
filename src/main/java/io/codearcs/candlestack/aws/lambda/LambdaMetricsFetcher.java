package io.codearcs.candlestack.aws.lambda;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.MetricsFetcher;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchAccessor;

/**
 * Class responsible for fetching the Lambda metrics from cloudwatch and saving this data.
 * 
 * @author Amanda
 *
 */
public class LambdaMetricsFetcher extends MetricsFetcher {

	private static final Logger LOGGER = LoggerFactory.getLogger( LambdaMetricsFetcher.class );

	private Set<LambdaCloudWatchMetric> lambdaCloudWatchMetrics;
	
	private CloudWatchAccessor cloudWatchAccessor;
	
	private AWSLambda lambdaClient;
	
	public LambdaMetricsFetcher() throws CandlestackException {
		super( LambdaUtil.TYPE_NAME, GlobalAWSProperties.getLambdaMetricsFetcherSleep() );
		
		lambdaCloudWatchMetrics = GlobalAWSProperties.getLambdaCloudwatchMetricsToFetch();
		
		cloudWatchAccessor = CloudWatchAccessor.getInstance();
		
		lambdaClient = AWSLambdaClientBuilder.standard().withRegion( GlobalAWSProperties.getRegion() ).build();
	}
	
	@Override
	public void fetchMetrics() {

		try {
			
			List<LambdaFunctionSpec> functionList = LambdaUtil.lookupFunctions( lambdaClient );
			
			for ( LambdaFunctionSpec lambdaFunction : functionList ) {

				// Construct the Dimensions
				LambdaCloudWatchDimensions dimensions = new LambdaCloudWatchDimensions();
				dimensions.setFunctionNameDimension( lambdaFunction.getName() );
				dimensions.setResourceDimension( lambdaFunction.getResource());
				
				for ( LambdaCloudWatchMetric cloudWatchMetric : lambdaCloudWatchMetrics ) {
					
					cloudWatchAccessor.lookupAndSaveMetricData( cloudWatchMetric, dimensions, lambdaFunction.getArn(), LambdaUtil.TYPE_NAME );
				}							
			}
			
		} catch( CandlestackException e ) {
			LOGGER.error( "LambdaMetricsFetcher encountered an error while trying to fetch metrics", e );
		}
		
	}

	@Override
	protected void close() {}

}
