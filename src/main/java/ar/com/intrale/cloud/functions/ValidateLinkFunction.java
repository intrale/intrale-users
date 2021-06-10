package ar.com.intrale.cloud.functions;

import java.util.Iterator;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;

import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.messages.ValidateLinkRequest;
import ar.com.intrale.cloud.messages.ValidateLinkResponse;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(ValidateLinkFunction.FUNCTION_NAME)
@Requires(property = IntraleFunction.APP_INSTANTIATE + ValidateLinkFunction.FUNCTION_NAME , value = IntraleFunction.TRUE, defaultValue = IntraleFunction.TRUE)
public class ValidateLinkFunction extends IntraleFunction<ValidateLinkRequest, ValidateLinkResponse, AWSCognitoIdentityProvider> {

	public static final String FUNCTION_NAME = "validatelink";
	
	public static final String EMAIL = "email";
	
	public static final String FIELD_USERNAME_ALREADY_EXIST = "field_username_already_exist";

	@Override
	public ValidateLinkResponse execute(ValidateLinkRequest request) throws FunctionException {
		ValidateLinkResponse response = new ValidateLinkResponse(); 

		AdminGetUserRequest adminGetUserRequest = new AdminGetUserRequest();
		adminGetUserRequest.setUserPoolId(config.getCognito().getUserPoolId());
		adminGetUserRequest.setUsername(request.getEmail());
		try {
			AdminGetUserResult adminGetUserResult =  provider.adminGetUser(adminGetUserRequest);
			
			Iterator<AttributeType> it = adminGetUserResult.getUserAttributes().iterator();
			while (it.hasNext()) {
				AttributeType attribute = (AttributeType) it.next();
				if (attribute.getName().contains(BUSINESS_ATTRIBUTE)) {
					if ((!attribute.getValue().isEmpty())) {
						response.setExists(attribute.getValue().contains(request.getHeaders().get(Lambda.HEADER_BUSINESS_NAME)));
					} else {
						response.setExists(Boolean.FALSE);
					}
					
				}
			}
		} catch (UserNotFoundException e) {
			response.setExists(Boolean.FALSE);
		}
		
		return response;
	}


}