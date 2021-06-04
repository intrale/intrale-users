package ar.com.intrale.cloud.test.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

import ar.com.intrale.cloud.Function;
import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.Runner;
import ar.com.intrale.cloud.functions.GetLinkFunction;
import ar.com.intrale.cloud.messages.GetLinkRequest;
import ar.com.intrale.cloud.messages.GetLinkResponse;
import io.micronaut.test.annotation.MicronautTest;

@MicronautTest
public class GetLinkUnitTest extends ar.com.intrale.cloud.Test {

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
    	
    	Collection<Map<String, AttributeValue>> items = new ArrayList<Map<String,AttributeValue>>();
    	Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
    	item.put(Function.BUSINESS_NAME, new AttributeValue(DUMMY_VALUE));
    	item.put(GetLinkFunction.EMAIL, new AttributeValue(DUMMY_EMAIL));
    	items.add(item);
    	queryResult.setItems(items);
    	
    	Mockito.when(providerDB.query(any())).thenReturn(queryResult);
    	
    	GetLinkRequest getLinkRequest = new GetLinkRequest();
    	getLinkRequest.setRequestId("1");
    	getLinkRequest.setBusinessName(DUMMY_VALUE);
    	getLinkRequest.setEmail(DUMMY_EMAIL);
    	
    	APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Lambda.HEADER_FUNCTION, GetLinkFunction.FUNCTION_NAME);
        requestEvent.setHeaders(headers);
        requestEvent.setBody(mapper.writeValueAsString(getLinkRequest));
        APIGatewayProxyResponseEvent responseEvent = lambda.execute(requestEvent);
        
        GetLinkResponse getLinkResponse = mapper.readValue(responseEvent.getBody(), GetLinkResponse.class);
        assertEquals(200, getLinkResponse.getStatusCode());
    }

}
