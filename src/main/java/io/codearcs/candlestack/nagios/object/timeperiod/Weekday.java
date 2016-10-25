package io.codearcs.candlestack.nagios.object.timeperiod;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;


public class Weekday {

	private final DayOfWeek day;

	private final List<TimeRange> timeRanges;


	public Weekday( DayOfWeek day, TimeRange... timeRanges ) {
		this.day = day;
		this.timeRanges = Arrays.asList( timeRanges );
	}


	public DayOfWeek getDay() {
		return day;
	}


	public List<TimeRange> getTimeRanges() {
		return timeRanges;
	}

}
