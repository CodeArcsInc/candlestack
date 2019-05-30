package io.codearcs.candlestack.aws.ec2;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.aws.cloudwatch.CloudWatchDimensions;


public class EC2CloudWatchDimensions extends CloudWatchDimensions {

	public void setInstanceIdDimension( String value ) {
		dimensions.add( new Dimension().withName( "InstanceId" ).withValue( value ) );
	}

}
