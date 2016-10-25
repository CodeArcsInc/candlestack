package io.codearcs.candlestack.nagios.object.commands;

import io.codearcs.candlestack.nagios.object.NagiosObject;


/**
 * Representation of a Nagios Command object definition,
 * {@link https://assets.nagios.com/downloads/nagioscore/docs/nagioscore/4/en/objectdefinitions.html#command}
 */
public class Command implements NagiosObject {

	private String name, line;


	public Command( String name, String line ) {
		this.name = name;
		this.line = line;
	}


	public String getName() {
		return name;
	}


	public String getLine() {
		return line;
	}


	@Override
	public String getObjectDefinitions() {

		StringBuilder sb = new StringBuilder( "define command{\n" );

		sb.append( "\tcommand_name\t" );
		sb.append( name );
		sb.append( "\n" );

		sb.append( "\tcommand_line\t" );
		sb.append( line );
		sb.append( "\n" );

		sb.append( "}\n\n" );

		return sb.toString();

	}

}

