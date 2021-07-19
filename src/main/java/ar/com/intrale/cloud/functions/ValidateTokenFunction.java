package ar.com.intrale.cloud.functions;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;

import ar.com.intrale.cloud.BaseFunction;
import ar.com.intrale.cloud.FunctionConst;
import ar.com.intrale.cloud.FunctionResponseToBase64HttpResponseBuilder;
import ar.com.intrale.cloud.Request;
import ar.com.intrale.cloud.Response;
import ar.com.intrale.cloud.StringToRequestDefaultBuilder;
import ar.com.intrale.cloud.exceptions.FunctionException;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(ValidateTokenFunction.FUNCTION_NAME)
@Requires(property = FunctionConst.APP_INSTANTIATE + ValidateTokenFunction.FUNCTION_NAME , value = FunctionConst.TRUE, defaultValue = FunctionConst.TRUE)
public class ValidateTokenFunction extends 
	BaseFunction<Request, Response, AWSCognitoIdentityProvider, StringToRequestDefaultBuilder, FunctionResponseToBase64HttpResponseBuilder> {
	
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
