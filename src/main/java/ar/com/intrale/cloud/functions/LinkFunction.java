package ar.com.intrale.cloud.functions;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import ar.com.intrale.cloud.Error;
import ar.com.intrale.cloud.Function;
import ar.com.intrale.cloud.FunctionException;
import ar.com.intrale.cloud.UserExistsException;
import ar.com.intrale.cloud.messages.LinkRequest;
import ar.com.intrale.cloud.messages.LinkResponse;

@Singleton
@Named(LinkFunction.FUNCTION_NAME)
public class LinkFunction extends Function<LinkRequest, LinkResponse, AmazonDynamoDB> {

	public static final String FUNCTION_NAME = "link";
	
	public static final String TABLE_NAME = "businessRelations";

	public static final String EMAIL = "email";
	
	public static final String FIELD_USERNAME_ALREADY_EXIST = "field_username_already_exist";

	@Override
	public LinkResponse execute(LinkRequest request) throws FunctionException {
		LinkResponse response = new LinkResponse(); 

		DynamoDB dynamoDB = new DynamoDB(provider);
		Table table = dynamoDB.getTable(TABLE_NAME);
		
		QuerySpec querySpec = new QuerySpec()
				.withKeyConditionExpression(EMAIL + " = " + TWO_POINTS + EMAIL + " and " + BUSINESS_NAME + " = " + TWO_POINTS + BUSINESS_NAME)
				.withValueMap(new ValueMap()
						.withString(TWO_POINTS + EMAIL, request.getEmail())	
						.withString(TWO_POINTS + BUSINESS_NAME, request.getBusinessName()));
		
		ItemCollection<QueryOutcome> items = table.query(querySpec);
		if (!items.iterator().hasNext()) {
			Map<String, AttributeValue> attributesValues = new HashMap<String, AttributeValue>(); 
			attributesValues.put(BUSINESS_NAME, new AttributeValue(request.getBusinessName()));
			attributesValues.put(EMAIL, new AttributeValue(request.getEmail()));
			
			provider.putItem(TABLE_NAME, attributesValues);
		} else {
			throw new UserExistsException(new Error(FIELD_USERNAME_ALREADY_EXIST, "Field username already exists"), mapper);

		}
		
		
		return response;
	}


}