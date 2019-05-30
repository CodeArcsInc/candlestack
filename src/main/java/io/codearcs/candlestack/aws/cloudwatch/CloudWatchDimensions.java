package io.codearcs.candlestack.aws.cloudwatch;

import java.util.HashSet;
import java.util.Set;

import com.amazonaws.services.cloudwatch.model.Dimension;


/**
 * Interface that returns the dimensions for a metric
 *
 * @author Amanda
 *
 */
public abstract class CloudWatchDimensions {

	protected Set<Dimension> dimensions = new HashSet<>();


	public Set<Dimension> getDimensions() {
		return dimensions;
	}

}
