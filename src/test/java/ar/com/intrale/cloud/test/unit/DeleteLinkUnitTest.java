package ar.com.intrale.cloud.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
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
    public void testFilter() throws JsonProcessingException {
    	AmazonDynamoDB providerDB = applicationContext.getBean(AmazonDynamoDB.class);
    	PutItemResult putItemResult = new PutItemResult();
    	Mockito.when(providerDB.putItem(any())).thenReturn(putItemResult);
    	QueryResult queryResult = new QueryResult();
    	Mockito.when(providerDB.query(any())).thenReturn(queryResult);
    	
    	GetItemResult getItemResult = new GetItemResult();
    	getItemResult.setItem(new HashMap<String, AttributeValue>());
    	Mockito.when(providerDB.getItem(any(), any())).thenReturn(getItemResult);
    	
    	DeleteLinkRequest deleteLinkRequest = new DeleteLinkRequest();
    	deleteLinkRequest.setRequestId("1");
    	deleteLinkRequest.setBusinessName(DUMMY_VALUE);
    	deleteLinkRequest.setEmail(DUMMY_EMAIL);
    	
    	APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Lambda.HEADER_FUNCTION, DeleteLinkFunction.FUNCTION_NAME);
        requestEvent.setHeaders(headers);
        requestEvent.setBody(mapper.writeValueAsString(deleteLinkRequest));
        APIGatewayProxyResponseEvent responseEvent = lambda.execute(requestEvent);
        
        DeleteLinkResponse deleteLinkResponse = mapper.readValue(responseEvent.getBody(), DeleteLinkResponse.class);
        assertEquals(200, deleteLinkResponse.getStatusCode());
    }

}
