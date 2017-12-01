package io.codearcs.candlestack.aws.lambda;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.aws.cloudwatch.CloudWatchDimensions;


public class LambdaCloudWatchDimensions extends CloudWatchDimensions {


	public void setResourceDimension( String value ) {
		dimensions.add( new Dimension().withName( "Resource" ).withValue( value ) );
	}


	public void setFunctionNameDimension( String value ) {
		dimensions.add( new Dimension().withName( "FunctionName" ).withValue( value ) );
	}

}
