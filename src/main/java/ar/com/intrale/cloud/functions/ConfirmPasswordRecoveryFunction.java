package ar.com.intrale.cloud.functions;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordResult;

import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.messages.ConfirmPasswordRecoveryRequest;
import ar.com.intrale.cloud.messages.ConfirmPasswordRecoveryResponse;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(ConfirmPasswordRecoveryFunction.FUNCTION_NAME)
@Requires(property = IntraleFunction.APP_INSTANTIATE + ConfirmPasswordRecoveryFunction.FUNCTION_NAME , value = IntraleFunction.TRUE, defaultValue = IntraleFunction.TRUE)
public class ConfirmPasswordRecoveryFunction extends IntraleFunction<ConfirmPasswordRecoveryRequest, ConfirmPasswordRecoveryResponse, AWSCognitoIdentityProvider> {

	
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
