package io.codearcs.candlestack.nagios.object.timeperiod;

public class TimeRange {

	private Time startTime, endTime;


	public TimeRange( Time startTime, Time endTime ) {
		this.startTime = startTime;
		this.endTime = endTime;
	}


	public Time getStartTime() {
		return startTime;
	}


	public Time getEndTime() {
		return endTime;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append( String.format( "%02d", startTime.getHour() ) );
		sb.append( ":" );
		sb.append( String.format( "%02d", startTime.getMinute() ) );

		sb.append( "-" );

		sb.append( String.format( "%02d", endTime.getHour() ) );
		sb.append( ":" );
		sb.append( String.format( "%02d", endTime.getMinute() ) );

		return sb.toString();
	}
}
