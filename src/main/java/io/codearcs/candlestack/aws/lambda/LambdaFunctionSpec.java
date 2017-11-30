package io.codearcs.candlestack.aws.lambda;

/**
 * Encapsulates the Specification For a Lambda Function.
 * 
 * @author Amanda
 *
 */
public class LambdaFunctionSpec {

	private String arn;
	private String name;
	private String resource;

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArn() {
		return arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}
	
}
