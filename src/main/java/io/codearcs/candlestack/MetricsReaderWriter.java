package io.codearcs.candlestack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;


/**
 * Singleton object for managing the writing of the metrics
 * data to the file system.
 *
 */
public class MetricsReaderWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger( MetricsReaderWriter.class );

	private static final String DIR_PROPERTY_KEY = "metrics.writer.dir",
			COMPONENT = "candlestack_metric",
			FILE_PREFIX = "candlestack_metrics_",
			FILE_SUFFIX = ".log",
			TIMESTAMP_KEY = "@timestamp",
			COMPONENT_KEY = "component",
			TYPE_KEY = "type",
			INSTANCE_ID_KEY = "instanceId",
			METRIC_NAME_KEY = "metric_name",
			METRIC_VALUE_KEY = "metric_value";

	private static final FastDateFormat FILE_DATE_FORMAT = FastDateFormat.getInstance( "yyyy_MM_dd" ),
			METRIC_DATE_FORMAT = FastDateFormat.getInstance( "yyyy-MM-dd'T'HH:mm:ss.SSSZZ" );


	private static MetricsReaderWriter instance = null;

	private File dir;

	private String currentFileName;

	private Writer writer;


	private MetricsReaderWriter() throws CandlestackException {

		dir = new File( GlobalCandlestackProperties.getStringProperty( DIR_PROPERTY_KEY ) );
		if ( dir.exists() && !dir.isDirectory() ) {
			throw new CandlestackException( "Provided property [" + DIR_PROPERTY_KEY + "] is not a directory [" + dir.getPath() + "]" );
		} else if ( !dir.exists() ) {
			dir.mkdirs();
		}

		currentFileName = getFileName();
		try {
			writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( new File( dir, currentFileName ), true ), "UTF-8" ) );
		} catch ( IOException e ) {
			throw new CandlestackException( "MetricsWriter failed to open file writer for file [" + new File( dir, currentFileName ).getPath() + "]", e );
		}

	}


	public synchronized static MetricsReaderWriter getInstance() throws CandlestackException {
		if ( instance == null ) {
			instance = new MetricsReaderWriter();
		}

		return instance;
	}


	public synchronized static void destroy() {
		if ( instance != null ) {
			instance.close();
			instance = null;
		}
	}


	public synchronized Date readMostRecentMetricDate( String type, String instanceId, String metricName ) throws CandlestackException {

		File lastModifiedFile = lastFileModified();

		Date mostRecentMetricDate = null;
		if ( lastModifiedFile != null ) {
			try ( BufferedReader br = new BufferedReader( new FileReader( lastModifiedFile ) ) ) {

				String sanitizedType = sanitizeString( type );
				String sanitedInstanceId = sanitizeString( instanceId );
				String sanitizedMetricName = sanitizeString( metricName );

				String line;
				while ( ( line = br.readLine() ) != null ) {

					if ( line.contains( sanitizedType ) && line.contains( sanitedInstanceId ) && line.contains( sanitizedMetricName ) ) {

						int colonIndex = line.indexOf( ":", line.indexOf( TIMESTAMP_KEY ) );
						String timestamp = line.substring( colonIndex + 2, line.indexOf( '"', colonIndex + 2 ) );
						Date currentDate = METRIC_DATE_FORMAT.parse( timestamp );
						if ( mostRecentMetricDate == null || currentDate.after( mostRecentMetricDate ) ) {
							mostRecentMetricDate = currentDate;
						}

					}

				}

			} catch ( IOException | ParseException e ) {
				throw new CandlestackException( "Encountered an error attempting to read file [" + lastModifiedFile.getAbsolutePath() + "]", e );
			}
		}

		return mostRecentMetricDate;

	}


	private File lastFileModified() {

		File[] files = dir.listFiles( new FileFilter() {

			@Override
			public boolean accept( File file ) {
				return file.isFile();
			}

		} );

		File choice = null;
		if ( files != null ) {

			long lastMod = Long.MIN_VALUE;
			for ( File file : files ) {
				if ( file.lastModified() > lastMod ) {
					choice = file;
					lastMod = file.lastModified();
				}
			}

		}

		return choice;

	}


	public void writeMetric( String type, String instanceId, Date metricDate, String metricName, Number metricValue ) throws CandlestackException {
		writeMetric( convertMetricData( type, instanceId, metricDate, metricName, metricValue ) );
	}


	private String convertMetricData( String type, String instanceId, Date metricDate, String metricName, Number metricValue ) {

		Map<String, Object> map = new HashMap<>();
		map.put( COMPONENT_KEY, COMPONENT );
		map.put( TYPE_KEY, sanitizeString( type ) );
		map.put( INSTANCE_ID_KEY, sanitizeString( instanceId ) );
		map.put( TIMESTAMP_KEY, METRIC_DATE_FORMAT.format( metricDate ) );
		map.put( METRIC_NAME_KEY, sanitizeString( metricName ) );
		map.put( METRIC_VALUE_KEY, metricValue.longValue() );

		return new Gson().toJson( map );

	}


	public static String sanitizeString( String data ) {
		return data.replace( '-', '_' ).replace( ' ', '_' );
	}


	private void close() {
		if ( writer != null ) {
			try {
				writer.close();
			} catch ( IOException e ) {
				LOGGER.error( "MetricsWriter failed to destroy writer", e );
			}
		}
	}


	private synchronized void writeMetric( String metricData ) throws CandlestackException {

		try {

			String fileName = getFileName();
			if ( writer != null && !currentFileName.equals( fileName ) ) {

				writer.close();
				writer = null;

				currentFileName = fileName;
				writer = new FileWriter( new File( dir, currentFileName ), true );

			} else if ( writer == null ) {

				currentFileName = fileName;
				writer = new FileWriter( new File( dir, currentFileName ), true );

			}

			writer.write( metricData );
			writer.write( "\r\n" );
			writer.flush();

		} catch ( IOException e ) {
			throw new CandlestackException( "MetricsWriter encountered an error attempting to close and open a new writer", e );
		}

	}


	private String getFileName() {
		return FILE_PREFIX + FILE_DATE_FORMAT.format( new Date() ) + FILE_SUFFIX;
	}

}
