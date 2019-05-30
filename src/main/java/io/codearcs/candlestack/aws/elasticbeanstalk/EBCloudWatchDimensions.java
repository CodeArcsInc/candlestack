package io.codearcs.candlestack.aws.elasticbeanstalk;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.aws.cloudwatch.CloudWatchDimensions;


public class EBCloudWatchDimensions extends CloudWatchDimensions {

	public void setEnvironmentNameDimension( String value ) {
		dimensions.add( new Dimension().withName( "EnvironmentName" ).withValue( value ) );
	}

}
