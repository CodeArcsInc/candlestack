package io.codearcs.candlestack.aws.rds;


public enum RDSType {

	UNSUPPORTED,
	AURORA,
	AURORA_MYSQL,
	MARIADB;


	public static RDSType getTypeFromEngine( String rdsEngine ) {
		RDSType result = RDSType.UNSUPPORTED;

		if ( rdsEngine != null ) {
			String cleanRDSEngine = rdsEngine.trim().replace( "-", "_" );
			for ( RDSType type : values() ) {
				if ( type.name().equalsIgnoreCase( cleanRDSEngine ) ) {
					result = type;
					break;
				}
			}
		}

		return result;
	}
}
