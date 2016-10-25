package io.codearcs.candlestack.nagios;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.nagios.object.NagiosObject;
import io.codearcs.candlestack.nagios.object.NagiosObjectWriter;
import io.codearcs.candlestack.nagios.object.hosts.HostGroup;
import io.codearcs.candlestack.nagios.object.timeperiod.TimePeriod;


public class NagiosUpdater extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger( NagiosUpdater.class );

	/*
	 * Defaults
	 */
	private static final int DEFAULT_SLEEP_INTERVAL = 10;

	private static final String DEFAULT_OBJECT_DEFINITION_DIR = "/var/tmp/nagios/objects/";


	/*
	 * Property Keys
	 */
	private static final String PROPERTY_KEY_SLEEP_INTERVAL = "nagios.updater.sleep.interval.min",
			PROPERTY_KEY_OBJECT_DEFINITION_DIR = "nagios.object.definition.dir",
			PROPERTY_KEY_RESTART_CMD = "nagios.updater.restart.cmd";

	/*
	 * Fields
	 */
	private File objectDefinitionDir, dynamicDir;

	private int sleepIntervalMinutes;

	private boolean keepAlive;

	private List<HostMonitorLookup> hostMonitorLookups;

	private Map<HostMonitorLookup, List<HostGroup>> previousHostGroupsPerMonitorLookup;

	private String restartCmd;


	public NagiosUpdater( List<HostMonitorLookup> hostMonitorLookups ) throws CandlestackNagiosException, CandlestackPropertiesException {

		this.hostMonitorLookups = hostMonitorLookups;
		previousHostGroupsPerMonitorLookup = new HashMap<>();

		restartCmd = GlobalNagiosProperties.getStringProperty( PROPERTY_KEY_RESTART_CMD, null );
		LOGGER.info( "NagiosUpdater will use the restart cmd [" + restartCmd + "] for Nagios" );

		sleepIntervalMinutes = GlobalNagiosProperties.getIntProperty( PROPERTY_KEY_SLEEP_INTERVAL, DEFAULT_SLEEP_INTERVAL );
		LOGGER.info( "NagiosUpdater will use a sleep interval of " + sleepIntervalMinutes + " minute(s)" );

		String objectDefinitionDirStr = GlobalNagiosProperties.getStringProperty( PROPERTY_KEY_OBJECT_DEFINITION_DIR, DEFAULT_OBJECT_DEFINITION_DIR ).trim();
		if ( objectDefinitionDirStr.isEmpty() ) {
			throw new CandlestackNagiosException( "Missing required property [" + PROPERTY_KEY_OBJECT_DEFINITION_DIR + "]" );
		}

		objectDefinitionDir = new File( objectDefinitionDirStr );
		if ( !objectDefinitionDir.exists() ) {
			objectDefinitionDir.mkdirs();
		} else if ( !objectDefinitionDir.isDirectory() ) {
			throw new CandlestackNagiosException( "Invalid property [" + PROPERTY_KEY_OBJECT_DEFINITION_DIR + "] due to [" + objectDefinitionDirStr + "] not being a directory" );
		}
		LOGGER.info( "NagiosUpdater will use the directory [" + objectDefinitionDir.getAbsolutePath() + "] for object definition files" );

		dynamicDir = createFreshDir( objectDefinitionDir, "dynamic" );

		generateStaticObjectDefinitions();

	}


	public void shutdown() {
		keepAlive = false;

		// Wake it up if it is sleeping
		interrupt();
	}


	@Override
	public void run() {

		keepAlive = true;
		while ( keepAlive ) {

			try {
				performChecksAndUpdates();
			} catch ( Throwable t ) {
				LOGGER.error( "NagiosUpdater encountered an unexpected error while performing checks and updates", t );
			}

			waitForNextCheckPeriod();

		}

	}


	private void waitForNextCheckPeriod() {
		if ( keepAlive ) {
			try {
				TimeUnit.MINUTES.sleep( sleepIntervalMinutes );
			} catch ( InterruptedException e ) {
				if ( keepAlive ) {
					LOGGER.warn( "NagiosUpdater was interrupted during the sleep interval between checks", e );
				}
			}
		}
	}


	private void performChecksAndUpdates() {

		try {

			boolean restartNagios = false;
			for ( HostMonitorLookup hostMonitorLookup : hostMonitorLookups ) {

				List<HostGroup> previousHostGroups = previousHostGroupsPerMonitorLookup.getOrDefault( hostMonitorLookup, new ArrayList<>() );
				List<HostGroup> currentHostGroups = hostMonitorLookup.lookupHostsToMonitor();

				if ( !HostGroup.areEquivalent( previousHostGroups, currentHostGroups ) ) {

					LOGGER.info( "NagiosUpdater has detected a change for host groups related to [" + hostMonitorLookup.getName() + "]" );
					restartNagios = true;

					// Create the dir for this resource monitor
					File resourceMonitorDir = createFreshDir( dynamicDir, hostMonitorLookup.getName() );

					// Save the resource files
					try {
						Map<String, InputStream> resourcesMap = hostMonitorLookup.getMonitorResources();
						for ( Entry<String, InputStream> resource : resourcesMap.entrySet() ) {

							File resourceFile = new File( resourceMonitorDir, resource.getKey() );
							Files.copy( resource.getValue(), resourceFile.toPath() );

							// TODO for now assume they need to be executable but down the road will likely need to do an extension check
							resourceFile.setExecutable( true, false );

						}
					} catch ( IOException e ) {
						throw new CandlestackNagiosException( "Encountered an error trying to save resourcs files for resource monitor lookup [" + hostMonitorLookup.getName() + "]", e );
					}

					// Save the command object definition file
					NagiosObjectWriter.writeToFile( new File( resourceMonitorDir, "commands.cfg" ), hostMonitorLookup.getMonitorCommands( getPath( resourceMonitorDir ) ) );

					// Save the files for the different host groups
					for ( HostGroup hostGroup : currentHostGroups ) {
						NagiosObjectWriter.writeToFile( new File( resourceMonitorDir, hostGroup.getName() + ".cfg" ), hostGroup );
					}

					previousHostGroupsPerMonitorLookup.put( hostMonitorLookup, currentHostGroups );

				} else {

					LOGGER.info( "NagiosUpdater has detected NO change for host groups related to [" + hostMonitorLookup.getName() + "]" );

				}

			}

			if ( restartNagios ) {
				restartNagios();
			}

		} catch ( CandlestackException e ) {
			LOGGER.error( "NagiosUpdater encountered an error trying to check and update Nagios configuration", e );
		}

	}


	private String getPath( File dir ) {
		String path = dir.getPath().replace( '\\', '/' );
		if ( !path.endsWith( "/" ) ) {
			path = path + "/";
		}
		return path;
	}


	private void generateStaticObjectDefinitions() throws CandlestackNagiosException, CandlestackPropertiesException {

		// Create the static directory fresh
		File staticObjectDefinitionDir = createFreshDir( objectDefinitionDir, "static" );

		// Create the contacts object definition file
		List<NagiosObject> contactsFileObjects = new ArrayList<>();
		contactsFileObjects.addAll( GlobalNagiosProperties.getAllContacts() );
		contactsFileObjects.addAll( GlobalNagiosProperties.getAllContactGroups() );
		NagiosObjectWriter.writeToFile( new File( staticObjectDefinitionDir, "contacts.cfg" ), contactsFileObjects );

		// Create the timeperiods object definition file
		NagiosObjectWriter.writeToFile( new File( staticObjectDefinitionDir, "timeperiods.cfg" ), TimePeriod.getAllDefaultTimePeriods() );

		// Create the commands object definition file
		NagiosObjectWriter.writeToFile( new File( staticObjectDefinitionDir, "commands.cfg" ), GlobalNagiosProperties.getAllCommands() );

	}


	private File createFreshDir( File relativeDir, String name ) throws CandlestackNagiosException {
		File dir = new File( relativeDir, name );
		if ( dir.exists() ) {
			try {
				FileUtils.deleteDirectory( dir );
			} catch ( IOException e ) {
				throw new CandlestackNagiosException( "Encountered an error attempting to delete directory [" + dir.getAbsolutePath() + "] : " + e.getMessage(), e );
			}
		}
		dir.mkdirs();

		return dir;
	}


	private void restartNagios() throws CandlestackNagiosException {

		try {

			LOGGER.info( "Attempting to restart Nagios" );
			Process process = Runtime.getRuntime().exec( restartCmd );
			process.waitFor();
			LOGGER.info( "Finished restarting Nagios" );

		} catch ( IOException e ) {
			throw new CandlestackNagiosException( "Encountered an error attempting to restart Nagios", e );
		} catch ( InterruptedException e ) {
			throw new CandlestackNagiosException( "Was interrupted while waiting for Nagios to restart", e );
		}

	}

}
