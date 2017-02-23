package io.codearcs.candlestack.aws.rds;


public class RDSUtil {

	public static final String TYPE_NAME = "aws_rds";


	public static boolean isDBInstanceEligible( String dbInstance, String dbInstancePrefix, String dbInstanceRegex ) {
		boolean eligble = true;
		if ( !dbInstancePrefix.isEmpty() && !dbInstance.startsWith( dbInstancePrefix ) ) {
			eligble = false;
		} else if ( !dbInstanceRegex.isEmpty() && !dbInstance.matches( dbInstanceRegex ) ) {
			eligble = false;
		}
		return eligble;
	}

}
