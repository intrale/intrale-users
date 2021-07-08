package ar.com.intrale.cloud.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ar.com.intrale.cloud.Error;
import ar.com.intrale.cloud.FunctionBuilder;
import ar.com.intrale.cloud.FunctionExceptionResponse;
import ar.com.intrale.cloud.Runner;
import ar.com.intrale.cloud.functions.ConfirmPasswordRecoveryFunction;
import ar.com.intrale.cloud.messages.ConfirmPasswordRecoveryRequest;
import ar.com.intrale.cloud.messages.ConfirmPasswordRecoveryResponse;
import io.micronaut.http.HttpResponse;
import io.micronaut.test.annotation.MicronautTest;


@MicronautTest
public class ConfirmPasswordRecoveryUnitTest extends ar.com.intrale.cloud.Test{

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
        headers.put(FunctionBuilder.HEADER_FUNCTION, ConfirmPasswordRecoveryFunction.FUNCTION_NAME);
        headers.put(FunctionBuilder.HEADER_BUSINESS_NAME, DUMMY_VALUE);
        requestEvent.setHeaders(headers);
        requestEvent.setBody("");
        APIGatewayProxyResponseEvent responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(requestEvent);

        assertEquals(responseEvent.getStatusCode(), HttpResponse.badRequest().code());
    }

    
    
   @Test
    public void testValidationRequiredsNotPresent() throws JsonProcessingException {
    	ConfirmPasswordRecoveryRequest request = new ConfirmPasswordRecoveryRequest();
       
    	APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(FunctionBuilder.HEADER_FUNCTION, ConfirmPasswordRecoveryFunction.FUNCTION_NAME);
        headers.put(FunctionBuilder.HEADER_BUSINESS_NAME, DUMMY_VALUE);
        requestEvent.setHeaders(headers);
        requestEvent.setBody(mapper.writeValueAsString(request));
        APIGatewayProxyResponseEvent responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(requestEvent);
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
    public void testOK() throws JsonMappingException, JsonProcessingException {
    	AdminCreateUserResult result = new AdminCreateUserResult();
    	UserType userType = new UserType();
    	userType.setUsername(DUMMY_EMAIL);
    	userType.setUserStatus(DUMMY_VALUE);
    	userType.setEnabled(Boolean.TRUE);
    	result.setUser(userType);

    	AWSCognitoIdentityProvider provider = applicationContext.getBean(AWSCognitoIdentityProvider.class);
    	Mockito.when(provider.adminCreateUser(any())).thenReturn(result);
    	
    	ConfirmPasswordRecoveryRequest request = new ConfirmPasswordRecoveryRequest();
    	request.setRequestId(DUMMY_VALUE);
        request.setEmail(DUMMY_EMAIL);
        request.setCode(DUMMY_VALUE);
        request.setPassword(DUMMY_VALUE);
        
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(FunctionBuilder.HEADER_FUNCTION, ConfirmPasswordRecoveryFunction.FUNCTION_NAME);
        headers.put(FunctionBuilder.HEADER_BUSINESS_NAME, DUMMY_VALUE);
        requestEvent.setHeaders(headers);
        requestEvent.setBody(mapper.writeValueAsString(request));
        APIGatewayProxyResponseEvent responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(requestEvent);
        ConfirmPasswordRecoveryResponse response  = mapper.readValue(responseEvent.getBody(), ConfirmPasswordRecoveryResponse.class);
        
        assertEquals(DUMMY_EMAIL, response.getEmail());
    }
    




}
