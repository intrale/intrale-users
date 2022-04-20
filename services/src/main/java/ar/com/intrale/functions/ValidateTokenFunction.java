package ar.com.intrale.functions;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;

import ar.com.intrale.BaseFunction;
import ar.com.intrale.FunctionConst;
import ar.com.intrale.FunctionResponseToBase64HttpResponseBuilder;
import ar.com.intrale.StringToRequestDefaultBuilder;
import ar.com.intrale.exceptions.FunctionException;
import ar.com.intrale.messages.RequestRoot;
import ar.com.intrale.messages.Response;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(ValidateTokenFunction.FUNCTION_NAME)
@Requires(property = FunctionConst.APP_INSTANTIATE + ValidateTokenFunction.FUNCTION_NAME , value = FunctionConst.TRUE, defaultValue = FunctionConst.TRUE)
public class ValidateTokenFunction extends 
	BaseFunction<RequestRoot, Response, AWSCognitoIdentityProvider, StringToRequestDefaultBuilder, FunctionResponseToBase64HttpResponseBuilder> {
	
	public static final String FUNCTION_NAME = "validateToken";

	@Override
	public Response execute(RequestRoot request) throws FunctionException {
		return new Response();
	}

	@Override
	protected boolean isSecurityEnabled() {
		return Boolean.TRUE;
	}
	
}
