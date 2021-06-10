package ar.com.intrale.cloud.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;

import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.messages.DeleteLinkRequest;
import ar.com.intrale.cloud.messages.DeleteLinkResponse;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(DeleteLinkFunction.FUNCTION_NAME)
@Requires(property = IntraleFunction.APP_INSTANTIATE + DeleteLinkFunction.FUNCTION_NAME , value = IntraleFunction.TRUE, defaultValue = IntraleFunction.TRUE)
public class DeleteLinkFunction extends IntraleFunction<DeleteLinkRequest, DeleteLinkResponse, AWSCognitoIdentityProvider> {

	public static final String FUNCTION_NAME = "deletelink";

	public static final String EMAIL = "email";
	
	public static final String FIELD_USERNAME_ALREADY_EXIST = "field_username_already_exist";

	@Override
	public DeleteLinkResponse execute(DeleteLinkRequest request) throws FunctionException {
		DeleteLinkResponse response = new DeleteLinkResponse(); 

		AdminGetUserRequest adminGetUserRequest = new AdminGetUserRequest();
		adminGetUserRequest.setUserPoolId(config.getCognito().getUserPoolId());
		adminGetUserRequest.setUsername(request.getEmail());
		try { 
			AdminGetUserResult adminGetUserResult =  provider.adminGetUser(adminGetUserRequest);
			
			List businessNames = new ArrayList();
			Iterator<AttributeType> it = adminGetUserResult.getUserAttributes().iterator();
			while (it.hasNext()) {
				AttributeType attribute = (AttributeType) it.next();
				if (attribute.getName().contains(BUSINESS_ATTRIBUTE)) {
					if ((!attribute.getValue().isEmpty())) {
						businessNames = new ArrayList(Arrays.asList(attribute.getValue().split(BUSINESS_ATTRIBUTE_SEPARATOR)));
						businessNames.remove(request.getHeaders().get(Lambda.HEADER_BUSINESS_NAME));
					}
				}
			}
			
			response.setLinksCounts(businessNames.size());
			
			AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = new AdminUpdateUserAttributesRequest()
			        .withUserPoolId(config.getCognito().getUserPoolId())
					.withUsername(request.getEmail());
		    adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(BUSINESS_ATTRIBUTE).withValue(String.join(BUSINESS_ATTRIBUTE_SEPARATOR , businessNames)));
		    provider.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
		} catch (UserNotFoundException e) {
			// do nothing
			response.setLinksCounts(0);
		}
				
		response.setUsername(request.getEmail());
		return response;
	}


}