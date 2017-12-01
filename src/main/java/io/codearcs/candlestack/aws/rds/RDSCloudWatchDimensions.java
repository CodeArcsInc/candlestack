package io.codearcs.candlestack.aws.rds;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.aws.cloudwatch.CloudWatchDimensions;


public class RDSCloudWatchDimensions extends CloudWatchDimensions {


	public void setClusterDimension( String value ) {
		dimensions.add( new Dimension().withName( "DBClusterIdentifier" ).withValue( value ) );
	}


	public void setInstanceDimension( String value ) {
		dimensions.add( new Dimension().withName( "DBInstanceIdentifier" ).withValue( value ) );
	}


	public void setEngineDimension( String value ) {
		dimensions.add( new Dimension().withName( "EngineName" ).withValue( value ) );
	}

}
