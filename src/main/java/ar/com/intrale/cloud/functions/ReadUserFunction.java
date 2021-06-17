package ar.com.intrale.cloud.functions;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.GroupType;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.UserType;

import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.exceptions.FunctionException;
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
	
	@Override
	public ReadUserResponse execute(ReadUserRequest request) throws FunctionException {
		ReadUserResponse response = new ReadUserResponse();

		ListUsersRequest listUsersRequest = new ListUsersRequest();
		listUsersRequest.setUserPoolId(config.getCognito().getUserPoolId());
		
		ListUsersResult result = provider.listUsers(listUsersRequest);
		Iterator<UserType> it = result.getUsers().iterator();
		while (it.hasNext()) {
			UserType userType = (UserType) it.next();

			User user = new User();
			user.setEmail(userType.getUsername());
			user.setStatus(userType.getUserStatus());
			
			
			Map<String, String> attributes = userType.getAttributes().stream()
				      .collect(Collectors.toMap(AttributeType::getName, AttributeType::getValue));
			
			String businessNames = attributes.get(BUSINESS_ATTRIBUTE);
			if ((businessNames.contains(request.getHeaders().get(Lambda.HEADER_BUSINESS_NAME))) && 
					((StringUtils.isEmpty(request.getEmail())) || user.getEmail().contains(request.getEmail()))) {
				user.setName(attributes.get(NAME));
				user.setFamilyName(attributes.get(FAMILY_NAME));
				
				AdminListGroupsForUserRequest adminListGroupsForUserRequest = new AdminListGroupsForUserRequest();
				adminListGroupsForUserRequest.setUserPoolId(config.getCognito().getUserPoolId());
				adminListGroupsForUserRequest.setUsername(userType.getUsername());
				
				AdminListGroupsForUserResult adminListGroupsForUserResult = provider.adminListGroupsForUser(adminListGroupsForUserRequest);
				if (adminListGroupsForUserResult.getGroups()!=null) {
					Iterator<GroupType> itGroups = adminListGroupsForUserResult.getGroups().iterator();
					while (itGroups.hasNext()) {
						GroupType groupType = (GroupType) itGroups.next();
						user.addGroup(groupType.getGroupName(), groupType.getDescription());
					}
				}
				
				response.addUser(user);
			}
			
		}	
		
		return response;
	}

}
