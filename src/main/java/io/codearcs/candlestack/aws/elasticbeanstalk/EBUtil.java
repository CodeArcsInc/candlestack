package io.codearcs.candlestack.aws.elasticbeanstalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;

import io.codearcs.candlestack.aws.ec2.EC2Util;


public class EBUtil {

	public static final String TYPE_NAME = "aws_eb";


	public static boolean isEnvironmentEligible( EnvironmentDescription environment, String environmentNamePrefix, String environmentNameRegex ) {
		boolean eligible = true;
		if ( !environment.getStatus().equalsIgnoreCase( "Ready" ) ) {
			eligible = false;
		} else if ( !isEnvironmentEligible( environment.getEnvironmentName(), environmentNamePrefix, environmentNameRegex ) ) {
			eligible = false;
		}
		return eligible;
	}


	private static boolean isEnvironmentEligible( String environmentName, String environmentNamePrefix, String environmentNameRegex ) {
		boolean eligible = true;
		if ( !environmentNamePrefix.isEmpty() && !environmentName.startsWith( environmentNamePrefix ) ) {
			eligible = false;
		} else if ( !environmentNameRegex.isEmpty() && !environmentName.matches( environmentNameRegex ) ) {
			eligible = false;
		}
		return eligible;
	}


	public static Map<String, List<Instance>> lookupInstances( AmazonEC2 ec2Client, String environmentNamePrefix, String environmentNameRegex ) {

		// We only care about those EC2 instance created by ElasticBeanstalk
		DescribeInstancesRequest request = new DescribeInstancesRequest().withFilters( new Filter().withName( "tag-key" ).withValues( "elasticbeanstalk:environment-name" ) );

		// Make the request
		DescribeInstancesResult result = ec2Client.describeInstances( request );

		// Process the results building up a Map of environment names to instances
		Map<String, List<Instance>> environmentInstanceMap = new HashMap<>();
		for ( Reservation reservation : result.getReservations() ) {

			for ( Instance instance : reservation.getInstances() ) {

				int stateCode = instance.getState().getCode().intValue();
				if ( stateCode != 16 ) {
					// TODO only care about running instances for now but need to come back and re-evaluate this
					continue;
				}

				String environmentName = EC2Util.getTagValue( instance, "elasticbeanstalk:environment-name" );
				if ( EBUtil.isEnvironmentEligible( environmentName, environmentNamePrefix, environmentNameRegex ) ) {

					List<Instance> instances = environmentInstanceMap.get( environmentName );
					if ( instances == null ) {
						instances = new ArrayList<>();
						environmentInstanceMap.put( environmentName, instances );
					}

					instances.add( instance );

				}

			}

		}

		return environmentInstanceMap;

	}

}
