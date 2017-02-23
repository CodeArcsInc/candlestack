package io.codearcs.candlestack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class ScriptFetcher {

	private static final String DIR_PROPERTY_KEY = "scripts.dir",
			DEFAULT_DIR = "/opt/candlestack/scripts/";


	private ScriptFetcher() {
		// Private constructor to prevent initialization of an object
	}


	public static InputStream fetchInputStream( String scriptFileName ) throws CandlestackException {

		String dirStr = GlobalCandlestackProperties.getStringProperty( DIR_PROPERTY_KEY, DEFAULT_DIR ).trim();
		File scriptDir = new File( dirStr );
		if ( !scriptDir.isDirectory() ) {
			throw new CandlestackException( "Scripts directory [" + dirStr + "] does not exist or is not a directory" );
		}

		try {
			return new FileInputStream( new File( scriptDir, scriptFileName ) );
		} catch ( FileNotFoundException e ) {
			throw new CandlestackException( "Script [" + scriptFileName + "] does not exist or is not accesible" );
		}

	}

}
