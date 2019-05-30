package io.codearcs.candlestack.aws.lambda;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.AliasConfiguration;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListAliasesRequest;
import com.amazonaws.services.lambda.model.ListAliasesResult;
import com.amazonaws.services.lambda.model.ListFunctionsResult;

import io.codearcs.candlestack.CandlestackPropertiesException;
import io.codearcs.candlestack.aws.GlobalAWSProperties;

/**
 * Helper class for managing the Lambda Metrics.
 * 
 * @author Amanda
 *
 */
public class LambdaUtil {

	public static final String TYPE_NAME = "aws_lambda";
	
	public static boolean isFunctionElligible( String arn, String arnRegex ) {
		
		Boolean eligible = true;
		
		if ( !arnRegex.isEmpty() && !arn.matches( arnRegex ) ) {
			eligible = false;
		}
		
		return eligible;
	}
	
	/**
	 * Retrieves the list of Lambda Functions 
	 * 
	 * @param lambdaClient
	 * @return
	 */
	public static List<LambdaFunctionSpec> lookupFunctions( AWSLambda lambdaClient ) throws CandlestackPropertiesException {
		
		List<LambdaFunctionSpec> elligibleFunctions = new ArrayList<LambdaFunctionSpec>();
		
		// Create a request to list the functions
		ListFunctionsResult functionResult = lambdaClient.listFunctions();

		for ( FunctionConfiguration function: functionResult.getFunctions() ) {

			// Create a request to list the alias
			ListAliasesRequest aliasrequest = new ListAliasesRequest();
			aliasrequest.withFunctionName( function.getFunctionName() );
			ListAliasesResult aliasresult = lambdaClient.listAliases( aliasrequest );
			
			for ( AliasConfiguration alias : aliasresult.getAliases() ) {
				if ( isFunctionElligible( alias.getAliasArn(), GlobalAWSProperties.getLambdaFunctionRegex() ) ) {
					LambdaFunctionSpec functionSpec = new LambdaFunctionSpec();
					functionSpec.setArn( alias.getAliasArn() );
					functionSpec.setName( function.getFunctionName() );
					functionSpec.setResource( getResourceFromArn( alias.getAliasArn() ) );
					elligibleFunctions.add( functionSpec );
				}
			}

		}
				
		// Return the list of lambda functions
		return elligibleFunctions;
	}
	
	/**
	 * Returns the Resource Dimension given the Arn
	 * 
	 * @param arn
	 * @return
	 */
	public static String getResourceFromArn( String arn ) {
		String[] qualifiedFunctionName = arn.split("function:");
		return qualifiedFunctionName[1];
	}
	
	/**
	 * Returns the Function Dimension given the Arn
	 * 
	 * @param arn
	 * @return
	 */
	public static String getFunctionNameFromArn( String arn ) {
		String[] qualifiedFunctionName = arn.split("function:");
		String[] functionName = qualifiedFunctionName[0].split( ":");
		return functionName[0];		
	}
	

}
