package io.codearcs.candlestack.aws.cloudwatch;

import com.amazonaws.services.cloudwatch.model.Dimension;

import io.codearcs.candlestack.aws.AWSMetric;
import io.codearcs.candlestack.aws.CloudWatchStatistic;


public interface CloudWatchMetric extends AWSMetric {

	public CloudWatchStatistic getStatistic();


	public String getNamespace();


	public Dimension getDimension( String dimensionValue );


	public String getName();

}
