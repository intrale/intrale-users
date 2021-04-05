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
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserResult;
import com.amazonaws.services.cognitoidp.model.GroupType;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.UserStatusType;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

import ar.com.intrale.cloud.Function;
import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.Runner;
import ar.com.intrale.cloud.functions.GetLinkFunction;
import ar.com.intrale.cloud.messages.ReadUserRequest;
import ar.com.intrale.cloud.messages.ReadUserResponse;
import io.micronaut.test.annotation.MicronautTest;

@MicronautTest
public class ReadUserUnitTest extends ar.com.intrale.cloud.Test {

	@Override
    public void beforeEach() {
    	applicationContext.registerSingleton(AWSCognitoIdentityProvider.class, Mockito.mock(AWSCognitoIdentityProvider.class));
    	applicationContext.registerSingleton(AmazonDynamoDB.class, Mockito.mock(AmazonDynamoDB.class));
    }

	@Override
	public void afterEach() {
    	AWSCognitoIdentityProvider provider = applicationContext.getBean(AWSCognitoIdentityProvider.class);
    	Mockito.reset(provider);
	}

    @Test
    public void testIsRunning() {
    	assertTrue(app.isRunning());
    }

    @Test
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
    	
    	Mockito.when(awsCognitoIdentityProvider.adminListGroupsForUser(any())).thenReturn(result);
    	
    	ListUsersResult listUsersResult = Mockito.mock(ListUsersResult.class);
    	
    	List<UserType> users = new ArrayList<UserType>();
    	UserType userType = new UserType();
    	userType.setUsername(DUMMY_EMAIL);
    	userType.setUserStatus(UserStatusType.CONFIRMED);
    	users.add(userType);
    	Mockito.when(listUsersResult.getUsers()).thenReturn(users);
    	
    	Mockito.when(awsCognitoIdentityProvider.listUsers(any())).thenReturn(listUsersResult);
    	
    	
    	AmazonDynamoDB amazonDynamoDB = applicationContext.getBean(AmazonDynamoDB.class);
    	ScanResult scanResult = new ScanResult();
    	Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
    	Collection<Map<String, AttributeValue>> items = new ArrayList<Map<String,AttributeValue>>();
    	item.put(Function.BUSINESS_NAME, new AttributeValue(DUMMY_VALUE));
    	item.put(GetLinkFunction.EMAIL, new AttributeValue(DUMMY_EMAIL));
    	items.add(item);
    	scanResult.setItems(items);
    	
    	Mockito.when(amazonDynamoDB.scan(any())).thenReturn(scanResult);
    	
    	ReadUserRequest readUserRequest = new ReadUserRequest();
    	readUserRequest.setRequestId(DUMMY_VALUE);
    	readUserRequest.setBusinessName(DUMMY_VALUE);
    	readUserRequest.setEmail(DUMMY_EMAIL);
    	
    	APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Lambda.FUNCTION, Function.READ);
        requestEvent.setHeaders(headers);
        requestEvent.setBody(mapper.writeValueAsString(readUserRequest));
        APIGatewayProxyResponseEvent responseEvent = lambda.execute(requestEvent);
    	
        ReadUserResponse readUserResponse = mapper.readValue(responseEvent.getBody(), ReadUserResponse.class);
        assertEquals(200, readUserResponse.getStatusCode());
    }
    

}
