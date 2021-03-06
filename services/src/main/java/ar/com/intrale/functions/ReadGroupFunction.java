package ar.com.intrale.functions;

import java.util.List;
import java.util.function.Consumer;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.GroupType;
import com.amazonaws.services.cognitoidp.model.ListGroupsRequest;
import com.amazonaws.services.cognitoidp.model.ListGroupsResult;

import ar.com.intrale.BaseFunction;
import ar.com.intrale.FunctionConst;
import ar.com.intrale.FunctionResponseToBase64HttpResponseBuilder;
import ar.com.intrale.StringToRequestDefaultBuilder;
import ar.com.intrale.exceptions.FunctionException;
import ar.com.intrale.messages.Group;
import ar.com.intrale.messages.ReadGroupResponse;
import ar.com.intrale.messages.RequestRoot;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(ReadGroupFunction.FUNCTION_NAME)
@Requires(property = FunctionConst.APP_INSTANTIATE + ReadGroupFunction.FUNCTION_NAME , value = FunctionConst.TRUE, defaultValue = FunctionConst.TRUE)
public class ReadGroupFunction extends 
	BaseFunction<RequestRoot, ReadGroupResponse, AWSCognitoIdentityProvider, StringToRequestDefaultBuilder, FunctionResponseToBase64HttpResponseBuilder> {

	public static final String FUNCTION_NAME = "readGroups";
	
	@Override
	public ReadGroupResponse execute(RequestRoot request) throws FunctionException {
		ReadGroupResponse response = new ReadGroupResponse();
		
		ListGroupsRequest groupsRequest = new ListGroupsRequest();
		groupsRequest.setUserPoolId(config.getCognito().getUserPoolId());		
		ListGroupsResult result = provider.listGroups(groupsRequest);
		
		List<GroupType> groups = result.getGroups();
		groups.forEach(new Consumer<GroupType>() {

			@Override
			public void accept(GroupType groupType) {
				Group group = new Group();
				group.setName(groupType.getGroupName());
				group.setDescription(groupType.getDescription());
				response.addGroup(group);
			}
		});
		
		return response;
	}

}
