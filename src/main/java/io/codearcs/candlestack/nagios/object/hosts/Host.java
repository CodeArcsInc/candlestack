package io.codearcs.candlestack.nagios.object.hosts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.codearcs.candlestack.nagios.NagiosUtil;
import io.codearcs.candlestack.nagios.object.NagiosObject;
import io.codearcs.candlestack.nagios.object.services.Service;
import io.codearcs.candlestack.nagios.object.timeperiod.TimePeriod;


/**
 * Representation of a Nagios Host object definition,
 * {@link https://assets.nagios.com/downloads/nagioscore/docs/nagioscore/4/en/objectdefinitions.html#host}
 */
public class Host implements NagiosObject {

	private final String name, alias, address;

	private final Set<String> contactGroups;

	private int maxCheckAttempts, notificationInterval, checkInterval;

	private String checkPeriod, notificationPeriod;

	private List<Service> services;


	public Host( String name, String alias, String address, Set<String> contactGroups ) {
		this.name = name;
		this.alias = alias;
		this.address = address;
		this.contactGroups = Collections.unmodifiableSet( new HashSet<>( contactGroups ) );

		// TODO this things need customization options
		maxCheckAttempts = 5;
		notificationInterval = 10;
		checkInterval = 5;

		checkPeriod = TimePeriod.getTwentyFourSevenName();
		notificationPeriod = TimePeriod.getTwentyFourSevenName();


		services = new ArrayList<>();
	}


	public String getName() {
		return name;
	}


	public String getAlias() {
		return alias;
	}


	public String getAddress() {
		return address;
	}


	public String getCheckPeriod() {
		return checkPeriod;
	}


	public String getNotificationPeriod() {
		return notificationPeriod;
	}


	public int getMaxCheckAttempts() {
		return maxCheckAttempts;
	}


	public int getNotificationInterval() {
		return notificationInterval;
	}


	public int getCheckInterval() {
		return checkInterval;
	}


	public Set<String> getContactGroups() {
		return contactGroups;
	}


	public void addService( Service service ) {
		services.add( service );
	}


	public List<Service> getServices() {
		return services;
	}


	@Override
	public String getObjectDefinitions() {
		StringBuilder sb = new StringBuilder( "define host{\n" );

		sb.append( "\thost_name\t" );
		sb.append( name );
		sb.append( "\n" );

		sb.append( "\talias\t" );
		sb.append( alias );
		sb.append( "\n" );

		sb.append( "\taddress\t" );
		sb.append( address );
		sb.append( "\n" );

		sb.append( "\tmax_check_attempts\t" );
		sb.append( maxCheckAttempts );
		sb.append( "\n" );

		sb.append( "\tcheck_interval\t" );
		sb.append( checkInterval );
		sb.append( "\n" );

		sb.append( "\tcheck_period\t" );
		sb.append( checkPeriod );
		sb.append( "\n" );

		sb.append( "\tnotification_interval\t" );
		sb.append( notificationInterval );
		sb.append( "\n" );

		sb.append( "\tnotification_period\t" );
		sb.append( notificationPeriod );
		sb.append( "\n" );

		sb.append( "\tcontact_groups\t" );
		sb.append( contactGroups.stream().collect( Collectors.joining( "," ) ) );
		sb.append( "\n" );

		sb.append( "}\n\n" );

		for ( Service service : services ) {
			sb.append( service.getObjectDefinitions() );
		}

		return sb.toString();
	}


	public static boolean areEquivalent( List<Host> hosts1, List<Host> hosts2 ) {

		boolean equivalent = true;

		if ( hosts1.size() != hosts2.size() ) {
			equivalent = false;
		} else {

			for ( Host host1 : hosts1 ) {

				boolean foundEquivalentHost = false;
				for ( Host host2 : hosts2 ) {
					if ( areEquivalent( host1, host2 ) ) {
						foundEquivalentHost = true;
						break;
					}
				}

				if ( !foundEquivalentHost ) {
					equivalent = false;
					break;
				}
			}

		}

		return equivalent;

	}


	public static boolean areEquivalent( Host host1, Host host2 ) {

		boolean equivalent = true;

		if ( !host1.getName().equals( host2.getName() ) ) {
			equivalent = false;
		} else if ( !host1.getAlias().equals( host2.getAlias() ) ) {
			equivalent = false;
		} else if ( !host1.getAddress().equals( host2.getAddress() ) ) {
			equivalent = false;
		} else if ( !host1.getCheckPeriod().equals( host2.getCheckPeriod() ) ) {
			equivalent = false;
		} else if ( !host1.getNotificationPeriod().equals( host2.getNotificationPeriod() ) ) {
			equivalent = false;
		} else if ( host1.getMaxCheckAttempts() != host2.getMaxCheckAttempts() ) {
			equivalent = false;
		} else if ( host1.getNotificationInterval() != host2.getNotificationInterval() ) {
			equivalent = false;
		} else if ( host1.getCheckInterval() != host2.getCheckInterval() ) {
			equivalent = false;
		} else if ( !Service.areEquivalent( host1.getServices(), host2.getServices() ) ) {
			equivalent = false;
		} else if ( !NagiosUtil.areEquivalent( host1.getContactGroups(), host2.getContactGroups() ) ) {
			equivalent = false;
		}

		return equivalent;

	}

}
