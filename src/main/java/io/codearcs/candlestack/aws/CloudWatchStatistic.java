package io.codearcs.candlestack.aws;

import com.amazonaws.services.cloudwatch.model.Datapoint;


public enum CloudWatchStatistic {

	Minimum,
	Maximum,
	Sum,
	Average,
	SampleCount;


	public double getValueFromDatapoint( Datapoint datapoint ) throws CandlestackAWSException {
		switch ( this ) {
			case Minimum :
				return datapoint.getMinimum();
			case Maximum :
				return datapoint.getMaximum();
			case Sum :
				return datapoint.getSum();
			case Average :
				return datapoint.getAverage();
			case SampleCount :
				return datapoint.getSampleCount();
			default :
				throw new CandlestackAWSException( "Unsupported CloudWatchStatistic [" + name() + "]" );
		}
	}
}
