package io.codearcs.candlestack.nagios;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.GlobalCandlestackProperties;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.contacts.Contact;
import io.codearcs.candlestack.nagios.object.contacts.Contact.ContactBuilder;
import io.codearcs.candlestack.nagios.object.contacts.ContactGroup;
import io.codearcs.candlestack.nagios.object.contacts.HostNotificationOption;
import io.codearcs.candlestack.nagios.object.contacts.ServiceNotificationOption;


/**
 * Manages the properties related to Nagios configuration
 * and provides global access to those properties with out
 * having to pass around the properties object throughout
 * the code.
 */
public class GlobalNagiosProperties extends GlobalCandlestackProperties {

	// Should only ever be a static object
	private GlobalNagiosProperties() {}


	private static Set<String> getNames( String propertyKeyPrefix ) {
		return getNames( propertyKeyPrefix, "" );
	}


	@SuppressWarnings( "unchecked" )
	private static Set<String> getNames( String propertyKeyPrefix, String excludePropertyKeyPrefix ) {
		Set<String> names = new HashSet<>();

		Enumeration<String> propNamesEnumeration = (Enumeration<String>) globalProps.propertyNames();
		while ( propNamesEnumeration.hasMoreElements() ) {

			String propName = propNamesEnumeration.nextElement();

			if ( propName.startsWith( propertyKeyPrefix ) && ( excludePropertyKeyPrefix.isEmpty() || !propName.startsWith( excludePropertyKeyPrefix ) ) ) {

				int trailingPeriodIndex = propName.indexOf( ".", propertyKeyPrefix.length() );
				if ( trailingPeriodIndex > 0 ) {
					names.add( propName.substring( propertyKeyPrefix.length(), trailingPeriodIndex ) );
				} else {
					names.add( propName.substring( propertyKeyPrefix.length() ) );
				}

			}

		}
		return names;
	}


	/*
	 * ---------------------------------------
	 * Properties related to Contacts
	 * ---------------------------------------
	 */

	private static final String CONTACT_PROPERTY_KEY_PREFIX = "nagios.contact.",
			CONTACT_DEFAULT_PROPERTY_KEY_PREFIX = "nagios.contact.default",
			CONTACT_DEFAULT_NAME = "default";

	private static final String CONTACT_HOST_NOTIFICATIONS_ENABLED_PROPERTY_KEY_SUFFIX = ".host.notifications.enabled",
			CONTACT_HOST_NOTIFICATION_OPTIONS_PROPERTY_KEY_SUFFIX = ".host.notification.options",
			CONTACT_HOST_NOTIFICATION_COMMANDS_PROPERTY_KEY_SUFFIX = ".host.notification.commands",
			CONTACT_HOST_NOTIFICATION_PERIOD_PROPERTY_KEY_SUFFIX = ".host.notification.period",
			CONTACT_SERVICE_NOTIFICATIONS_ENABLED_PROPERTY_KEY_SUFFIX = ".service.notifications.enabled",
			CONTACT_SERVICE_NOTIFICATION_OPTIONS_PROPERTY_KEY_SUFFIX = ".service.notification.options",
			CONTACT_SERVICE_NOTIFICATION_COMMANDS_PROPERTY_KEY_SUFFIX = ".service.notification.commands",
			CONTACT_SERVICE_NOTIFICATION_PERIOD_PROPERTY_KEY_SUFFIX = ".service.notification.period",
			CONTACT_ALIAS_PROPERTY_KEY_SUFFIX = ".alias",
			CONTACT_EMAIL_PROPERTY_KEY_SUFFIX = ".email";


	public static boolean isContactHostNotificationsEnabledByDefault() throws CandlestackPropertiesException {
		return getBooleanProperty( CONTACT_DEFAULT_PROPERTY_KEY_PREFIX + CONTACT_HOST_NOTIFICATIONS_ENABLED_PROPERTY_KEY_SUFFIX, null );
	}


	public static boolean isContactServiceNotificationsEnabledByDefault() throws CandlestackPropertiesException {
		return getBooleanProperty( CONTACT_DEFAULT_PROPERTY_KEY_PREFIX + CONTACT_SERVICE_NOTIFICATIONS_ENABLED_PROPERTY_KEY_SUFFIX, null );
	}


	public static Set<HostNotificationOption> getDefaultContactHostNotificationOptions() throws CandlestackPropertiesException {
		return getContactHostNotificationOptions( CONTACT_DEFAULT_NAME );
	}


	public static Set<ServiceNotificationOption> getDefaultContactServiceNotificationOptions() throws CandlestackPropertiesException {
		return getContactServiceNotificationOptions( CONTACT_DEFAULT_NAME );
	}


