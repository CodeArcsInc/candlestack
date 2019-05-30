package io.codearcs.candlestack.aws.sqs;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.aws.cloudwatch.CloudWatchDimensions;


public class SQSCloudWatchDimensions extends CloudWatchDimensions {

	public void setQueueNameDimension( String value ) {
		dimensions.add( new Dimension().withName( "QueueName" ).withValue( value ) );
	}

}
