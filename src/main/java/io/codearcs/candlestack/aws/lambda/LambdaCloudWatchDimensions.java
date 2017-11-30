package io.codearcs.candlestack.aws.lambda;

import java.util.HashSet;
import java.util.Set;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.aws.cloudwatch.CloudWatchDimensions;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchMetric;

public class LambdaCloudWatchDimensions implements CloudWatchDimensions {

	private Set<Dimension> dimensions;
	
	public LambdaCloudWatchDimensions() {
		dimensions = new HashSet<Dimension>();
	}
	
	@Override
	public Set<Dimension> getDimensions( CloudWatchMetric metric ) {
		return dimensions;
	}

	public void setResourceDimension( String value ) {
		dimensions.add( new Dimension().withName( "Resource" ).withValue( value ) );
	}
	
	public void setFunctionNameDimension( String value ) {
		dimensions.add( new Dimension().withName( "FunctionName" ).withValue( value ) );		
	}
}
