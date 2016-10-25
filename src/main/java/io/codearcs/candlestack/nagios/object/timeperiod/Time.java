package io.codearcs.candlestack.nagios.object.timeperiod;


public class Time {

	public int hour, minute;


	public Time( int hour, int minute ) {
		this.hour = hour;
		this.minute = minute;
	}


	public int getHour() {
		return hour;
	}


	public int getMinute() {
		return minute;
	}

}