	public static Set<String> getDefaultContactHostNotificationCommands() throws CandlestackPropertiesException {
		return getSetProperty( CONTACT_PROPERTY_KEY_PREFIX + CONTACT_DEFAULT_NAME + CONTACT_HOST_NOTIFICATION_COMMANDS_PROPERTY_KEY_SUFFIX );
	}


	public static Set<String> getDefaultContactServiceNotificationCommands() throws CandlestackPropertiesException {
		return getSetProperty( CONTACT_PROPERTY_KEY_PREFIX + CONTACT_DEFAULT_NAME + CONTACT_SERVICE_NOTIFICATION_COMMANDS_PROPERTY_KEY_SUFFIX );
	}


	public static String getDefaultContactHostNotificationPeriod() throws CandlestackPropertiesException {
		return getStringProperty( CONTACT_PROPERTY_KEY_PREFIX + CONTACT_DEFAULT_NAME + CONTACT_HOST_NOTIFICATION_PERIOD_PROPERTY_KEY_SUFFIX );
	}


	public static String getDefaultContactServiceNotificationPeriod() throws CandlestackPropertiesException {
		return getStringProperty( CONTACT_PROPERTY_KEY_PREFIX + CONTACT_DEFAULT_NAME + CONTACT_SERVICE_NOTIFICATION_PERIOD_PROPERTY_KEY_SUFFIX );
	}


	public static List<Contact> getAllContacts() throws CandlestackPropertiesException {
		List<Contact> contacts = new ArrayList<>();

		// First we have to find what contact names have been defined by searching through the properties
		Set<String> contactNames = getNames( CONTACT_PROPERTY_KEY_PREFIX, CONTACT_DEFAULT_PROPERTY_KEY_PREFIX );

		// Now that we have the set of names we can create the contacts
		for ( String contactName : contactNames ) {
			contacts.add( getContactByName( contactName ) );
		}

		return contacts;
	}


	public static Contact getContactByName( String contactName ) throws CandlestackPropertiesException {

		ContactBuilder contactBuilder = new Contact.ContactBuilder( contactName, getStringProperty( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_ALIAS_PROPERTY_KEY_SUFFIX, "" ), getStringProperty( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_EMAIL_PROPERTY_KEY_SUFFIX, null ) );

		if ( globalProps.containsKey( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_HOST_NOTIFICATIONS_ENABLED_PROPERTY_KEY_SUFFIX ) ) {
			contactBuilder.setHostNotificationsEnabled( getBooleanProperty( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_HOST_NOTIFICATIONS_ENABLED_PROPERTY_KEY_SUFFIX, null ) );
		}

		if ( globalProps.containsKey( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_HOST_NOTIFICATION_OPTIONS_PROPERTY_KEY_SUFFIX ) ) {
			contactBuilder.setHostNotificationOptions( getContactHostNotificationOptions( contactName ) );
		}

		if ( globalProps.containsKey( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_HOST_NOTIFICATION_COMMANDS_PROPERTY_KEY_SUFFIX ) ) {
			contactBuilder.setHostNotificationCommands( getSetProperty( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_HOST_NOTIFICATION_COMMANDS_PROPERTY_KEY_SUFFIX ) );
		}

		if ( globalProps.containsKey( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_HOST_NOTIFICATION_PERIOD_PROPERTY_KEY_SUFFIX ) ) {
			contactBuilder.setHostNotificationPeriod( getStringProperty( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_HOST_NOTIFICATION_PERIOD_PROPERTY_KEY_SUFFIX ) );
		}

		if ( globalProps.containsKey( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_SERVICE_NOTIFICATIONS_ENABLED_PROPERTY_KEY_SUFFIX ) ) {
			contactBuilder.setServiceNotificationsEnabled( getBooleanProperty( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_SERVICE_NOTIFICATIONS_ENABLED_PROPERTY_KEY_SUFFIX, null ) );
		}

		if ( globalProps.containsKey( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_SERVICE_NOTIFICATION_OPTIONS_PROPERTY_KEY_SUFFIX ) ) {
			contactBuilder.setServiceNotificationOptions( getContactServiceNotificationOptions( contactName ) );
		}

		if ( globalProps.containsKey( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_SERVICE_NOTIFICATION_COMMANDS_PROPERTY_KEY_SUFFIX ) ) {
			contactBuilder.setServiceNotificationCommands( getSetProperty( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_SERVICE_NOTIFICATION_COMMANDS_PROPERTY_KEY_SUFFIX ) );
		}

		if ( globalProps.containsKey( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_SERVICE_NOTIFICATION_PERIOD_PROPERTY_KEY_SUFFIX ) ) {
			contactBuilder.setServiceNotificationPeriod( getStringProperty( CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_SERVICE_NOTIFICATION_PERIOD_PROPERTY_KEY_SUFFIX ) );
		}

		return contactBuilder.build();

	}


