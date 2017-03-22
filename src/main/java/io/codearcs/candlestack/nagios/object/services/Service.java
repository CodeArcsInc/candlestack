package io.codearcs.candlestack.nagios.object.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.codearcs.candlestack.nagios.NagiosUtil;
import io.codearcs.candlestack.nagios.object.NagiosObject;
import io.codearcs.candlestack.nagios.object.timeperiod.TimePeriod;


/**
 * Representation of a Nagios Service object definition,
 * {@link https://assets.nagios.com/downloads/nagioscore/docs/nagioscore/4/en/objectdefinitions.html#service}
 */
public class Service implements NagiosObject {

	private final String hostName, description, checkCommand, notes;

	private final Set<String> contactGroups;

	private int maxCheckAttempts, checkInterval, retryInterval, notificationInterval;

	private String checkPeriod, notificationPeriod;

	private boolean notificationsEnabled;


	public Service( String description, String hostName, String checkCommand, String notes, String notificationPeriod, Set<String> contactGroups ) {

		this.description = description;
		this.hostName = hostName;
		this.checkCommand = checkCommand;
		this.notes = notes;
		this.notificationPeriod = notificationPeriod;
		this.contactGroups = Collections.unmodifiableSet( new HashSet<>( contactGroups ) );

		maxCheckAttempts = 3;
		checkInterval = 5;
		retryInterval = 1;
		notificationInterval = 0;
		notificationsEnabled = true;

		checkPeriod = TimePeriod.getTwentyFourSevenName();

	}


	public String getHostName() {
		return hostName;
	}


	public String getDescription() {
		return description;
	}


	public String getCheckCommand() {
		return checkCommand;
	}


	public String getNotes() {
		return notes;
	}


	public int getMaxCheckAttempts() {
		return maxCheckAttempts;
	}


	public int getCheckInterval() {
		return checkInterval;
	}


	public int getRetryInterval() {
		return retryInterval;
	}


	public int getNotificationInterval() {
		return notificationInterval;
	}


	public String getCheckPeriod() {
		return checkPeriod;
	}


	public String getNotificationPeriod() {
		return notificationPeriod;
	}


	public Set<String> getContactGroups() {
		return contactGroups;
	}


	public boolean isNotificationsEnabled() {
		return notificationsEnabled;
	}


	@Override
	public String getObjectDefinitions() {
		StringBuilder sb = new StringBuilder( "define service{\n" );

		sb.append( "\thost_name\t" );
		sb.append( hostName );
		sb.append( "\n" );

		sb.append( "\tservice_description\t" );
		sb.append( description );
		sb.append( "\n" );

		sb.append( "\tnotes\t" );
		sb.append( notes );
		sb.append( "\n" );

		sb.append( "\tcheck_command\t" );
		sb.append( checkCommand );
		sb.append( "\n" );

		sb.append( "\tmax_check_attempts\t" );
		sb.append( maxCheckAttempts );
		sb.append( "\n" );

		sb.append( "\tcheck_interval\t" );
		sb.append( checkInterval );
		sb.append( "\n" );

		sb.append( "\tretry_interval\t" );
		sb.append( retryInterval );
		sb.append( "\n" );

		sb.append( "\tnotification_interval\t" );
		sb.append( notificationInterval );
		sb.append( "\n" );

		sb.append( "\tnotifications_enabled\t" );
		sb.append( notificationsEnabled ? "1" : "0" );
		sb.append( "\n" );

		sb.append( "\tcheck_period\t" );
		sb.append( checkPeriod );
		sb.append( "\n" );

		sb.append( "\tnotification_period\t" );
		sb.append( notificationPeriod );
		sb.append( "\n" );

		sb.append( "\tcontact_groups\t" );
		sb.append( contactGroups.stream().collect( Collectors.joining( "," ) ) );
		sb.append( "\n" );

		sb.append( "}\n\n" );

		return sb.toString();
	}


	public static boolean areEquivalent( List<Service> services1, List<Service> services2 ) {
		boolean equivalent = true;

		if ( services1.size() != services2.size() ) {
			equivalent = false;
		} else {

			for ( Service service1 : services1 ) {

				boolean foundEquivalentService = false;
				for ( Service service2 : services2 ) {
					if ( areEquivalent( service1, service2 ) ) {
						foundEquivalentService = true;
						break;
					}
				}

				if ( !foundEquivalentService ) {
					equivalent = false;
					break;
				}
			}

		}

		return equivalent;
	}


	public static boolean areEquivalent( Service service1, Service service2 ) {
		boolean equivalent = true;

		if ( !service1.getHostName().equals( service2.getHostName() ) ) {
			equivalent = false;
		} else if ( !service1.getDescription().equals( service2.getDescription() ) ) {
			equivalent = false;
		} else if ( !service1.getCheckCommand().equals( service2.getCheckCommand() ) ) {
			equivalent = false;
		} else if ( !service1.getCheckPeriod().equals( service2.getCheckPeriod() ) ) {
			equivalent = false;
		} else if ( !service1.getNotificationPeriod().equals( service2.getNotificationPeriod() ) ) {
			equivalent = false;
		} else if ( service1.getCheckInterval() != service2.getCheckInterval() ) {
			equivalent = false;
		} else if ( service1.getRetryInterval() != service2.getRetryInterval() ) {
			equivalent = false;
		} else if ( service1.getNotificationInterval() != service2.getNotificationInterval() ) {
			equivalent = false;
		} else if ( service1.getMaxCheckAttempts() != service2.getMaxCheckAttempts() ) {
			equivalent = false;
		} else if ( !NagiosUtil.areEquivalent( service1.getContactGroups(), service2.getContactGroups() ) ) {
			equivalent = false;
		}

		return equivalent;
	}


}
