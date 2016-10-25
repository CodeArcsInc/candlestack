package io.codearcs.candlestack.aws.resources;

import java.io.InputStream;


public class AWSResourceFetcher {


	public static InputStream fetchInputStream( String resourceName ) {
		return AWSResourceFetcher.class.getResourceAsStream( resourceName );
	}


	public static InputStream fetchInputStream( Object relativeObject, String resourceName ) {
		return relativeObject.getClass().getResourceAsStream( resourceName );
	}

}
