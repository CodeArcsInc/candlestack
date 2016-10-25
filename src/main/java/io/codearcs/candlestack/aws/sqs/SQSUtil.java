package io.codearcs.candlestack.aws.sqs;


public class SQSUtil {

	public static final String TYPE_NAME = "aws_sqs";


	public static String getQueueNameFromURL( String sqsQueueUrl ) {
		return sqsQueueUrl.substring( sqsQueueUrl.lastIndexOf( '/' ) + 1 );
	}


	public static boolean isQueueEligible( String queueName, String queueNamePrefix, boolean deadLetterEnabled ) {
		if ( !deadLetterEnabled && queueName.equals( "dead-letter" ) ) {
			return false;
		} else if ( !queueName.equals( "dead-letter" ) && !queueNamePrefix.isEmpty() && !queueName.startsWith( queueNamePrefix ) ) {
			return false;
		}
		return true;
	}

}
