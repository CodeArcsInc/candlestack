package io.codearcs.candlestack.aws.rds;


public class RDSUtil {

	public static final String TYPE_NAME = "aws_rds";


	public static boolean isDBInstanceEligible( String dbInstance, String dbInstancePrefix ) {
		if ( !dbInstancePrefix.isEmpty() && !dbInstance.startsWith( dbInstancePrefix ) ) {
			return false;
		}
		return true;
	}

}
