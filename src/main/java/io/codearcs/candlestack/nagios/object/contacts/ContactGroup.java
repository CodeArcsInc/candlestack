package io.codearcs.candlestack.nagios.object.contacts;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.codearcs.candlestack.nagios.object.NagiosObject;


/**
 * Representation of a Nagios ContactGroup object definition,
 * {@link https://assets.nagios.com/downloads/nagioscore/docs/nagioscore/4/en/objectdefinitions.html#contactgroup}
 */
public class ContactGroup implements NagiosObject {

	private String name, alias;

	private Set<String> members;


	public ContactGroup( String name, String alias ) {
		this( name, alias, new HashSet<>() );
	}


	public ContactGroup( String name, String alias, Set<String> members ) {
		this.name = name;
		this.alias = alias;
		this.members = members;
	}


	public String getName() {
		return name;
	}


	public String getAlias() {
		return alias;
	}


	public void addMember( String contactName ) {
		members.add( contactName );
	}


	public Set<String> getMembers() {
		return members;
	}


	@Override
	public String getObjectDefinitions() {

		StringBuilder sb = new StringBuilder( "define contactgroup{\n" );

		sb.append( "\tcontactgroup_name\t" );
		sb.append( name );
		sb.append( "\n" );

		sb.append( "\talias\t" );
		sb.append( alias );
		sb.append( "\n" );

		sb.append( "\tmembers\t" );
		sb.append( members.stream().collect( Collectors.joining( "," ) ) );
		sb.append( "\n" );

		sb.append( "}\n\n" );

		return sb.toString();

	}

}
