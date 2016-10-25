package io.codearcs.candlestack;


public class CandlestackException extends Exception {

	private static final long serialVersionUID = 1L;


	public CandlestackException( String msg ) {
		super( msg );
	}


	public CandlestackException( String msg, Throwable error ) {
		super( msg, error );
	}

}
