package ar.com.intrale.cloud.functions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;

import ar.com.intrale.cloud.Error;
import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.exceptions.UserExistsException;
import ar.com.intrale.cloud.messages.LinkRequest;
import ar.com.intrale.cloud.messages.LinkResponse;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;

@Singleton
@Named(LinkFunction.FUNCTION_NAME)
@Requires(property = IntraleFunction.APP_INSTANTIATE + LinkFunction.FUNCTION_NAME , value = IntraleFunction.TRUE, defaultValue = IntraleFunction.TRUE)
public class LinkFunction extends IntraleFunction<LinkRequest, LinkResponse, AWSCognitoIdentityProvider> {

	public static final String FUNCTION_NAME = "link";
	
	public static final String EMAIL = "email";
	
	public static final String FIELD_USERNAME_ALREADY_EXIST = "field_username_already_exist";

	@Override
	public LinkResponse execute(LinkRequest request) throws FunctionException {
		LinkResponse response = new LinkResponse(); 

		// Se valida si ya se encuentra registrada la relacion del usuario con el negocio
		AdminGetUserRequest adminGetUserRequest = new AdminGetUserRequest();
		adminGetUserRequest.setUserPoolId(config.getCognito().getUserPoolId());
		adminGetUserRequest.setUsername(request.getEmail());
		AdminGetUserResult adminGetUserResult =  provider.adminGetUser(adminGetUserRequest);
		
		String businessNames = StringUtils.EMPTY_STRING;
		Iterator<AttributeType> it = adminGetUserResult.getUserAttributes().iterator();
		while (it.hasNext()) {
			AttributeType attribute = (AttributeType) it.next();
			if (attribute.getName().contains(BUSINESS_ATTRIBUTE)) {
				if ((!attribute.getValue().isEmpty())) {
					businessNames = attribute.getValue();
					List<String> businessNamesRegistered = Arrays.asList(businessNames.split(BUSINESS_ATTRIBUTE_SEPARATOR));
					Iterator<String> itBusinessNamesRegistered = businessNamesRegistered.iterator();
					while (itBusinessNamesRegistered.hasNext()) {
						String businessName = (String) itBusinessNamesRegistered.next();
						if (businessName.equals(request.getHeaders().get(Lambda.HEADER_BUSINESS_NAME))) {
							throw new UserExistsException(new Error(FIELD_USERNAME_ALREADY_EXIST, "Field username already exists"), mapper);
						}
						
					}
				}
			}
		}

		if (!businessNames.isEmpty()) {
			businessNames += BUSINESS_ATTRIBUTE_SEPARATOR;
		}
		businessNames += request.getHeaders().get(Lambda.HEADER_BUSINESS_NAME);
		
	    AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = new AdminUpdateUserAttributesRequest()
			        .withUserPoolId(config.getCognito().getUserPoolId())
					.withUsername(request.getEmail());
	    adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(BUSINESS_ATTRIBUTE).withValue(businessNames));
	    provider.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
		
		return response;
	}


}