package ar.com.intrale.functions;

import java.util.List;
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

import ar.com.intrale.messages.Error;
import ar.com.intrale.BaseFunction;
import ar.com.intrale.CredentialsGenerator;
import ar.com.intrale.FunctionBuilder;
import ar.com.intrale.FunctionConst;
import ar.com.intrale.FunctionResponseToBase64HttpResponseBuilder;
import ar.com.intrale.TemporaryPasswordConfig;
import ar.com.intrale.exceptions.FunctionException;
import ar.com.intrale.exceptions.UserExistsException;
import ar.com.intrale.messages.SignUpRequest;
import ar.com.intrale.messages.SignUpResponse;
import ar.com.intrale.messages.builders.StringToSignUpRequestBuilder;
import com.nimbusds.jwt.JWTClaimsSet;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(SignUpFunction.FUNCTION_NAME)
@Requires(property = FunctionConst.APP_INSTANTIATE + SignUpFunction.FUNCTION_NAME , value = FunctionConst.TRUE, defaultValue = FunctionConst.TRUE)
public class SignUpFunction extends 
	BaseFunction<SignUpRequest, SignUpResponse, AWSCognitoIdentityProvider, StringToSignUpRequestBuilder, FunctionResponseToBase64HttpResponseBuilder> {

	public static final String FUNCTION_NAME = "signup";
	
	public static final String EMAIL = "email";
	public static final String FIELD_USERNAME_ALREADY_EXIST = "field_username_already_exist";

	@Inject
	private TemporaryPasswordConfig temporaryPasswordConfig;
	
	@Inject
	private CredentialsGenerator credentialGenerator;

	@Override
	protected List getGroups(JWTClaimsSet claimsSet) {
		return super.getGroups(claimsSet);
	}

	@Override
	public SignUpResponse execute(SignUpRequest request) throws FunctionException {
		SignUpResponse response = new SignUpResponse(); 

		String temporaryPassword = credentialGenerator.generate(temporaryPasswordConfig.length);
		String businessName = request.getHeaders().get(FunctionBuilder.HEADER_BUSINESS_NAME);
		
		AdminCreateUserRequest adminCreateUserRequest = new AdminCreateUserRequest()
				.withUserPoolId(config.getCognito().getUserPoolId())
				.withUsername(request.getEmail())
				.withUserAttributes(new AttributeType().withName(EMAIL).withValue(request.getEmail()))
				.withUserAttributes(new AttributeType().withName(FunctionConst.BUSINESS_ATTRIBUTE).withValue(businessName) )
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
			String businessAttributeValue = attributes.get(FunctionConst.BUSINESS_ATTRIBUTE);
			if (businessAttributeValue.contains(businessName)) {
				throw new UserExistsException(new Error(FIELD_USERNAME_ALREADY_EXIST, "Field username already exists"), mapper);
			}
			
		     AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = new AdminUpdateUserAttributesRequest()
				        .withUserPoolId(config.getCognito().getUserPoolId())
						.withUsername(request.getEmail());
				      
		      adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(FunctionConst.BUSINESS_ATTRIBUTE).withValue(businessAttributeValue + FunctionConst.BUSINESS_ATTRIBUTE_SEPARATOR + businessName));
		     
		     provider.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
			
		} 
		
	    if (temporaryPasswordConfig.returned) {
	    	response.setTemporaryPassword(temporaryPassword);
	    }
	    
	    response.setEmail(request.getEmail());	
	    response.setBusinessName(request.getHeaders().get(FunctionBuilder.HEADER_BUSINESS_NAME));
		return response;
	}

}