package io.codearcs.candlestack.aws.ec2;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.nagios.HostMonitorLookup;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.hosts.HostGroup;


public class EC2HostMonitorLookup implements HostMonitorLookup {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<HostGroup> lookupHostsToMonitor() throws CandlestackException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, InputStream> getMonitorResources() throws CandlestackException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<Command> getMonitorCommands( String relativePathToMonitorResource ) throws CandlestackException {
		// TODO Auto-generated method stub
		return null;
	}

}
