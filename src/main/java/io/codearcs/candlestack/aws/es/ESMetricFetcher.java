package io.codearcs.candlestack.aws.es;

import io.codearcs.candlestack.CandlestackException;
import io.codearcs.candlestack.MetricsFetcher;


public class ESMetricFetcher extends MetricsFetcher {


	public ESMetricFetcher( String name, int sleepIntervalMinutes ) throws CandlestackException {
		super( name, sleepIntervalMinutes );
		// TODO Auto-generated constructor stub
	}


	@Override
	public void fetchMetrics() {
		// TODO Auto-generated method stub

	}


	@Override
	public void close() {
		// TODO Auto-generated method stub

	}


	public void performChecks() {

		// for ( DomainInfo domainInfo : esClient.listDomainNames( new ListDomainNamesRequest() ).getDomainNames() ) {
		//
		// ElasticsearchDomainStatus status = esClient.describeElasticsearchDomain( new DescribeElasticsearchDomainRequest().withDomainName( domainInfo.getDomainName() ) ).getDomainStatus();
		// System.out.println( domainInfo.getDomainName() );
		// System.out.println( "\tversion : " + status.getElasticsearchVersion() );
		// System.out.println( "\tversion : " + status.get );
		//
		// }

	}
}
