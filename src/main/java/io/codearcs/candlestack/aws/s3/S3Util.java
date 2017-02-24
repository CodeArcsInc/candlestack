package io.codearcs.candlestack.aws.s3;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.aws.GlobalAWSProperties;


public class S3Util {

	public static final String TYPE_NAME = "aws_s3";


	public static S3Location[] getS3Locations() throws CandlestackPropertiesException {
		try {
			return new Gson().fromJson( GlobalAWSProperties.getS3Locations(), S3Location[].class );
		} catch ( JsonSyntaxException e ) {
			throw new CandlestackPropertiesException( "S3 locations property is invalid JSON", e );
		}
	}

}
