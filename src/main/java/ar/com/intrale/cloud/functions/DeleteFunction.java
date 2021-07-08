package ar.com.intrale.cloud.functions;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;

import ar.com.intrale.cloud.BaseFunction;
import ar.com.intrale.cloud.FunctionConst;
import ar.com.intrale.cloud.FunctionResponseToHttpResponseBuilder;
import ar.com.intrale.cloud.Response;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.messages.DeleteRequest;
import ar.com.intrale.cloud.messages.builders.StringToDeleteRequestBuilder;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(DeleteFunction.FUNCTION_NAME)
@Requires(property = FunctionConst.APP_INSTANTIATE + DeleteFunction.FUNCTION_NAME , value = FunctionConst.TRUE, defaultValue = FunctionConst.TRUE)
public class DeleteFunction extends 
	BaseFunction<DeleteRequest, Response, AWSCognitoIdentityProvider, StringToDeleteRequestBuilder, FunctionResponseToHttpResponseBuilder> {

	public static final String FUNCTION_NAME = "delete";
	
	@Override
	public Response execute(DeleteRequest request) throws FunctionException {
		try {
			AdminDeleteUserRequest adminDeleteUserRequest = new AdminDeleteUserRequest();
			adminDeleteUserRequest.setUserPoolId(config.getCognito().getUserPoolId());
			adminDeleteUserRequest.setUsername(request.getEmail());
			provider.adminDeleteUser(adminDeleteUserRequest);
		} catch (UserNotFoundException e) {
			// do nothing
		}
		return new Response();
	}


}
