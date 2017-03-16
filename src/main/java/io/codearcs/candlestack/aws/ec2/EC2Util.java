package io.codearcs.candlestack.aws.ec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.nagios.object.hosts.Host;


public class EC2Util {

	public static final String TYPE_NAME = "aws_ec2";


	public static boolean isInstanceEligible( Instance instance, String namePrefix, String nameRegex ) {

		boolean eligible = true;

		int stateCode = instance.getState().getCode().intValue();
		String name = EC2Util.getTagValue( instance, "Name" );

		if ( stateCode != 16 ) {
			eligible = false;
		} else if ( !namePrefix.isEmpty() && !name.startsWith( namePrefix ) ) {
			eligible = false;
		} else if ( !nameRegex.isEmpty() && !name.matches( nameRegex ) ) {
			eligible = false;
		}

		return eligible;

	}


	public static List<Instance> lookupElligibleInstances( AmazonEC2 ec2Client, String namePrefix, String nameRegex ) {

		// We only care about those EC2 instance that have a Name tag
		DescribeInstancesRequest request = new DescribeInstancesRequest().withFilters( new Filter().withName( "tag-key" ).withValues( "Name" ) );

		// Make the request
		DescribeInstancesResult result = ec2Client.describeInstances( request );

		// Process the results building up a list of EC2 instances that match the criteria
		List<Instance> instances = new ArrayList<>();
		for ( Reservation reservation : result.getReservations() ) {

			for ( Instance instance : reservation.getInstances() ) {

				// We don't want EC2 instances that are part of Elastic Beanstalk since they fall under the Elastic Beanstalk logic
				String environmentName = EC2Util.getTagValue( instance, "elasticbeanstalk:environment-name" );
				if ( !environmentName.isEmpty() ) {
					continue;
				}

				// Check to see if the instance is eligible and add it to the list if so
				if ( EC2Util.isInstanceEligible( instance, namePrefix, nameRegex ) ) {
					instances.add( instance );
				}

			}

		}

		return instances;

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


	public static Host createHostFromInstance( Instance instance, Set<String> contactGroups, Set<EC2CloudWatchMetric> ec2CloudWatchMetrics, Set<EC2GraphiteMetric> ec2GraphiteMetrics ) throws CandlestackPropertiesException {

		// Lookup the alias and create the host object
		String alias = EC2Util.getTagValue( instance, "Name" );
		Host host = new Host( instance.getInstanceId(), alias, instance.getPublicIpAddress(), contactGroups );

		for ( EC2CloudWatchMetric metric : ec2CloudWatchMetrics ) {
			host.addService( metric.getService( instance.getInstanceId(), contactGroups ) );
		}

		for ( EC2GraphiteMetric metric : ec2GraphiteMetrics ) {
			host.addService( metric.getService( instance.getInstanceId(), contactGroups ) );
		}

		return host;

	}

}
