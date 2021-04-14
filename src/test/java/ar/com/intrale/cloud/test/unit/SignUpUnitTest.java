package ar.com.intrale.cloud.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ar.com.intrale.cloud.Error;
import ar.com.intrale.cloud.FunctionExceptionResponse;
import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.Runner;
import ar.com.intrale.cloud.functions.LinkFunction;
import ar.com.intrale.cloud.functions.SignUpFunction;
import ar.com.intrale.cloud.messages.SignUpRequest;
import ar.com.intrale.cloud.messages.SignUpResponse;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MicronautTest;


@MicronautTest
public class SignUpUnitTest extends ar.com.intrale.cloud.Test{

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

    /*@Test
    public void testIsRunning() {
    	assertTrue(app.isRunning());
    }*/

    @Test
    public void testRunner() {
    	Runner runner = new Runner();
    	runner.main(null);
    	assertTrue(Boolean.TRUE);
    }
    
    @Test
    public void testRequestNull() throws JsonProcessingException {
    	APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Lambda.FUNCTION, SignUpFunction.FUNCTION_NAME);
        requestEvent.setHeaders(headers);
        requestEvent.setBody("");
        APIGatewayProxyResponseEvent responseEvent = lambda.execute(requestEvent);

        assertEquals(responseEvent.getStatusCode(), HttpResponse.badRequest().code());
    }

    
    
   @Test
    public void testValidationRequiredsNotPresent() throws JsonProcessingException {
    	SignUpRequest request = new SignUpRequest();
       
    	APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Lambda.FUNCTION, SignUpFunction.FUNCTION_NAME);
        requestEvent.setHeaders(headers);
        requestEvent.setBody(mapper.writeValueAsString(request));
        APIGatewayProxyResponseEvent responseEvent = lambda.execute(requestEvent);
        FunctionExceptionResponse functionExceptionResponse  = mapper.readValue(responseEvent.getBody(), FunctionExceptionResponse.class);

        assertTrue(functionExceptionResponse.getErrors().size()>0);
        assertTrue(containError(functionExceptionResponse.getErrors(), "email"));
    }

	private Boolean containError(Collection<Error> errors, String code) {
		Boolean contain = Boolean.FALSE;
        Iterator<Error> it = errors.iterator();
        while (it.hasNext()) {
			Error error = (Error) it.next();
			if (code.equals(error.getCode())) {
				contain = Boolean.TRUE;
			}
		}
        return contain;
	}
    
    @Test
    public void testSignupOK() throws JsonMappingException, JsonProcessingException {
    	AdminCreateUserResult result = new AdminCreateUserResult();
    	UserType userType = new UserType();
    	userType.setUsername(DUMMY_EMAIL);
    	userType.setUserStatus(DUMMY_VALUE);
    	userType.setEnabled(Boolean.TRUE);
    	result.setUser(userType);

    	AWSCognitoIdentityProvider provider = applicationContext.getBean(AWSCognitoIdentityProvider.class);
    	Mockito.when(provider.adminCreateUser(any())).thenReturn(result);
    	
    	AmazonDynamoDB providerDB = applicationContext.getBean(AmazonDynamoDB.class);
    	PutItemResult putItemResult = new PutItemResult();
    	Mockito.when(providerDB.putItem(any())).thenReturn(putItemResult);
    	QueryResult queryResult = new QueryResult();
    	Mockito.when(providerDB.query(any())).thenReturn(queryResult);
    	
    	SignUpRequest request = new SignUpRequest();
    	request.setBusinessName(DUMMY_VALUE);
    	request.setRequestId(DUMMY_VALUE);
        request.setEmail(DUMMY_EMAIL);
        
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Lambda.FUNCTION, SignUpFunction.FUNCTION_NAME);
        requestEvent.setHeaders(headers);
        requestEvent.setBody(mapper.writeValueAsString(request));
        APIGatewayProxyResponseEvent responseEvent = lambda.execute(requestEvent);
        SignUpResponse response  = mapper.readValue(responseEvent.getBody(), SignUpResponse.class);
        
        assertEquals(DUMMY_EMAIL, response.getEmail());
    }
    
    @Test
    public void testUsernameExist() throws JsonMappingException, JsonProcessingException {
    	AWSCognitoIdentityProvider provider = applicationContext.getBean(AWSCognitoIdentityProvider.class);
    	Mockito.when(provider.adminCreateUser(any())).thenThrow(UsernameExistsException.class);
    	AmazonDynamoDB providerDB = applicationContext.getBean(AmazonDynamoDB.class);
    	PutItemResult putItemResult = new PutItemResult();
    	Mockito.when(providerDB.putItem(any())).thenReturn(putItemResult);
    	QueryResult queryResult = new QueryResult();
    	queryResult.setCount(1);
    	Collection<Map<String, AttributeValue>> items = new ArrayList<Map<String, AttributeValue>>();
    	Map<String, AttributeValue> values = new HashMap<String, AttributeValue>();
    	values.put(LinkFunction.EMAIL, new AttributeValue(DUMMY_EMAIL));
    	values.put(LinkFunction.BUSINESS_NAME, new AttributeValue(DUMMY_VALUE));
    	items.add(values);
    	queryResult.setItems(items);;
    	Mockito.when(providerDB.query(any())).thenReturn(queryResult);
    	
    	SignUpRequest request = new SignUpRequest();
    	request.setBusinessName(DUMMY_VALUE);
    	request.setRequestId(DUMMY_VALUE);
        request.setEmail(DUMMY_EMAIL);
        
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Lambda.FUNCTION, SignUpFunction.FUNCTION_NAME);
        requestEvent.setHeaders(headers);
        requestEvent.setBody(mapper.writeValueAsString(request));
        APIGatewayProxyResponseEvent responseEvent = lambda.execute(requestEvent);
        FunctionExceptionResponse functionExceptionResponse  = mapper.readValue(responseEvent.getBody(), FunctionExceptionResponse.class);
        
        assertEquals(HttpStatus.BAD_REQUEST.getCode(), responseEvent.getStatusCode());
        assertTrue(containError(functionExceptionResponse.getErrors(), SignUpFunction.FIELD_USERNAME_ALREADY_EXIST));
    }



}
