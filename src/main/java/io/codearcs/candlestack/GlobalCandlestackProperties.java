package io.codearcs.candlestack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;


public class GlobalCandlestackProperties {

	protected static Properties globalProps;


	/**
	 * Initializes the properties for usage. Must provide
	 * a non-null Properties object otherwise GlobalNagioProperties
	 * will continue to be considered not initialized. If you call
	 * initialize multiple times only the first call with a non-null
	 * props parameter will be used (aka if you called it with Properties
	 * object A then subsequently Properties object B it will continue
	 * to use Properties object A).
	 *
	 * @param props
	 */
	public synchronized static void init( Properties props ) {
		if ( globalProps == null && props != null ) {
			globalProps = props;
		}
	}


	/**
	 * Looks up the int property associated to the provided key. The following rules are used to determine
	 * what is returned or if an error will be thrown.
	 *
	 * 1) If the key doesn't exist and the provided default value is NULL then an error will be thrown
	 * 2) Same as 1 but if the default value is not null then the default value will be returned
	 * 3) If when trying to convert the property value to an int a NumberFormatException is thrown and the default value is NULL then an error will be thrown
	 * 4) Same as 3 but if the default value is not null then the default value will be returned
	 * 5) The int value associated to the property key will be returned regardless of what default value is set to
	 *
	 * @param propertyKey
	 * @param defaultVal
	 * @return
	 * @throws CandlestackPropertiesException
	 */
	public static int getIntProperty( String propertyKey, Integer defaultVal ) throws CandlestackPropertiesException {
		initCheck();

		int val = -1;

		String valStr = globalProps.getProperty( propertyKey );
		if ( valStr == null && defaultVal == null ) {

			throw new CandlestackPropertiesException( "GlobalCandlestackProperties was unable to locate int property for property key [" + propertyKey + "]" );

		} else if ( valStr == null ) {
			val = defaultVal;

		} else {

			try {
				val = Integer.parseInt( valStr );
			} catch ( NumberFormatException e ) {

				if ( defaultVal == null ) {
					throw new CandlestackPropertiesException( "GlobalCandlestackProperties found non-int value [" + valStr + "] for property key [" + propertyKey + "]" );
				}
				val = defaultVal;
			}

		}

		return val;
	}


	/**
	 * Looks up the long property associated to the provided key. The following rules are used to determine
	 * what is returned or if an error will be thrown.
	 *
	 * 1) If the key doesn't exist and the provided default value is NULL then an error will be thrown
	 * 2) Same as 1 but if the default value is not null then the default value will be returned
	 * 3) If when trying to convert the property value to a long a NumberFormatException is thrown and the default value is NULL then an error will be thrown
	 * 4) Same as 3 but if the default value is not null then the default value will be returned
	 * 5) The int value associated to the property key will be returned regardless of what default value is set to
	 *
	 * @param propertyKey
	 * @param defaultVal
	 * @return
	 * @throws CandlestackPropertiesException
	 */
	public static long getLongProperty( String propertyKey, Long defaultVal ) throws CandlestackPropertiesException {
		initCheck();

		long val = -1;

		String valStr = globalProps.getProperty( propertyKey );
		if ( valStr == null && defaultVal == null ) {

			throw new CandlestackPropertiesException( "GlobalCandlestackProperties was unable to locate long property for property key [" + propertyKey + "]" );

		} else if ( valStr == null ) {
			val = defaultVal;

		} else {

			try {
				val = Long.parseLong( valStr );
			} catch ( NumberFormatException e ) {

				if ( defaultVal == null ) {
					throw new CandlestackPropertiesException( "GlobalCandlestackProperties found non-long value [" + valStr + "] for property key [" + propertyKey + "]" );
				}
				val = defaultVal;
			}

		}

		return val;
	}


