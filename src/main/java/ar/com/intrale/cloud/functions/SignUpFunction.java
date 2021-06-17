package ar.com.intrale.cloud.functions;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;

import ar.com.intrale.cloud.CredentialsGenerator;
import ar.com.intrale.cloud.Error;
import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.TemporaryPasswordConfig;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.exceptions.UserExistsException;
import ar.com.intrale.cloud.messages.SignUpRequest;
import ar.com.intrale.cloud.messages.SignUpResponse;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(SignUpFunction.FUNCTION_NAME)
@Requires(property = IntraleFunction.APP_INSTANTIATE + SignUpFunction.FUNCTION_NAME , value = IntraleFunction.TRUE, defaultValue = IntraleFunction.TRUE)
public class SignUpFunction extends IntraleFunction<SignUpRequest, SignUpResponse, AWSCognitoIdentityProvider> {

	public static final String FUNCTION_NAME = "signup";
	
	public static final String EMAIL = "email";
	public static final String FIELD_USERNAME_ALREADY_EXIST = "field_username_already_exist";

	@Inject
	private TemporaryPasswordConfig temporaryPasswordConfig;
	
	@Inject
	private CredentialsGenerator credentialGenerator;
	
	/*@Inject
	private LinkFunction linkFunction;*/
	
	@Override
	public SignUpResponse execute(SignUpRequest request) throws FunctionException {
		SignUpResponse response = new SignUpResponse(); 

		String temporaryPassword = credentialGenerator.generate(temporaryPasswordConfig.length);
		String businessName = request.getHeaders().get(Lambda.HEADER_BUSINESS_NAME);
		
		AdminCreateUserRequest adminCreateUserRequest = new AdminCreateUserRequest()
				.withUserPoolId(config.getCognito().getUserPoolId())
				.withUsername(request.getEmail())
				.withUserAttributes(new AttributeType().withName(EMAIL).withValue(request.getEmail()))
				.withUserAttributes(new AttributeType().withName(BUSINESS_ATTRIBUTE).withValue(businessName) )
				.withTemporaryPassword(temporaryPassword);
		
		try {
			AdminCreateUserResult createUserResult = provider.adminCreateUser(adminCreateUserRequest);
		} catch (UsernameExistsException e) {
			AdminGetUserRequest adminGetUserRequest = new AdminGetUserRequest();
			adminGetUserRequest.setUserPoolId(config.getCognito().getUserPoolId());
			adminGetUserRequest.setUsername(request.getEmail());
			AdminGetUserResult adminGetUserResult =  provider.adminGetUser(adminGetUserRequest);
			
			Map<String, String> attributes = adminGetUserResult.getUserAttributes().stream()
				      .collect(Collectors.toMap(AttributeType::getName, AttributeType::getValue));
			String businessAttributeValue = attributes.get(BUSINESS_ATTRIBUTE);
			if (businessAttributeValue.contains(businessName)) {
				throw new UserExistsException(new Error(FIELD_USERNAME_ALREADY_EXIST, "Field username already exists"), mapper);
			}
			
		     AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = new AdminUpdateUserAttributesRequest()
				        .withUserPoolId(config.getCognito().getUserPoolId())
						.withUsername(request.getEmail());
				      
		      adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(BUSINESS_ATTRIBUTE).withValue(businessAttributeValue + BUSINESS_ATTRIBUTE_SEPARATOR + businessName));
		     
		     provider.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
			
			
			
			
		} /*finally {
			try {
				linkEmailWithBusiness(request);
			} catch (UserExistsException error) {
				throw new BadRequestException(new Error(FIELD_USERNAME_ALREADY_EXIST, "Field username already exists"), mapper);
			}
		}*/
		
	    if (temporaryPasswordConfig.returned) {
	    	response.setTemporaryPassword(temporaryPassword);
	    }
	    
	    response.setEmail(request.getEmail());	
	    response.setBusinessName(request.getHeaders().get(Lambda.HEADER_BUSINESS_NAME));
		return response;
	}

	/*private void linkEmailWithBusiness(SignUpRequest request) throws FunctionException {
		LinkRequest linkRequest = new LinkRequest();
		linkRequest.setRequestId(request.getRequestId());
		linkRequest.setEmail(request.getEmail());
		linkRequest.setHeaders(request.getHeaders());
		
		LinkResponse linkResponse = linkFunction.execute(linkRequest);
	}*/


}