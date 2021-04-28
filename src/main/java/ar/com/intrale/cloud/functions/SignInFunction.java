package ar.com.intrale.cloud.functions;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeRequest;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeResult;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.ChallengeNameType;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;

import ar.com.intrale.cloud.Error;
import ar.com.intrale.cloud.Function;
import ar.com.intrale.cloud.FunctionException;
import ar.com.intrale.cloud.NewPasswordRequiredException;
import ar.com.intrale.cloud.UnauthorizeExeption;
import ar.com.intrale.cloud.messages.SignInRequest;
import ar.com.intrale.cloud.messages.SignInResponse;
import ar.com.intrale.cloud.messages.ValidateLinkRequest;
import ar.com.intrale.cloud.messages.ValidateLinkResponse;
import io.micronaut.core.util.StringUtils;

@Singleton
@Named(SignInFunction.FUNCTION_NAME)
public class SignInFunction extends Function<SignInRequest, SignInResponse, AWSCognitoIdentityProvider> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SignInFunction.class);
	
	public static final String FUNCTION_NAME = "signin";

	public static final String NEW_PASSWORD_REQUIRED = "NEW_PASSWORD_REQUIRED";
	
	public static final String FAMILY_NAME = "family_name";
	public static final String NAME = "name";
	
	@Inject
	private ValidateLinkFunction validateLinkFunction;
	
	@Override
	public SignInResponse execute(SignInRequest request) throws FunctionException {
		try {
			LOGGER.debug("INTRALE: Link validation ");
			SignInResponse response = new SignInResponse();
			
			ValidateLinkRequest validateLinkRequest = new ValidateLinkRequest();
			validateLinkRequest.setBusinessName(request.getBusinessName());
			validateLinkRequest.setEmail(request.getEmail());
			validateLinkRequest.setRequestId(request.getRequestId());
			
			ValidateLinkResponse validateLinkResponse = validateLinkFunction.execute(validateLinkRequest);
			LOGGER.debug("INTRALE: Link validation finishing");
			
			if (validateLinkResponse.getExists()) {
			    final Map<String, String>authParams = new HashMap();
			    authParams.put("USERNAME", request.getEmail());  
			    authParams.put("PASSWORD", request.getPassword());
			    
			    LOGGER.debug("INTRALE: Pre Autenticacion ");
			 
			   final AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
			       authRequest.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
			       .withClientId(config.getAws().getClientId())
			       .withUserPoolId(config.getAws().getUserPoolId())
			       .withAuthParameters(authParams);
			 
			   AdminInitiateAuthResult result = provider.adminInitiateAuth(authRequest);
			   AuthenticationResultType authenticationResult = result.getAuthenticationResult();
			   
			   LOGGER.debug("INTRALE: Post Autenticacion ");
			   
			   if(NEW_PASSWORD_REQUIRED.equals(result.getChallengeName())){
				   LOGGER.debug("INTRALE: NEW PASSWORD REQUIRED ");
				   // La autenticacion solicita una nueva password
				   if (StringUtils.isEmpty(request.getNewPassword())) {
					   LOGGER.debug("INTRALE: NEW PASSWORD IS EMPTY ");
					   //return HttpResponse.unauthorized().body(NEW_PASSWORD_REQUIRED);
					   throw new NewPasswordRequiredException(new Error(NEW_PASSWORD_REQUIRED, NEW_PASSWORD_REQUIRED), mapper);
				   } else {
					   LOGGER.debug("INTRALE: NEW PASSWORD DETECTED "); 
					   final Map<String, String> challengeResponses = new HashMap();
				       challengeResponses.put("USERNAME", request.getEmail());
				       challengeResponses.put("PASS_WORD", request.getPassword());
				       //add the new password to the params map
				       challengeResponses.put("NEW_PASSWORD", request.getNewPassword());
				       //populate the challenge response
				        final AdminRespondToAuthChallengeRequest requestChallenge = new AdminRespondToAuthChallengeRequest();
				        requestChallenge.withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
				          .withChallengeResponses(challengeResponses)
				          .withClientId(config.getAws().getClientId())
					      .withUserPoolId(config.getAws().getUserPoolId())
				          .withSession(result.getSession());
				 
				      AdminRespondToAuthChallengeResult responseChallenge = provider.adminRespondToAuthChallenge(requestChallenge);
				      authenticationResult = responseChallenge.getAuthenticationResult();
				      
				      AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = new AdminUpdateUserAttributesRequest()
				        .withUserPoolId(config.getAws().getUserPoolId())
						.withUsername(request.getEmail());
				      
				      if (!StringUtils.isEmpty(request.getName())) {
				    	  adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(NAME).withValue(request.getName()));
			      	  }
				      if (!StringUtils.isEmpty(request.getFamilyName())) {
				    	  adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(FAMILY_NAME).withValue(request.getFamilyName()));
				      }
				      
				      adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName("email_verified").withValue("true"));
				      
				      provider.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
				      
				   }
			   }
			
			   LOGGER.debug("INTRALE: LOGIN FINALIZING ");
			   
			   response.setIdToken(authenticationResult.getIdToken());
			   response.setAccessToken(authenticationResult.getAccessToken());
			   response.setRefreshToken(authenticationResult.getRefreshToken());
			} else {
				throw new UnauthorizeExeption(new Error("UNAUTHORIZED", "NOT LINKED WITH BUSINESS " + request.getBusinessName()), mapper);
			}
		   LOGGER.info("finalizando handler");
	       return response;
	   } catch (NotAuthorizedException e) {
			throw new UnauthorizeExeption(new Error("UNAUTHORIZED", "UNAUTHORIZED"), mapper);
	   }
	}
	
}
