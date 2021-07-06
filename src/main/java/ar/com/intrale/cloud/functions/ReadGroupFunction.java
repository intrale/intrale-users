package ar.com.intrale.cloud.functions;

import java.util.List;
import java.util.function.Consumer;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.GroupType;
import com.amazonaws.services.cognitoidp.model.ListGroupsRequest;
import com.amazonaws.services.cognitoidp.model.ListGroupsResult;

import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.Request;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.messages.Group;
import ar.com.intrale.cloud.messages.ReadGroupResponse;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named(ReadGroupFunction.FUNCTION_NAME)
@Requires(property = IntraleFunction.APP_INSTANTIATE + ReadGroupFunction.FUNCTION_NAME , value = IntraleFunction.TRUE, defaultValue = IntraleFunction.TRUE)
public class ReadGroupFunction extends IntraleFunction<Request, ReadGroupResponse, AWSCognitoIdentityProvider> {

	public static final String FUNCTION_NAME = "readGroups";
	
	@Override
	public ReadGroupResponse execute(Request request) throws FunctionException {
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