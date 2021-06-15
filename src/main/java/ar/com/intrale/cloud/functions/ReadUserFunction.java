package ar.com.intrale.cloud.functions;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.UserType;

import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.messages.GetLinkRequest;
import ar.com.intrale.cloud.messages.GetLinkResponse;
import ar.com.intrale.cloud.messages.Link;
import ar.com.intrale.cloud.messages.ReadUserRequest;
import ar.com.intrale.cloud.messages.ReadUserResponse;
import ar.com.intrale.cloud.messages.User;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;

@Singleton
@Named(IntraleFunction.READ)
@Requires(property = IntraleFunction.APP_INSTANTIATE + IntraleFunction.READ , value = IntraleFunction.TRUE, defaultValue = IntraleFunction.TRUE)
public class ReadUserFunction extends IntraleFunction<ReadUserRequest, ReadUserResponse, AWSCognitoIdentityProvider> {

	public static final String EMAIL = "email";
	public static final String FAMILY_NAME = "family_name";
	public static final String NAME = "name";
	
	private Map<String, String> links;
	
	@Override
	public ReadUserResponse execute(ReadUserRequest request) throws FunctionException {
		ReadUserResponse response = new ReadUserResponse();
		
		if (links==null) {
			GetLinkRequest getLinkRequest = new GetLinkRequest();
			getLinkRequest.setRequestId(request.getRequestId());
			getLinkRequest.setEmail(request.getEmail());
			
			GetLinkFunction getLinkFunction = applicationContext.getBean(GetLinkFunction.class);
			GetLinkResponse getLinkResponse = getLinkFunction.execute(getLinkRequest);
			Collection<Link> linksResponse = getLinkResponse.getLinks();
			
			links = linksResponse.stream().collect(Collectors.toMap(Link::getCompleteName, Link::getEmail));					
		}
		
		ListUsersRequest listUsersRequest = new ListUsersRequest();
		listUsersRequest.setUserPoolId(config.getCognito().getUserPoolId());
		
		ListUsersResult result = provider.listUsers(listUsersRequest);
		Iterator<UserType> it = result.getUsers().iterator();
		while (it.hasNext()) {
			UserType userType = (UserType) it.next();

			User user = new User();
			user.setEmail(userType.getUsername());
			user.setStatus(userType.getUserStatus());
			
			if (userType.getAttributes()!=null) {
				Iterator<AttributeType> itAttributes = userType.getAttributes().iterator();
				while (itAttributes.hasNext()) {
					AttributeType attributeType = (AttributeType) itAttributes.next();
					if (NAME.equals(attributeType.getName())) {
						user.setName(attributeType.getValue());
					}
					if (FAMILY_NAME.equals(attributeType.getName())) {
						user.setFamilyName(attributeType.getValue());
					}
				}
			}
			
			// Filtrar antes de seguir
			Boolean match = Boolean.TRUE;
			
			// Filtro por negocio
			match = links.containsKey(request.getHeaders().get(Lambda.HEADER_BUSINESS_NAME) + Link.SEPARATOR + user.getEmail() );
			
			// Filtro por email / nombre de usuario
			if (match  && !StringUtils.isEmpty(request.getEmail())) {
				match = user.getEmail().contains(request.getEmail());
			} 
			
			if (match) {
				response.addUser(user);
				//TODO: Resta gestionar los grupos como perfiles de usuario
				/*AdminListGroupsForUserRequest adminListGroupsForUserRequest = new AdminListGroupsForUserRequest();
				adminListGroupsForUserRequest.setUserPoolId(config.getCognito().getUserPoolId());
				adminListGroupsForUserRequest.setUsername(userType.getUsername());
				
				AdminListGroupsForUserResult adminListGroupsForUserResult = provider.adminListGroupsForUser(adminListGroupsForUserRequest);
				if (adminListGroupsForUserResult.getGroups()!=null) {
					Iterator<GroupType> itGroups = adminListGroupsForUserResult.getGroups().iterator();
					while (itGroups.hasNext()) {
						GroupType groupType = (GroupType) itGroups.next();
						user.addGroup(groupType.getGroupName(), groupType.getDescription());
					}
				}*/
			}
			
			
		}	
		
		return response;
	}

}