	/**
	 * Looks up the boolean property associated to the provided key. The following rules are used to determine
	 * what is returned or if an error will be thrown.
	 *
	 * 1) If the key doesn't exist or the value associated to the key is not a valid boolean value (true/false, yes/no, on/off) and the provided default value is NULL then an error will be thrown
	 * 2) Same as 1 but if the default value is not null then the default value will be returned
	 * 3) Property key exists and is associated to a valid boolean value then it will be returned regardless of what default value is set to
	 *
	 * @param propertyKey
	 * @param defaultVal
	 * @return true or false depending on the rules listed above
	 * @throws CandlestackPropertiesException
	 *           if GlobalCandlestackProperties have not been initialized and if rule 1 applies
	 */
	public static boolean getBooleanProperty( String propertyKey, Boolean defaultVal ) throws CandlestackPropertiesException {
		initCheck();

		Boolean val = BooleanUtils.toBooleanObject( globalProps.getProperty( propertyKey ) );
		if ( val == null && defaultVal == null ) {
			throw new CandlestackPropertiesException( "GlobalCandlestackProperties was unable to locate valid boolean property for property key [" + propertyKey + "]" );
		} else if ( val == null ) {
			val = defaultVal;
		}

		return val.booleanValue();
	}


	/**
	 * Looks up the string property associated to the provided key. The following rules are used to determine
	 * what is returned or if an error will be thrown.
	 *
	 * 1) If the key doesn't exist and the provided default value is NULL then an error will be thrown
	 * 2) Same as 1 but if the default value is not null then the default value will be returned
	 * 3) The string value associated to the property key will be returned regardless of what default value is set to
	 *
	 * @param propertyKey
	 * @param defaultVal
	 * @return either the string associated the property key or the provided default value based off the rules listed above,
	 *         no trimming or modification will be done to the strings so it is up to the caller to validate empty strings
	 * @throws CandlestackPropertiesException
	 *           if GlobalCandlestackProperties have not been initialized and if rule 1 applies
	 */
	public static String getStringProperty( String propertyKey, String defaultVal ) throws CandlestackPropertiesException {
		initCheck();

		String val = globalProps.getProperty( propertyKey );
		if ( val == null && defaultVal == null ) {
			throw new CandlestackPropertiesException( "GlobalCandlestackProperties was unable to locate string property for property key [" + propertyKey + "]" );
		} else if ( val == null ) {
			val = defaultVal;
		}

		return val;
	}


	/**
	 * Similar to calling the other getStringProperty method with null as the default value but will
	 * also throw an error if the string property is a blank string, returned value will also be already
	 * trimmed.
	 *
	 * @param propertyKey
	 * @return
	 * @throws CandlestackPropertiesException
	 */
	public static String getStringProperty( String propertyKey ) throws CandlestackPropertiesException {
		String val = getStringProperty( propertyKey, null ).trim();
		if ( val.isEmpty() ) {
			throw new CandlestackPropertiesException( "GlobalCandlestackProperties was unable to locate non-empty string property for property key [" + propertyKey + "]" );
		}
		return val;
	}


	protected static void initCheck() throws CandlestackPropertiesException {
		if ( globalProps == null ) {
			throw new CandlestackPropertiesException( "GlobalCandlestackProperties has not been properly initialized" );
		}
	}


	protected static Set<String> getSetProperty( String propertyKey ) throws CandlestackPropertiesException {
		return getSetProperty( propertyKey, false );
	}


	protected static Set<String> getSetProperty( String propertyKey, boolean allowEmpty ) throws CandlestackPropertiesException {
		String optionsStr = getStringProperty( propertyKey, null ).trim();
		if ( !allowEmpty && optionsStr.isEmpty() ) {
			throw new CandlestackPropertiesException( "GlobalCandlestackProperties was unable to locate non-empty string property for property key [" + propertyKey + "]" );
		} else if ( optionsStr.isEmpty() ) {
			return new HashSet<>();
		} else {
			return new HashSet<>( Arrays.asList( optionsStr.split( "," ) ) );
		}
	}

}
