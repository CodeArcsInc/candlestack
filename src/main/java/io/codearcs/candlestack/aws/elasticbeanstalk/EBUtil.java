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
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;


public class EBUtil {

	public static final String TYPE_NAME = "aws_eb";


	public static boolean isEnvironmentEligible( EnvironmentDescription environment, String environmentNamePrefix ) {
		boolean eligible = true;
		if ( !environment.getStatus().equalsIgnoreCase( "Ready" ) ) {
			eligible = false;
		} else if ( !environmentNamePrefix.isEmpty() && !environment.getEnvironmentName().startsWith( environmentNamePrefix ) ) {
			eligible = false;
		}
		return eligible;
	}


	public static boolean isEnvironmentEligible( String environmentName, String environmentNamePrefix ) {
		boolean eligible = true;
		if ( !environmentNamePrefix.isEmpty() && !environmentName.startsWith( environmentNamePrefix ) ) {
			eligible = false;
		}
		return eligible;
	}


	public static Map<String, List<Instance>> lookupInstances( AmazonEC2 ec2Client, String environmentNamePrefix ) {

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

				String environmentName = getTagValue( instance, "elasticbeanstalk:environment-name" );
				if ( EBUtil.isEnvironmentEligible( environmentName, environmentNamePrefix ) ) {

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


	public static String getTagValue( Instance instance, String tagKey ) {
		String tagValue = "";
		for ( Tag tag : instance.getTags() ) {
			if ( tag.getKey().equals( tagKey ) ) {
				tagValue = tag.getValue();
				break;
			}
		}
		return tagValue;
	}

}
