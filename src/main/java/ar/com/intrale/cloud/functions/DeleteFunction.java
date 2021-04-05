package ar.com.intrale.cloud.functions;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserResult;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;

import ar.com.intrale.cloud.Function;
import ar.com.intrale.cloud.FunctionException;
import ar.com.intrale.cloud.messages.DeleteLinkRequest;
import ar.com.intrale.cloud.messages.DeleteLinkResponse;
import ar.com.intrale.cloud.messages.DeleteRequest;
import ar.com.intrale.cloud.messages.DeleteResponse;

@Singleton
@Named(DeleteFunction.FUNCTION_NAME)
public class DeleteFunction extends Function<DeleteRequest, DeleteResponse, AWSCognitoIdentityProvider> {

	public static final String FUNCTION_NAME = "delete";
	
	@Inject
	private DeleteLinkFunction deleteLinkFunction;

	@Override
	public DeleteResponse execute(DeleteRequest request) throws FunctionException {
		DeleteResponse response = new DeleteResponse(); 
		
		DeleteLinkRequest deleteLinkRequest = new DeleteLinkRequest();
		deleteLinkRequest.setRequestId(request.getRequestId());
		deleteLinkRequest.setBusinessName(request.getBusinessName());
		deleteLinkRequest.setEmail(request.getEmail());
		DeleteLinkResponse deleteLinkResponse = deleteLinkFunction.execute(deleteLinkRequest);
		
		if (deleteLinkResponse.getLinksCounts()==0) {
			try {
				AdminDeleteUserRequest adminDeleteUserRequest = new AdminDeleteUserRequest();
				adminDeleteUserRequest.setUserPoolId(config.getAws().getUserPoolId());
				adminDeleteUserRequest.setUsername(request.getEmail());
				AdminDeleteUserResult deleteResult = provider.adminDeleteUser(adminDeleteUserRequest);
			} catch (UserNotFoundException e) {
				// do nothing
			}
		}
		
		response.setUsername(request.getEmail());
		
		return response;
	}


}
