package ar.com.intrale.functions;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
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
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;

import ar.com.intrale.BaseFunction;
import ar.com.intrale.Error;
import ar.com.intrale.FunctionBuilder;
import ar.com.intrale.FunctionConst;
import ar.com.intrale.FunctionResponseToBase64HttpResponseBuilder;
import ar.com.intrale.exceptions.FunctionException;
import ar.com.intrale.exceptions.NewPasswordRequiredException;
import ar.com.intrale.exceptions.UnauthorizeExeption;
import ar.com.intrale.messages.SignInRequest;
import ar.com.intrale.messages.SignInResponse;
import ar.com.intrale.messages.builders.StringToSignInRequestBuilder;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;

@Singleton
@Named(SignInFunction.FUNCTION_NAME)
@Requires(property = FunctionConst.APP_INSTANTIATE + SignInFunction.FUNCTION_NAME , value = FunctionConst.TRUE, defaultValue = FunctionConst.TRUE)
public class SignInFunction extends 
	BaseFunction<SignInRequest, SignInResponse, AWSCognitoIdentityProvider, StringToSignInRequestBuilder, FunctionResponseToBase64HttpResponseBuilder> {
	
	private static final String NOT_LINKED_WITH_BUSINESS_MESSAGE = "NOT LINKED WITH BUSINESS ";

	private static final String EMAIL_VERIFIED_ATTRIBUTE = "email_verified";

	private static final String NEW_PASSWORD_PARAM = "NEW_PASSWORD";

	private static final String PASS_WORD_PARAM = "PASS_WORD";

	private static final String PASSWORD_PARAM = "PASSWORD";

	private static final String USERNAME_PARAM = "USERNAME";

	private static final Logger LOGGER = LoggerFactory.getLogger(SignInFunction.class);
	
	public static final String FUNCTION_NAME = "signin";

	public static final String NEW_PASSWORD_REQUIRED = "NEW_PASSWORD_REQUIRED";
	
	public static final String FAMILY_NAME = "family_name";
	public static final String NAME = "name";
	
	@Override
	public SignInResponse execute(SignInRequest request) throws FunctionException {
		LOGGER.info("INTRALE: LOGIN INITIALIZING ");
		SignInResponse response = new SignInResponse();
		try {
			String businessName = request.getHeaders().get(FunctionBuilder.HEADER_BUSINESS_NAME);
		
			AdminGetUserResult adminGetUserResult = null;
			try {
				AdminGetUserRequest adminGetUserRequest = new AdminGetUserRequest();
				adminGetUserRequest.setUserPoolId(config.getCognito().getUserPoolId());
				adminGetUserRequest.setUsername(request.getEmail());
				adminGetUserResult =  provider.adminGetUser(adminGetUserRequest);
			} catch (UserNotFoundException e) {
				throw new UnauthorizeExeption(new Error(FunctionConst.UNAUTHORIZED, FunctionConst.UNAUTHORIZED), mapper);
			}
			
			Map<String, String> attributes = adminGetUserResult.getUserAttributes().stream()
				      .collect(Collectors.toMap(AttributeType::getName, AttributeType::getValue));
			String businessAttributeValue = attributes.get(FunctionConst.BUSINESS_ATTRIBUTE);
			
			if (businessAttributeValue.contains(businessName)) {
			    final Map<String, String>authParams = new HashMap();
			    authParams.put(USERNAME_PARAM, request.getEmail());  
			    authParams.put(PASSWORD_PARAM, request.getPassword());
			    
			    LOGGER.info("INTRALE: Pre Autenticacion ");
			 
			   final AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
			       authRequest.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
			       .withClientId(config.getCognito().getClientId())
			       .withUserPoolId(config.getCognito().getUserPoolId())
			       .withAuthParameters(authParams);
			       
			   AdminInitiateAuthResult result = provider.adminInitiateAuth(authRequest);
			   AuthenticationResultType authenticationResult = result.getAuthenticationResult();
			   
			   LOGGER.info("INTRALE: Post Autenticacion ");
			   
			   if(NEW_PASSWORD_REQUIRED.equals(result.getChallengeName())){
				   LOGGER.info("INTRALE: NEW PASSWORD REQUIRED ");
				   // La autenticacion solicita una nueva password
				   if (StringUtils.isEmpty(request.getNewPassword())) {
					   LOGGER.info("INTRALE: NEW PASSWORD IS EMPTY ");
					   throw new NewPasswordRequiredException(new Error(NEW_PASSWORD_REQUIRED, NEW_PASSWORD_REQUIRED), mapper);
				   } else {
					   LOGGER.info("INTRALE: NEW PASSWORD DETECTED "); 
					   final Map<String, String> challengeResponses = new HashMap();
				       challengeResponses.put(USERNAME_PARAM, request.getEmail());
				       challengeResponses.put(PASS_WORD_PARAM, request.getPassword());
				       //add the new password to the params map
				       challengeResponses.put(NEW_PASSWORD_PARAM, request.getNewPassword());
				       //populate the challenge response
				        final AdminRespondToAuthChallengeRequest requestChallenge = new AdminRespondToAuthChallengeRequest();
				        requestChallenge.withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
				          .withChallengeResponses(challengeResponses)
				          .withClientId(config.getCognito().getClientId())
					      .withUserPoolId(config.getCognito().getUserPoolId())
				          .withSession(result.getSession());
				 
				      AdminRespondToAuthChallengeResult responseChallenge = provider.adminRespondToAuthChallenge(requestChallenge);
				      authenticationResult = responseChallenge.getAuthenticationResult();
				      
				      AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = new AdminUpdateUserAttributesRequest()
				        .withUserPoolId(config.getCognito().getUserPoolId())
						.withUsername(request.getEmail());
				      
				      if (!StringUtils.isEmpty(request.getName())) {
				    	  adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(NAME).withValue(request.getName()));
			      	  }
				      if (!StringUtils.isEmpty(request.getFamilyName())) {
				    	  adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(FAMILY_NAME).withValue(request.getFamilyName()));
				      }
				      
				      adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(EMAIL_VERIFIED_ATTRIBUTE).withValue(FunctionConst.TRUE));
				      
				      provider.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
				      
				   }
			   }
			
			   LOGGER.info("INTRALE: LOGIN FINALIZING ");
			   
			   response.setIdToken(authenticationResult.getIdToken());
			   response.setAccessToken(authenticationResult.getAccessToken());
			   response.setRefreshToken(authenticationResult.getRefreshToken());
			} else {
				throw new UnauthorizeExeption(new Error(FunctionConst.UNAUTHORIZED, NOT_LINKED_WITH_BUSINESS_MESSAGE + request.getHeaders().get(FunctionBuilder.HEADER_BUSINESS_NAME)), mapper);
			}
		   LOGGER.info("finalizando handler");
	       return response;
	   } catch (NotAuthorizedException e) {
			throw new UnauthorizeExeption(new Error(FunctionConst.UNAUTHORIZED, FunctionConst.UNAUTHORIZED), mapper);
	   }
	}

	
}
