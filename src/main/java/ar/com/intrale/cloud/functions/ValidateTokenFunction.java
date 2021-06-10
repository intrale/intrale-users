package ar.com.intrale.cloud.functions;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;

import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.Request;
import ar.com.intrale.cloud.Response;
import ar.com.intrale.cloud.exceptions.FunctionException;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(ValidateTokenFunction.FUNCTION_NAME)
@Requires(property = IntraleFunction.APP_INSTANTIATE + ValidateTokenFunction.FUNCTION_NAME , value = IntraleFunction.TRUE, defaultValue = IntraleFunction.TRUE)
public class ValidateTokenFunction extends IntraleFunction<Request, Response, AWSCognitoIdentityProvider> {
	
	public static final String FUNCTION_NAME = "validateToken";

	@Override
	public Response execute(Request request) throws FunctionException {
		return new Response();
	}

	@Override
	protected boolean isSecurityEnabled() {
		return Boolean.TRUE;
	}
	
}
