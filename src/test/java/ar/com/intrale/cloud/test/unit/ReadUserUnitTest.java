package ar.com.intrale.cloud.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserResult;
import com.amazonaws.services.cognitoidp.model.GroupType;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.UserStatusType;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.Runner;
import ar.com.intrale.cloud.messages.ReadUserRequest;
import ar.com.intrale.cloud.messages.ReadUserResponse;
import io.micronaut.test.annotation.MicronautTest;

//@MicronautTest
public class ReadUserUnitTest extends ar.com.intrale.cloud.Test {

	@Override
    public void beforeEach() {
    	applicationContext.registerSingleton(AWSCognitoIdentityProvider.class, Mockito.mock(AWSCognitoIdentityProvider.class));
    }

	@Override
	public void afterEach() {
    	AWSCognitoIdentityProvider provider = applicationContext.getBean(AWSCognitoIdentityProvider.class);
    	Mockito.reset(provider);
	}

    /*@Test
    public void testIsRunning() {
    	assertTrue(app.isRunning());
    }*/

  /*  @Test
    public void testRunner() {
    	Runner runner = new Runner();
    	runner.main(null);
    	assertTrue(Boolean.TRUE);
    }
    
    @Test
    public void test() throws JsonProcessingException {
    	AWSCognitoIdentityProvider awsCognitoIdentityProvider = applicationContext.getBean(AWSCognitoIdentityProvider.class);
    	AdminListGroupsForUserResult result = new AdminListGroupsForUserResult();
    	
    	Collection<GroupType> groupTypes = new ArrayList<GroupType>();
    	GroupType groupType = new GroupType();
    	groupType.setGroupName(DUMMY_VALUE);
    	groupType.setDescription(DUMMY_VALUE);
    	groupTypes.add(groupType);
    	
    	result.setGroups(groupTypes);
    	
    	Mockito.when(awsCognitoIdentityProvider.adminGetUser(any())).thenReturn(new AdminGetUserResult());
    	Mockito.when(awsCognitoIdentityProvider.adminListGroupsForUser(any())).thenReturn(result);
    	
    	ListUsersResult listUsersResult = Mockito.mock(ListUsersResult.class);
    	
    	List<UserType> users = new ArrayList<UserType>();
    	UserType userType = new UserType();
    	userType.setUsername(DUMMY_EMAIL);
    	userType.setUserStatus(UserStatusType.CONFIRMED);
    	users.add(userType);
    	Mockito.when(listUsersResult.getUsers()).thenReturn(users);
    	
    	Mockito.when(awsCognitoIdentityProvider.listUsers(any())).thenReturn(listUsersResult);
    	
    	
    	
    	ReadUserRequest readUserRequest = new ReadUserRequest();
    	readUserRequest.setRequestId(DUMMY_VALUE);
    	readUserRequest.setEmail(DUMMY_EMAIL);
    	
    	APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Lambda.HEADER_FUNCTION, IntraleFunction.READ);
        headers.put(Lambda.HEADER_BUSINESS_NAME, DUMMY_VALUE);
        requestEvent.setHeaders(headers);
        requestEvent.setBody(mapper.writeValueAsString(readUserRequest));
        APIGatewayProxyResponseEvent responseEvent = lambda.execute(requestEvent);
    	
        ReadUserResponse readUserResponse = mapper.readValue(responseEvent.getBody(), ReadUserResponse.class);
        assertEquals(200, readUserResponse.getStatusCode());
    }*/
    

}
