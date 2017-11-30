package io.codearcs.candlestack.aws.sqs;

import java.util.HashSet;
import java.util.Set;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.aws.cloudwatch.CloudWatchDimensions;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchMetric;

public class SQSCloudWatchDimensions implements CloudWatchDimensions {

	private Set<Dimension> dimensions;
	
	public SQSCloudWatchDimensions() {
		dimensions = new HashSet<Dimension>();
	}

	@Override
	public Set<Dimension> getDimensions( CloudWatchMetric metric ) {
		return dimensions;
	}

	public void setQueueNameDimension( String value ) {
		dimensions.add( new Dimension().withName( "QueueName" ).withValue( value ) );
	}

}
