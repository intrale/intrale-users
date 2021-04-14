package ar.com.intrale.cloud.functions;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordResult;

import ar.com.intrale.cloud.Function;
import ar.com.intrale.cloud.FunctionException;
import ar.com.intrale.cloud.messages.PasswordRecoveryRequest;
import ar.com.intrale.cloud.messages.PasswordRecoveryResponse;

@Singleton
@Named(PasswordRecoveryFunction.FUNCTION_NAME)
public class PasswordRecoveryFunction extends Function<PasswordRecoveryRequest, PasswordRecoveryResponse, AWSCognitoIdentityProvider> {

	public static final String FUNCTION_NAME = "passwordRecovery";

	@Override
	public PasswordRecoveryResponse execute(PasswordRecoveryRequest request) throws FunctionException {
		PasswordRecoveryResponse response = new PasswordRecoveryResponse(); 

		ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
		forgotPasswordRequest.setClientId(config.getAws().getClientId());
		forgotPasswordRequest.setUsername(request.getEmail());

		ForgotPasswordResult result = provider.forgotPassword(forgotPasswordRequest);

		response.setUsername(request.getEmail());
		
		return response;
	}


}
