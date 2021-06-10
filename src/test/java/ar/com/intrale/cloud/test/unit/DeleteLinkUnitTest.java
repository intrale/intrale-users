package ar.com.intrale.cloud.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.Runner;
import ar.com.intrale.cloud.functions.DeleteLinkFunction;
import ar.com.intrale.cloud.messages.DeleteLinkRequest;
import ar.com.intrale.cloud.messages.DeleteLinkResponse;
import io.micronaut.test.annotation.MicronautTest;

@MicronautTest
public class DeleteLinkUnitTest extends ar.com.intrale.cloud.Test {

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
    public void testFilter() throws JsonProcessingException {
    	
    	AWSCognitoIdentityProvider provider = applicationContext.getBean(AWSCognitoIdentityProvider.class);
    	AdminGetUserResult adminGetUserResult = Mockito.mock(AdminGetUserResult.class);
    	Mockito.when(provider.adminGetUser(any())).thenReturn(adminGetUserResult);
    	Mockito.when(adminGetUserResult.getUserAttributes()).thenReturn(new ArrayList<AttributeType>());
    	Mockito.when(provider.adminUpdateUserAttributes(any())).thenReturn(new AdminUpdateUserAttributesResult());
    	
    	DeleteLinkRequest deleteLinkRequest = new DeleteLinkRequest();
    	deleteLinkRequest.setRequestId("1");
    	deleteLinkRequest.setEmail(DUMMY_EMAIL);
    	
    	APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Lambda.HEADER_FUNCTION, DeleteLinkFunction.FUNCTION_NAME);
        headers.put(Lambda.HEADER_BUSINESS_NAME, DUMMY_VALUE);
        requestEvent.setHeaders(headers);
        requestEvent.setBody(mapper.writeValueAsString(deleteLinkRequest));
        APIGatewayProxyResponseEvent responseEvent = lambda.execute(requestEvent);
        
        DeleteLinkResponse deleteLinkResponse = mapper.readValue(responseEvent.getBody(), DeleteLinkResponse.class);
        assertEquals(200, deleteLinkResponse.getStatusCode());
    }

}
