package io.codearcs.candlestack.nagios.object.contacts;

import java.util.Set;
import java.util.stream.Collectors;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.nagios.GlobalNagiosProperties;
import io.codearcs.candlestack.nagios.object.NagiosObject;


/**
 * Representation of a Nagios Contact object definition,
 * {@link https://assets.nagios.com/downloads/nagioscore/docs/nagioscore/4/en/objectdefinitions.html#contact}
 */
public class Contact implements NagiosObject {

	private final String name, alias, email;

	private final boolean hostNotificationsEnabled, serviceNotificationsEnabled;

	private final String hostNotificationPeriod, serviceNotificationPeriod;

	private final Set<String> hostNotificationCommands, serviceNotificationCommands;

	private final Set<HostNotificationOption> hostNotificationOptions;

	private final Set<ServiceNotificationOption> serviceNotificationOptions;


	private Contact( ContactBuilder builder ) {
		name = builder.name;
		alias = builder.alias;
		email = builder.email;

		hostNotificationsEnabled = builder.hostNotificationsEnabled;
		serviceNotificationsEnabled = builder.serviceNotificationsEnabled;

		hostNotificationPeriod = builder.hostNotificationPeriod;
		serviceNotificationPeriod = builder.serviceNotificationPeriod;

		hostNotificationOptions = builder.hostNotificationOptions;
		serviceNotificationOptions = builder.serviceNotificationOptions;

		hostNotificationCommands = builder.hostNotificationCommands;
		serviceNotificationCommands = builder.serviceNotificationCommands;
	}


	public String getName() {
		return name;
	}


	public String getAlias() {
		return alias;
	}


	public String getEmail() {
		return email;
	}


	public boolean isHostNotificationsEnabled() {
		return hostNotificationsEnabled;
	}


	public boolean isServiceNotificationsEnabled() {
		return serviceNotificationsEnabled;
	}


	public String getHostNotificationPeriod() {
		return hostNotificationPeriod;
	}


	public String getServiceNotificationPeriod() {
		return serviceNotificationPeriod;
	}


	public Set<HostNotificationOption> getHostNotificationOptions() {
		return hostNotificationOptions;
	}


	public Set<ServiceNotificationOption> getServiceNotificationOptions() {
		return serviceNotificationOptions;
	}


	public Set<String> getHostNotificationCommands() {
		return hostNotificationCommands;
	}


	public Set<String> getServiceNotificationCommands() {
		return serviceNotificationCommands;
	}


	@Override
	public String getObjectDefinitions() {

		StringBuilder sb = new StringBuilder( "define contact{\n" );

		sb.append( "\tcontact_name\t" );
		sb.append( name );
		sb.append( "\n" );

		sb.append( "\talias\t" );
		sb.append( alias );
		sb.append( "\n" );

		sb.append( "\temail\t" );
		sb.append( email );
		sb.append( "\n" );

		sb.append( "\thost_notifications_enabled\t" );
		sb.append( hostNotificationsEnabled ? "1" : "0" );
		sb.append( "\n" );

		sb.append( "\tservice_notifications_enabled\t" );
		sb.append( serviceNotificationsEnabled ? "1" : "0" );
		sb.append( "\n" );

		sb.append( "\thost_notification_period\t" );
		sb.append( hostNotificationPeriod );
		sb.append( "\n" );

		sb.append( "\tservice_notification_period\t" );
		sb.append( serviceNotificationPeriod );
		sb.append( "\n" );

		sb.append( "\thost_notification_options\t" );
		sb.append( hostNotificationOptions.stream().map( option -> option.name() ).collect( Collectors.joining( "," ) ) );
		sb.append( "\n" );

		sb.append( "\tservice_notification_options\t" );
		sb.append( serviceNotificationOptions.stream().map( option -> option.name() ).collect( Collectors.joining( "," ) ) );
		sb.append( "\n" );

		sb.append( "\thost_notification_commands\t" );
		sb.append( hostNotificationCommands.stream().collect( Collectors.joining( "," ) ) );
		sb.append( "\n" );

		sb.append( "\tservice_notification_commands\t" );
		sb.append( serviceNotificationCommands.stream().collect( Collectors.joining( "," ) ) );
		sb.append( "\n" );

		sb.append( "}\n\n" );

		return sb.toString();

	}

