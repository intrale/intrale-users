package ar.com.intrale.functions;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordResult;

import ar.com.intrale.BaseFunction;
import ar.com.intrale.FunctionConst;
import ar.com.intrale.FunctionResponseToBase64HttpResponseBuilder;
import ar.com.intrale.exceptions.FunctionException;
import ar.com.intrale.messages.ConfirmPasswordRecoveryRequest;
import ar.com.intrale.messages.ConfirmPasswordRecoveryResponse;
import ar.com.intrale.messages.builders.StringToConfirmPasswordRecoveryRequestBuilder;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(ConfirmPasswordRecoveryFunction.FUNCTION_NAME)
@Requires(property = FunctionConst.APP_INSTANTIATE + ConfirmPasswordRecoveryFunction.FUNCTION_NAME , value = FunctionConst.TRUE, defaultValue = FunctionConst.TRUE)
public class ConfirmPasswordRecoveryFunction extends 
	BaseFunction<ConfirmPasswordRecoveryRequest, ConfirmPasswordRecoveryResponse, 
	AWSCognitoIdentityProvider, StringToConfirmPasswordRecoveryRequestBuilder, FunctionResponseToBase64HttpResponseBuilder> {

	
	public static final String FUNCTION_NAME = "confirmPasswordRecovery";

	@Override
	public ConfirmPasswordRecoveryResponse execute(ConfirmPasswordRecoveryRequest request) throws FunctionException {
		ConfirmPasswordRecoveryResponse response = new ConfirmPasswordRecoveryResponse(); 

		ConfirmForgotPasswordRequest confirmForgotPasswordRequest = new ConfirmForgotPasswordRequest();
		confirmForgotPasswordRequest.setClientId(config.getCognito().getClientId());
		confirmForgotPasswordRequest.setUsername(request.getEmail());
		confirmForgotPasswordRequest.setConfirmationCode(request.getCode());
		confirmForgotPasswordRequest.setPassword(request.getPassword());

		ConfirmForgotPasswordResult result = provider.confirmForgotPassword(confirmForgotPasswordRequest);

		response.setEmail(request.getEmail());
		
		return response;
	}


}
