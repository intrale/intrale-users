package ar.com.intrale.functions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminAddUserToGroupRequest;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserResult;
import com.amazonaws.services.cognitoidp.model.AdminRemoveUserFromGroupRequest;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.GroupType;

import ar.com.intrale.BaseFunction;
import ar.com.intrale.FunctionConst;
import ar.com.intrale.FunctionResponseToBase64HttpResponseBuilder;
import ar.com.intrale.Response;
import ar.com.intrale.exceptions.FunctionException;
import ar.com.intrale.messages.UpdateUserRequest;
import ar.com.intrale.messages.builders.StringToUpdateUserRequestBuilder;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;

@Singleton
@Named(FunctionConst.UPDATE)
@Requires(property = FunctionConst.APP_INSTANTIATE + FunctionConst.UPDATE , value = FunctionConst.TRUE, defaultValue = FunctionConst.TRUE)
public class UpdateUserFunction extends 
	BaseFunction<UpdateUserRequest, Response, AWSCognitoIdentityProvider, StringToUpdateUserRequestBuilder, FunctionResponseToBase64HttpResponseBuilder> {
	
	public static final String FAMILY_NAME = "family_name";
	public static final String NAME = "name";
	
	@Override
	public Response execute(UpdateUserRequest request) throws FunctionException {
	     AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = new AdminUpdateUserAttributesRequest()
			        .withUserPoolId(config.getCognito().getUserPoolId())
					.withUsername(request.getEmail());
			      
	     if (!StringUtils.isEmpty(request.getName())) {
	      adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(NAME).withValue(request.getName()));
	  	 }
	     if (!StringUtils.isEmpty(request.getFamilyName())) {
	    	  adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(FAMILY_NAME).withValue(request.getFamilyName()));
	     }
	     
	     provider.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
		
		AdminListGroupsForUserRequest adminListGroupsForUserRequest = new AdminListGroupsForUserRequest();
		adminListGroupsForUserRequest.setUserPoolId(config.getCognito().getUserPoolId());
		adminListGroupsForUserRequest.setUsername(request.getEmail());
		
		AdminListGroupsForUserResult result = provider.adminListGroupsForUser(adminListGroupsForUserRequest);
		
		Set<String> actualGroups = new HashSet<String>();
		if (result.getGroups()!=null) {
			Iterator<GroupType> it = result.getGroups().iterator();
			while (it.hasNext()) {
				GroupType groupType = (GroupType) it.next();
				if (!request.getGroups().contains(groupType.getGroupName())) {
					// deberiamos borrar el groupType.getGroupName()
					
					AdminRemoveUserFromGroupRequest adminRemoveUserFromGroupRequest = new AdminRemoveUserFromGroupRequest();
					adminRemoveUserFromGroupRequest.setUserPoolId(config.getCognito().getUserPoolId());
					adminRemoveUserFromGroupRequest.setUsername(request.getEmail());
					adminRemoveUserFromGroupRequest.setGroupName(groupType.getGroupName());
					provider.adminRemoveUserFromGroup(adminRemoveUserFromGroupRequest);
					
				} else {
					actualGroups.add(groupType.getGroupName());
				}
			}
		}
		
		Iterator<String> it = request.getGroups().iterator();
		while (it.hasNext()) {
			String actual = (String) it.next();
			if (!actualGroups.contains(actual)) {
				// deberiamos agregar el grupo "actual"
				AdminAddUserToGroupRequest adminAddUserToGroupRequest = new AdminAddUserToGroupRequest();
				adminAddUserToGroupRequest.setUserPoolId(config.getCognito().getUserPoolId());
				adminAddUserToGroupRequest.setUsername(request.getEmail());
				adminAddUserToGroupRequest.setGroupName(actual);
				provider.adminAddUserToGroup(adminAddUserToGroupRequest);
			}
		}
		
		
		return new Response();
	}

}