	public static class ContactBuilder {

		private final String name, alias, email;

		private boolean hostNotificationsEnabled, serviceNotificationsEnabled;

		private String hostNotificationPeriod, serviceNotificationPeriod;

		private Set<String> hostNotificationCommands, serviceNotificationCommands;

		private Set<HostNotificationOption> hostNotificationOptions;

		private Set<ServiceNotificationOption> serviceNotificationOptions;


		public ContactBuilder( String name, String alias, String email ) throws CandlestackPropertiesException {
			this.name = name;
			this.alias = alias;
			this.email = email;

			hostNotificationsEnabled = GlobalNagiosProperties.isContactHostNotificationsEnabledByDefault();
			serviceNotificationsEnabled = GlobalNagiosProperties.isContactServiceNotificationsEnabledByDefault();

			hostNotificationPeriod = GlobalNagiosProperties.getDefaultContactHostNotificationPeriod();
			serviceNotificationPeriod = GlobalNagiosProperties.getDefaultContactServiceNotificationPeriod();

			hostNotificationCommands = GlobalNagiosProperties.getDefaultContactHostNotificationCommands();
			serviceNotificationCommands = GlobalNagiosProperties.getDefaultContactServiceNotificationCommands();

			hostNotificationOptions = GlobalNagiosProperties.getDefaultContactHostNotificationOptions();
			serviceNotificationOptions = GlobalNagiosProperties.getDefaultContactServiceNotificationOptions();
		}


		public boolean isHostNotificationsEnabled() {
			return hostNotificationsEnabled;
		}


		public ContactBuilder setHostNotificationsEnabled( boolean hostNotificationsEnabled ) {
			this.hostNotificationsEnabled = hostNotificationsEnabled;
			return this;
		}


		public boolean isServiceNotificationsEnabled() {
			return serviceNotificationsEnabled;
		}


		public ContactBuilder setServiceNotificationsEnabled( boolean serviceNotificationsEnabled ) {
			this.serviceNotificationsEnabled = serviceNotificationsEnabled;
			return this;
		}


		public String getHostNotificationPeriod() {
			return hostNotificationPeriod;
		}


		public ContactBuilder setHostNotificationPeriod( String hostNotificationPeriod ) {
			this.hostNotificationPeriod = hostNotificationPeriod;
			return this;
		}


		public String getServiceNotificationPeriod() {
			return serviceNotificationPeriod;
		}


		public ContactBuilder setServiceNotificationPeriod( String serviceNotificationPeriod ) {
			this.serviceNotificationPeriod = serviceNotificationPeriod;
			return this;
		}


		public Set<String> getHostNotificationCommands() {
			return hostNotificationCommands;
		}


		public ContactBuilder setHostNotificationCommands( Set<String> hostNotificationCommands ) {
			this.hostNotificationCommands = hostNotificationCommands;
			return this;
		}


		public Set<String> getServiceNotificationCommands() {
			return serviceNotificationCommands;
		}


		public ContactBuilder setServiceNotificationCommands( Set<String> serviceNotificationCommands ) {
			this.serviceNotificationCommands = serviceNotificationCommands;
			return this;
		}


		public Set<HostNotificationOption> getHostNotificationOptions() {
			return hostNotificationOptions;
		}


		public ContactBuilder setHostNotificationOptions( Set<HostNotificationOption> hostNotificationOptions ) {
			this.hostNotificationOptions = hostNotificationOptions;
			return this;
		}


		public Set<ServiceNotificationOption> getServiceNotificationOptions() {
			return serviceNotificationOptions;
		}


		public ContactBuilder setServiceNotificationOptions( Set<ServiceNotificationOption> serviceNotificationOptions ) {
			this.serviceNotificationOptions = serviceNotificationOptions;
			return this;
		}


		public String getName() {
			return name;
		}


		public String getAlias() {
			return alias;
		}


		public String getEmail() {
			return email;
		}


		public Contact build() {
			return new Contact( this );
		}

	}


}
