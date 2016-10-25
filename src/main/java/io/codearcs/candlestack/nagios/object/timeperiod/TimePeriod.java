package io.codearcs.candlestack.nagios.object.timeperiod;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.codearcs.candlestack.nagios.object.NagiosObject;


/**
 * Representation of a Nagios TimePeriod object definition,
 * {@link https://assets.nagios.com/downloads/nagioscore/docs/nagioscore/4/en/objectdefinitions.html#timeperiod}
 */
public class TimePeriod implements NagiosObject {

	private final String name, alias;

	private final List<Weekday> weekdays;


	private TimePeriod( TimePeriodBuilder builder ) {
		name = builder.name;
		alias = builder.alias;
		weekdays = builder.weekdays;
	}


	public String getName() {
		return name;
	}


	public String getAlias() {
		return alias;
	}


	public List<Weekday> getWeekdays() {
		return weekdays;
	}


	@Override
	public String getObjectDefinitions() {
		StringBuilder sb = new StringBuilder( "define timeperiod{\n" );

		sb.append( "\ttimeperiod_name\t" );
		sb.append( name );
		sb.append( "\n" );

		sb.append( "\talias\t" );
		sb.append( alias );
		sb.append( "\n" );

		for ( Weekday weekday : weekdays ) {
			sb.append( "\t" );
			sb.append( weekday.getDay().name().toLowerCase() );
			sb.append( "\t" );

			sb.append( weekday.getTimeRanges().stream().map( timeRange -> timeRange.toString() ).collect( Collectors.joining( "," ) ) );
			sb.append( "\n" );
		}

		sb.append( "}\n\n" );

		return sb.toString();
	}


	public static final List<TimePeriod> getAllDefaultTimePeriods() {
		return Arrays.asList( TwentyFourSeven() );
	}


	public static final String getTwentyFourSevenName() {
		return "24x7";
	}


	public static final TimePeriod TwentyFourSeven() {
		TimeRange allDay = new TimeRange( new Time( 0, 0 ), new Time( 24, 0 ) );
		return new TimePeriodBuilder( "24x7", "24 hours a day, 7 days a week" )
				.addWeekday( new Weekday( DayOfWeek.SUNDAY, allDay ) )
				.addWeekday( new Weekday( DayOfWeek.MONDAY, allDay ) )
				.addWeekday( new Weekday( DayOfWeek.TUESDAY, allDay ) )
				.addWeekday( new Weekday( DayOfWeek.WEDNESDAY, allDay ) )
				.addWeekday( new Weekday( DayOfWeek.THURSDAY, allDay ) )
				.addWeekday( new Weekday( DayOfWeek.FRIDAY, allDay ) )
				.addWeekday( new Weekday( DayOfWeek.SATURDAY, allDay ) )
				.build();
	}


	public static class TimePeriodBuilder {

		private final String name, alias;

		private List<Weekday> weekdays;


		public TimePeriodBuilder( String name, String alias ) {
			this.name = name;
			this.alias = alias;
			weekdays = new ArrayList<>();
		}


		public TimePeriodBuilder addWeekday( Weekday weekday ) {
			weekdays.add( weekday );
			return this;
		}


		public TimePeriod build() {
			return new TimePeriod( this );
		}
	}

}
