package ar.com.intrale.cloud.functions;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

import ar.com.intrale.cloud.Function;
import ar.com.intrale.cloud.FunctionException;
import ar.com.intrale.cloud.messages.DeleteLinkRequest;
import ar.com.intrale.cloud.messages.DeleteLinkResponse;

@Singleton
@Named(DeleteLinkFunction.FUNCTION_NAME)
public class DeleteLinkFunction extends Function<DeleteLinkRequest, DeleteLinkResponse, AmazonDynamoDB> {

	public static final String FUNCTION_NAME = "deletelink";
	
	public static final String TABLE_NAME = "businessRelations";

	public static final String EMAIL = "email";
	
	public static final String FIELD_USERNAME_ALREADY_EXIST = "field_username_already_exist";

	@Override
	public DeleteLinkResponse execute(DeleteLinkRequest request) throws FunctionException {
		DeleteLinkResponse response = new DeleteLinkResponse(); 

		DynamoDB dynamoDB = new DynamoDB(provider);
		Table table = dynamoDB.getTable(TABLE_NAME);
		
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put(BUSINESS_NAME, new AttributeValue(request.getBusinessName()));
		key.put(EMAIL, new AttributeValue(request.getEmail()));

		provider.deleteItem(TABLE_NAME, key);
		
		GetItemResult result =  provider.getItem(TABLE_NAME, key);
		if (result.getItem()!=null) {
			response.setLinksCounts(result.getItem().size());
		} else {
			response.setLinksCounts(0);
		}
		response.setUsername(request.getEmail());
		return response;
	}


}