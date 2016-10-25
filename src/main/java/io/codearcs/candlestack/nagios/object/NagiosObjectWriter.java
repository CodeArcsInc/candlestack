package io.codearcs.candlestack.nagios.object;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import io.codearcs.candlestack.nagios.CandlestackNagiosException;


public class NagiosObjectWriter {

	private NagiosObjectWriter() {};


	public static void writeToFile( File file, NagiosObject nagiosObject ) throws CandlestackNagiosException {

		try ( FileWriter fw = new FileWriter( file ); ) {
			fw.write( nagiosObject.getObjectDefinitions() );
			fw.flush();
		} catch ( IOException e ) {
			throw new CandlestackNagiosException( "Encountered an error attempting to write NagiosObject of type [" + nagiosObject.getClass().getName() + "] to file [" + file.getAbsolutePath() + "]", e );
		}

	}


	public static void writeToFile( File file, List<? extends NagiosObject> nagiosObjects ) throws CandlestackNagiosException {

		try ( FileWriter fw = new FileWriter( file ); ) {

			for ( NagiosObject nagiosObject : nagiosObjects ) {
				fw.write( nagiosObject.getObjectDefinitions() );
				fw.write( "\n\n" );
				fw.flush();
			}

		} catch ( IOException e ) {
			throw new CandlestackNagiosException( "Encountered an error attempting to write NagiosObjects to file [" + file.getAbsolutePath() + "]", e );
		}

	}

}
