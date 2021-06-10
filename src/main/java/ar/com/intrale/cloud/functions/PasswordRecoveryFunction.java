package ar.com.intrale.cloud.functions;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordResult;

import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.messages.PasswordRecoveryRequest;
import ar.com.intrale.cloud.messages.PasswordRecoveryResponse;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(PasswordRecoveryFunction.FUNCTION_NAME)
@Requires(property = IntraleFunction.APP_INSTANTIATE + PasswordRecoveryFunction.FUNCTION_NAME , value = IntraleFunction.TRUE, defaultValue = IntraleFunction.TRUE)
public class PasswordRecoveryFunction extends IntraleFunction<PasswordRecoveryRequest, PasswordRecoveryResponse, AWSCognitoIdentityProvider> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PasswordRecoveryFunction.class);
	
	public static final String FUNCTION_NAME = "passwordRecovery";

	@Override
	public PasswordRecoveryResponse execute(PasswordRecoveryRequest request) throws FunctionException {
		PasswordRecoveryResponse response = new PasswordRecoveryResponse(); 

		
		LOGGER.info("INTRALE: PRE ForgotPasswordRequest");
		LOGGER.info("INTRALE: CLIENT ID:" + config.getCognito().getClientId());
		LOGGER.info("INTRALE: EMAIL:" + request.getEmail());
		
		ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
		forgotPasswordRequest.setClientId(config.getCognito().getClientId());
		forgotPasswordRequest.setUsername(request.getEmail());

		ForgotPasswordResult result = provider.forgotPassword(forgotPasswordRequest);
		LOGGER.info("INTRALE: RESULT:" + result.getCodeDeliveryDetails().toString());

		response.setEmail(request.getEmail());
		
		return response;
	}


}
