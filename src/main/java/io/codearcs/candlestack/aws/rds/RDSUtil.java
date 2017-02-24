package io.codearcs.candlestack.aws.rds;

import java.util.HashSet;
import java.util.Set;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.DBClusterMember;
import com.amazonaws.services.rds.model.DescribeDBClustersResult;


public class RDSUtil {

	public static final String TYPE_NAME = "aws_rds";


	public static boolean isDBInstanceEligible( String dbInstance, String dbInstancePrefix, String dbInstanceRegex, RDSType rdsType ) {
		boolean eligble = true;
		if ( rdsType == RDSType.UNSUPPORTED ) {
			eligble = false;
		} else if ( !dbInstancePrefix.isEmpty() && !dbInstance.startsWith( dbInstancePrefix ) ) {
			eligble = false;
		} else if ( !dbInstanceRegex.isEmpty() && !dbInstance.matches( dbInstanceRegex ) ) {
			eligble = false;
		}
		return eligble;
	}


	public static Set<String> getReplicaInstances( AmazonRDS rdsClient ) {
		DescribeDBClustersResult dbClusterResults = rdsClient.describeDBClusters();
		Set<String> replicas = new HashSet<>();
		for ( DBCluster cluster : dbClusterResults.getDBClusters() ) {
			for ( DBClusterMember member : cluster.getDBClusterMembers() ) {
				if ( !member.getIsClusterWriter() ) {
					replicas.add( member.getDBInstanceIdentifier() );
				}
			}
		}
		return replicas;
	}

}
