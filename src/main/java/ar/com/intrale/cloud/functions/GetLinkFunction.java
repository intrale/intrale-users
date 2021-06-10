package ar.com.intrale.cloud.functions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;

import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.messages.GetLinkRequest;
import ar.com.intrale.cloud.messages.GetLinkResponse;
import ar.com.intrale.cloud.messages.Link;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;

@Singleton
@Named(GetLinkFunction.FUNCTION_NAME)
@Requires(property = IntraleFunction.APP_INSTANTIATE + GetLinkFunction.FUNCTION_NAME , value = IntraleFunction.TRUE, defaultValue = IntraleFunction.TRUE)
public class GetLinkFunction extends IntraleFunction<GetLinkRequest, GetLinkResponse, AWSCognitoIdentityProvider> {

	public static final String FUNCTION_NAME = "getlink";
	
	public static final String EMAIL = "email";
	
	@Override
	public GetLinkResponse execute(GetLinkRequest request) throws FunctionException {
		GetLinkResponse response = new GetLinkResponse(); 
		
		AdminGetUserRequest adminGetUserRequest = new AdminGetUserRequest();
		adminGetUserRequest.setUserPoolId(config.getCognito().getUserPoolId());
		adminGetUserRequest.setUsername(request.getEmail());
		AdminGetUserResult adminGetUserResult =  provider.adminGetUser(adminGetUserRequest);
		
		if (adminGetUserRequest!=null) {
			String businessNames = StringUtils.EMPTY_STRING;
			if (adminGetUserResult.getUserAttributes()!=null) {
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
								Link link = new Link();
								link.setBusinessName(businessName);
								link.setEmail(request.getEmail());
								response.add(link);
								
							}
						}
					}
				}
			}
		}
		
		return response;
	}

}