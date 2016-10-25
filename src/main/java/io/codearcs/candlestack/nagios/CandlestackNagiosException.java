package io.codearcs.candlestack.nagios;

import io.codearcs.candlestack.CandlestackException;


/**
 * Exception for errors that occur in the Nagios code
 */
public class CandlestackNagiosException extends CandlestackException {

	private static final long serialVersionUID = 1L;


	public CandlestackNagiosException( String msg ) {
		super( msg );
	}


	public CandlestackNagiosException( String msg, Throwable t ) {
		super( msg, t );
	}

}
