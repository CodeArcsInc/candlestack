package io.codearcs.candlestack.nagios.object.hosts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.codearcs.candlestack.nagios.object.NagiosObject;


/**
 * Representation of a Nagios HostGroup object definition,
 * {@link https://assets.nagios.com/downloads/nagioscore/docs/nagioscore/4/en/objectdefinitions.html#hostgroup}
 */
public class HostGroup implements NagiosObject {

	private final String name, alias;

	private List<Host> hosts;


	public HostGroup( String name, String alias ) {
		this.name = name;

		if ( alias == null ) {
			this.alias = "";
		} else {
			this.alias = alias;
		}

		hosts = new ArrayList<>();
	}


	public String getName() {
		return name;
	}


	public String getAlias() {
		return alias;
	}


	public void addHost( Host host ) {
		hosts.add( host );
	}


	public List<Host> getHosts() {
		return hosts;
	}


	@Override
	public String getObjectDefinitions() {
		StringBuilder sb = new StringBuilder( "define hostgroup{\n" );

		sb.append( "\thostgroup_name\t" );
		sb.append( name );
		sb.append( "\n" );

		sb.append( "\talias\t" );
		sb.append( alias );
		sb.append( "\n" );


		sb.append( "\tmembers\t" );
		sb.append( hosts.stream().map( host -> host.getName() ).collect( Collectors.joining( "," ) ) );
		sb.append( "\n" );

		sb.append( "}\n\n" );

		for ( Host host : hosts ) {
			sb.append( host.getObjectDefinitions() );
		}

		return sb.toString();
	}


	public static boolean areEquivalent( List<HostGroup> hostGroups1, List<HostGroup> hostGroups2 ) {

		boolean equivalent = true;

		if ( hostGroups1.size() != hostGroups2.size() ) {
			equivalent = false;
		} else {

			for ( HostGroup hostGroup1 : hostGroups1 ) {

				boolean foundEquivalentHostGroup = false;
				for ( HostGroup hostGroup2 : hostGroups2 ) {
					if ( areEquivalent( hostGroup1, hostGroup2 ) ) {
						foundEquivalentHostGroup = true;
						break;
					}
				}

				if ( !foundEquivalentHostGroup ) {
					equivalent = false;
					break;
				}

			}

		}

		return equivalent;

	}


	public static boolean areEquivalent( HostGroup hostGroup1, HostGroup hostGroup2 ) {

		boolean equivalent = true;
		if ( !hostGroup1.getName().equals( hostGroup2.getName() ) ) {
			equivalent = false;
		} else if ( !hostGroup1.getAlias().equals( hostGroup2.getAlias() ) ) {
			equivalent = false;
		} else if ( !Host.areEquivalent( hostGroup1.getHosts(), hostGroup2.getHosts() ) ) {
			equivalent = false;
		}

		return equivalent;
	}


}
