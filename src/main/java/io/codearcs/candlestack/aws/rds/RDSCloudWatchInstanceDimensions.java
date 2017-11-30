package io.codearcs.candlestack.aws.rds;

import java.util.HashSet;
import java.util.Set;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.aws.cloudwatch.CloudWatchDimensions;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchMetric;

public class RDSCloudWatchInstanceDimensions implements CloudWatchDimensions {

	private Set<Dimension> dimensions;
	
	public RDSCloudWatchInstanceDimensions() {
		dimensions = new HashSet<Dimension>();
	}
	
	@Override
	public Set<Dimension> getDimensions( CloudWatchMetric metric ) {
		return dimensions;
	}
	
	public void setInstanceDimension( String value ) {
		dimensions.add( new Dimension().withName( "DBInstanceIdentifier" ).withValue( value ) );
	}

}