	private static Set<HostNotificationOption> getContactHostNotificationOptions( String contactName ) throws CandlestackPropertiesException {
		String propertyKey = CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_HOST_NOTIFICATION_OPTIONS_PROPERTY_KEY_SUFFIX;

		String optionsStr = getStringProperty( propertyKey, null ).trim();
		if ( optionsStr.isEmpty() ) {
			throw new CandlestackPropertiesException( "GlobalNagiosProperties was unable to locate non-empty string property for property key [" + propertyKey + "]" );
		}

		Set<HostNotificationOption> options = new HashSet<>();
		for ( String optionStr : optionsStr.split( "," ) ) {
			HostNotificationOption option = HostNotificationOption.valueOf( optionStr.toLowerCase() );
			if ( option == null ) {
				throw new CandlestackPropertiesException( "GlobalNagiosProperties found invalid value of [" + optionStr + "] for property key [" + propertyKey + "]" );
			}
			options.add( option );
		}

		return options;
	}


	private static Set<ServiceNotificationOption> getContactServiceNotificationOptions( String contactName ) throws CandlestackPropertiesException {
		String propertyKey = CONTACT_PROPERTY_KEY_PREFIX + contactName + CONTACT_SERVICE_NOTIFICATION_OPTIONS_PROPERTY_KEY_SUFFIX;
		String optionsStr = getStringProperty( propertyKey, null ).trim();
		if ( optionsStr.isEmpty() ) {
			throw new CandlestackPropertiesException( "GlobalNagiosProperties was unable to locate non-empty string property for property key [" + propertyKey + "]" );
		}

		Set<ServiceNotificationOption> options = new HashSet<>();
		for ( String optionStr : optionsStr.split( "," ) ) {
			ServiceNotificationOption option = ServiceNotificationOption.valueOf( optionStr.toLowerCase() );
			if ( option == null ) {
				throw new CandlestackPropertiesException( "GlobalNagiosProperties found invalid value of [" + optionStr + "] for property key [" + propertyKey + "]" );
			}
			options.add( option );
		}

		return options;
	}


	/*
	 * ---------------------------------------
	 * Properties related to ContactGroups
	 * ---------------------------------------
	 */

	private static final String CONTACTGROUP_PROPERTY_KEY_PREFIX = "nagios.contactgroup.";

	private static final String CONTACTGROUP_ALIAS_PROPERTY_KEY_SUFFIX = ".alias",
			CONTACTGROUP_MEMBERS_PROPERTY_KEY_SUFFIX = ".members";


	public static ContactGroup getContactGroupByName( String contactGroupName ) throws CandlestackPropertiesException {
		return new ContactGroup( contactGroupName, getStringProperty( CONTACTGROUP_PROPERTY_KEY_PREFIX + contactGroupName + CONTACTGROUP_ALIAS_PROPERTY_KEY_SUFFIX ), getSetProperty( CONTACTGROUP_PROPERTY_KEY_PREFIX + contactGroupName + CONTACTGROUP_MEMBERS_PROPERTY_KEY_SUFFIX ) );
	}


	public static List<ContactGroup> getAllContactGroups() throws CandlestackPropertiesException {
		List<ContactGroup> contactGroups = new ArrayList<>();

		// First we have to find what contact group names have been defined by searching through the properties
		Set<String> contactGroupNames = getNames( CONTACTGROUP_PROPERTY_KEY_PREFIX );

		// Now that we have the set of names we can create the contact groups
		for ( String contactGroupName : contactGroupNames ) {
			contactGroups.add( getContactGroupByName( contactGroupName ) );
		}

		return contactGroups;
	}


	/*
	 * ----------------------------------
	 * Properties related to Commands
	 * ----------------------------------
	 */
	private static final String COMMANDS_PROPERTY_KEY_PREFIX = "nagios.command.";


	public static List<Command> getAllCommands() throws CandlestackPropertiesException {
		List<Command> commands = new ArrayList<>();

		Set<String> commandNames = getNames( COMMANDS_PROPERTY_KEY_PREFIX );
		for ( String commandName : commandNames ) {
			commands.add( getCommandByName( commandName ) );
		}

		return commands;
	}


	public static Command getCommandByName( String commandName ) throws CandlestackPropertiesException {
		return new Command( commandName, getStringProperty( COMMANDS_PROPERTY_KEY_PREFIX + commandName ).replace( "\n", "\\n" ) );
	}

}
