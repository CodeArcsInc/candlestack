package io.codearcs.candlestack.aws.rds;

import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.DBInstance;


public class RDSUtil {

	public static final String TYPE_NAME = "aws_rds";


	public static boolean isDBClusterEligible( DBCluster dbCluster, String dbClusterPrefix, String dbClusterRegex, RDSType rdsType ) {
		return isEligible( dbCluster.getDBClusterIdentifier(), dbClusterPrefix, dbClusterRegex, rdsType );
	}


	public static boolean isDBInstanceEligible( DBInstance dbInstance, String dbInstancePrefix, String dbInstanceRegex, RDSType rdsType ) {
		if ( dbInstance.getDBClusterIdentifier() != null && !dbInstance.getDBClusterIdentifier().trim().isEmpty() ) {
			return false;
		}
		return isEligible( dbInstance.getDBInstanceIdentifier(), dbInstancePrefix, dbInstanceRegex, rdsType );
	}


	private static boolean isEligible( String id, String idPrefix, String idRegex, RDSType rdsType ) {
		boolean eligble = true;
		if ( rdsType == RDSType.UNSUPPORTED ) {
			eligble = false;
		} else if ( !idPrefix.isEmpty() && !id.startsWith( idPrefix ) ) {
			eligble = false;
		} else if ( !idRegex.isEmpty() && !id.matches( idRegex ) ) {
			eligble = false;
		}
		return eligble;
	}

}
