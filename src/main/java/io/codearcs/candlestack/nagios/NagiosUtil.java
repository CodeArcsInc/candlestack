package io.codearcs.candlestack.nagios;

import java.util.Set;


/**
 * Provides utility methods for the Nagios code
 */
public class NagiosUtil {

	/**
	 * Helper method for determining if two sets of Strings are equivalent to each other
	 *
	 * @param set1
	 * @param set2
	 * @return true if the two sets are logically equivalent, false otherwise
	 */
	public static boolean areEquivalent( Set<String> set1, Set<String> set2 ) {
		boolean equivalent = true;

		if ( set1.size() != set2.size() ) {
			equivalent = false;
		} else {

			for ( String string1 : set1 ) {
				if ( !set2.contains( string1 ) ) {
					equivalent = false;
					break;
				}
			}

		}

		return equivalent;
	}

}
