package io.codearcs.candlestack.aws;

import io.codearcs.candlestack.CandlestackException;


public class CandlestackAWSException extends CandlestackException {

	private static final long serialVersionUID = 1L;


	public CandlestackAWSException( String msg ) {
		super( msg );
	}


	public CandlestackAWSException( String msg, Throwable error ) {
		super( msg, error );
	}

}
