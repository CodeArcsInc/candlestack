package io.codearcs.candlestack.aws.es;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.elasticsearch.AWSElasticsearch;
import com.amazonaws.services.elasticsearch.AWSElasticsearchClientBuilder;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.aws.GlobalAWSProperties;
import io.codearcs.candlestack.nagios.HostMonitorLookup;
import io.codearcs.candlestack.nagios.object.commands.Command;
import io.codearcs.candlestack.nagios.object.hosts.HostGroup;


public class ESHostMonitorLookup implements HostMonitorLookup {

	private AWSElasticsearch esClient;


	public ESHostMonitorLookup() throws CandlestackPropertiesException {

		esClient = AWSElasticsearchClientBuilder.standard().withRegion( GlobalAWSProperties.getRegion() ).build();

	}


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
