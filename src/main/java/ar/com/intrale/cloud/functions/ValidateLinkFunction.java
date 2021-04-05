package ar.com.intrale.cloud.functions;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import ar.com.intrale.cloud.Function;
import ar.com.intrale.cloud.FunctionException;
import ar.com.intrale.cloud.messages.ValidateLinkRequest;
import ar.com.intrale.cloud.messages.ValidateLinkResponse;

@Singleton
@Named(ValidateLinkFunction.FUNCTION_NAME)
public class ValidateLinkFunction extends Function<ValidateLinkRequest, ValidateLinkResponse, AmazonDynamoDB> {

	public static final String FUNCTION_NAME = "validatelink";
	
	public static final String TABLE_NAME = "businessRelations";

	public static final String EMAIL = "email";
	
	public static final String FIELD_USERNAME_ALREADY_EXIST = "field_username_already_exist";

	@Override
	public ValidateLinkResponse execute(ValidateLinkRequest request) throws FunctionException {
		ValidateLinkResponse response = new ValidateLinkResponse(); 

		DynamoDB dynamoDB = new DynamoDB(provider);
		Table table = dynamoDB.getTable(TABLE_NAME);
		
		QuerySpec querySpec = new QuerySpec()
				.withKeyConditionExpression(EMAIL + " = " + TWO_POINTS + EMAIL + " and " + BUSINESS_NAME + " = " + TWO_POINTS + BUSINESS_NAME)
				.withValueMap(new ValueMap()
						.withString(TWO_POINTS + EMAIL, request.getEmail())	
						.withString(TWO_POINTS + BUSINESS_NAME, request.getBusinessName()));
		
		ItemCollection<QueryOutcome> items = table.query(querySpec);
		response.setExists(items.iterator().hasNext());
		
		return response;
	}


}