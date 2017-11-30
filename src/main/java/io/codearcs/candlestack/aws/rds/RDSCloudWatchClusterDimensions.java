package io.codearcs.candlestack.aws.rds;

import java.util.HashSet;
import java.util.Set;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.aws.cloudwatch.CloudWatchDimensions;
import io.codearcs.candlestack.aws.cloudwatch.CloudWatchMetric;

public class RDSCloudWatchClusterDimensions implements CloudWatchDimensions {

	private Set<Dimension> dimensions;
	
	public RDSCloudWatchClusterDimensions() {
		dimensions = new HashSet<Dimension>();
	}
	
	@Override
	public Set<Dimension> getDimensions( CloudWatchMetric metric ) {
		if ( metric.getName().equals( RDSCloudWatchMetric.VolumeBytesUsed.getName() ) ) {
			Set<Dimension> dimensionSet = new HashSet<Dimension>( dimensions );
			dimensionSet.add( new Dimension().withName( "EngineName" ).withValue( "aurora" ) );
			return dimensionSet;
		} else {
			return dimensions;			
		}
	}
	
	public void setClusterDimension( String value ) {
		dimensions.add( new Dimension().withName( "DBClusterIdentifier" ).withValue( value ) );
	}
	
}
