package io.codearcs.candlestack.aws.cloudwatch;

import java.util.Set;

import com.amazonaws.services.cloudwatch.model.Dimension;

/**
 * Interface that returns the dimensions for a metric
 * 
 * @author Amanda
 *
 */
public interface CloudWatchDimensions {

	Set<Dimension> getDimensions( CloudWatchMetric metric );
	
}
