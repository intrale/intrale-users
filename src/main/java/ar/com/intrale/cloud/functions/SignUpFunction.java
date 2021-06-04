package ar.com.intrale.cloud.functions;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;

import ar.com.intrale.cloud.CredentialsGenerator;
import ar.com.intrale.cloud.Error;
import ar.com.intrale.cloud.Function;
import ar.com.intrale.cloud.TemporaryPasswordConfig;
import ar.com.intrale.cloud.exceptions.BadRequestException;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.exceptions.UserExistsException;
import ar.com.intrale.cloud.messages.LinkRequest;
import ar.com.intrale.cloud.messages.LinkResponse;
import ar.com.intrale.cloud.messages.SignUpRequest;
import ar.com.intrale.cloud.messages.SignUpResponse;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(SignUpFunction.FUNCTION_NAME)
@Requires(property = Function.APP_INSTANTIATE + SignUpFunction.FUNCTION_NAME , value = Function.TRUE, defaultValue = Function.TRUE)
public class SignUpFunction extends Function<SignUpRequest, SignUpResponse, AWSCognitoIdentityProvider> {

	public static final String FUNCTION_NAME = "signup";
	
	public static final String TABLE_NAME 		= "businessRelations";

	public static final String EMAIL = "email";
	public static final String FIELD_USERNAME_ALREADY_EXIST = "field_username_already_exist";

	@Inject
	private TemporaryPasswordConfig temporaryPasswordConfig;
	
	@Inject
	private CredentialsGenerator credentialGenerator;
	
	@Inject
	private LinkFunction linkFunction;
	
	@Override
	public SignUpResponse execute(SignUpRequest request) throws FunctionException {
		SignUpResponse response = new SignUpResponse(); 

		String temporaryPassword = credentialGenerator.generate(temporaryPasswordConfig.length);
		
		AdminCreateUserRequest cognitoRequest = new AdminCreateUserRequest()
				.withUserPoolId(config.getCognito().getUserPoolId())
				.withUsername(request.getEmail())
				.withUserAttributes(new AttributeType().withName(EMAIL).withValue(request.getEmail()))
				.withTemporaryPassword(temporaryPassword);
		
		try {
			AdminCreateUserResult createUserResult = provider.adminCreateUser(cognitoRequest);
		    
		    linkEmailWithBusiness(request);

		    if (temporaryPasswordConfig.returned) {
		    	response.setTemporaryPassword(temporaryPassword);
		    }
		} catch (UsernameExistsException e) {
			try {
				linkEmailWithBusiness(request);
			} catch (UserExistsException error) {
				throw new BadRequestException(new Error(FIELD_USERNAME_ALREADY_EXIST, "Field username already exists"), mapper);
			}
		}
	    response.setEmail(request.getEmail());	
	    response.setBusinessName(request.getBusinessName());
		return response;
	}

	private void linkEmailWithBusiness(SignUpRequest request) throws FunctionException {
		LinkRequest linkRequest = new LinkRequest();
		linkRequest.setRequestId(request.getRequestId());
		linkRequest.setBusinessName(request.getBusinessName());
		linkRequest.setEmail(request.getEmail());
		
		LinkResponse linkResponse = linkFunction.execute(linkRequest);
	}


}