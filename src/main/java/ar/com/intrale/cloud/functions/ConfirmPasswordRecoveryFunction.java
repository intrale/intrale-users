package ar.com.intrale.cloud.functions;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordResult;

import ar.com.intrale.cloud.Function;
import ar.com.intrale.cloud.FunctionException;
import ar.com.intrale.cloud.messages.ConfirmPasswordRecoveryRequest;
import ar.com.intrale.cloud.messages.ConfirmPasswordRecoveryResponse;

@Singleton
@Named(ConfirmPasswordRecoveryFunction.FUNCTION_NAME)
public class ConfirmPasswordRecoveryFunction extends Function<ConfirmPasswordRecoveryRequest, ConfirmPasswordRecoveryResponse, AWSCognitoIdentityProvider> {

	public static final String FUNCTION_NAME = "confirmPasswordRecovery";

	@Override
	public ConfirmPasswordRecoveryResponse execute(ConfirmPasswordRecoveryRequest request) throws FunctionException {
		ConfirmPasswordRecoveryResponse response = new ConfirmPasswordRecoveryResponse(); 

		ConfirmForgotPasswordRequest confirmForgotPasswordRequest = new ConfirmForgotPasswordRequest();
		confirmForgotPasswordRequest.setClientId(config.getAws().getClientId());
		confirmForgotPasswordRequest.setUsername(request.getEmail());
		confirmForgotPasswordRequest.setConfirmationCode(request.getCode());
		confirmForgotPasswordRequest.setPassword(request.getPassword());

		ConfirmForgotPasswordResult result = provider.confirmForgotPassword(confirmForgotPasswordRequest);

		response.setUsername(request.getEmail());
		
		return response;
	}


}
