package ar.com.intrale.cloud.functions;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import ar.com.intrale.cloud.Function;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.messages.GetLinkRequest;
import ar.com.intrale.cloud.messages.GetLinkResponse;
import ar.com.intrale.cloud.messages.Link;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;

@Singleton
@Named(GetLinkFunction.FUNCTION_NAME)
@Requires(property = Function.APP_INSTANTIATE + GetLinkFunction.FUNCTION_NAME , value = Function.TRUE, defaultValue = Function.TRUE)
public class GetLinkFunction extends Function<GetLinkRequest, GetLinkResponse, AmazonDynamoDB> {

	public static final String FUNCTION_NAME = "getlink";
	
	public static final String TABLE_NAME = "businessRelations";

	public static final String EMAIL = "email";
	
	@Override
	public GetLinkResponse execute(GetLinkRequest request) throws FunctionException {
		GetLinkResponse response = new GetLinkResponse(); 
		Boolean filtered = !StringUtils.isEmpty(request.getBusinessName()) || !StringUtils.isEmpty(request.getEmail());
		
		if (filtered) {
			getItemsFiltered(request, response);
		} else {
			ScanResult result = provider.scan(new ScanRequest().withTableName(TABLE_NAME));
			List<Map<String, AttributeValue>> items = result.getItems();
			if (items!=null) {
				Iterator<Map<String, AttributeValue>> it = items.iterator();
				while (it.hasNext()) {
					Map<String, AttributeValue> map = (Map<String, AttributeValue>) it.next();
					Link link = new Link();
					link.setBusinessName(map.get(BUSINESS_NAME).getS());
					link.setEmail(map.get(EMAIL).getS());
					response.add(link);
				}
			}
		}
		
		return response;
	}

	private void getItemsFiltered(GetLinkRequest request, GetLinkResponse response) {
		DynamoDB dynamoDB = new DynamoDB(provider);
		Table table = dynamoDB.getTable(TABLE_NAME);
		
		ValueMap valueMap = new ValueMap();
		StringBuilder keyConditionExpression = new StringBuilder();
		
		if (!StringUtils.isEmpty(request.getBusinessName())) {
			keyConditionExpression.append(BUSINESS_NAME + " = " + TWO_POINTS + BUSINESS_NAME);
			valueMap.withString(TWO_POINTS + BUSINESS_NAME, request.getBusinessName());
		}
		if (!StringUtils.isEmpty(request.getEmail())) {
			if (keyConditionExpression.length()>0) {
				keyConditionExpression.append(" and ");
			}
			keyConditionExpression.append(EMAIL + " = " + TWO_POINTS + EMAIL);
			valueMap.withString(TWO_POINTS + EMAIL, request.getEmail());
		}
		
		QuerySpec querySpec = new QuerySpec();
		if (keyConditionExpression.length()>0) {
			querySpec.withKeyConditionExpression(keyConditionExpression.toString())
				.withValueMap(valueMap);
		} 

		ItemCollection<QueryOutcome> items = table.query(querySpec);
		
		Iterator<Item> iterator = items.iterator();

		while (iterator.hasNext()) {
			Item item = (Item) iterator.next();
			Link link = new Link();
			link.setBusinessName(item.getString(BUSINESS_NAME));
			link.setEmail(item.getString(EMAIL));
			response.add(link);
		}
	}


}