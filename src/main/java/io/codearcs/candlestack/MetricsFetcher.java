package io.codearcs.candlestack;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class MetricsFetcher extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger( MetricsFetcher.class );

	protected MetricsReaderWriter metricsReaderWriter;

	protected int sleepIntervalMinutes;

	private boolean keepAlive;


	public MetricsFetcher( String name, int sleepIntervalMinutes ) throws CandlestackException {
		super( "MetricsFetcher_" + name );

		this.sleepIntervalMinutes = sleepIntervalMinutes;

		metricsReaderWriter = MetricsReaderWriter.getInstance();
	}


	/**
	 * This method will be routinely called based off of the sleep interval and
	 * it is up to the implementer to fetch and any metric data and save it via
	 * the MetricsWriter. It is also up to the implementer to handle any errors.
	 */
	public abstract void fetchMetrics();


	/**
	 * This method will be called with the MetricsFetcher thread is shutting down
	 * and the implementer should immediately close any open resources when called.
	 */
	protected abstract void close();


	public final void shutdown() {
		keepAlive = false;

		// Wake it up if it is sleeping
		interrupt();
	}


	@Override
	public final void run() {

		keepAlive = true;
		while ( keepAlive ) {

			try {
				fetchMetrics();
			} catch ( Throwable t ) {
				LOGGER.error( getName() + " encountered an unexpected error while performing fetch", t );
			}

			waitForNextFetch();

		}

		close();

	}


	private void waitForNextFetch() {
		if ( keepAlive ) {
			try {
				TimeUnit.MINUTES.sleep( sleepIntervalMinutes );
			} catch ( InterruptedException e ) {
				if ( keepAlive ) {
					LOGGER.warn( getName() + " was interrupted during the sleep interval between fetches", e );
				}
			}
		}
	}

}
