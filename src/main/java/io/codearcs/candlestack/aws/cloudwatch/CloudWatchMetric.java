package io.codearcs.candlestack.aws.cloudwatch;

import io.codearcs.candlestack.aws.AWSMetric;
import io.codearcs.candlestack.aws.CloudWatchStatistic;


public interface CloudWatchMetric extends AWSMetric {

	public CloudWatchStatistic getStatistic();


	public String getNamespace();

	public String getName();

}
