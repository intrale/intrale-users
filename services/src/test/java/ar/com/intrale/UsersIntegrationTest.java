package ar.com.intrale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ar.com.intrale.functions.DeleteFunction;
import ar.com.intrale.functions.PasswordRecoveryFunction;
import ar.com.intrale.functions.ReadGroupFunction;
import ar.com.intrale.functions.SignInFunction;
import ar.com.intrale.functions.SignUpFunction;
import ar.com.intrale.functions.ValidateTokenFunction;
import ar.com.intrale.messages.DeleteRequest;
import ar.com.intrale.messages.Group;
import ar.com.intrale.messages.PasswordRecoveryRequest;
import ar.com.intrale.messages.ReadGroupResponse;
import ar.com.intrale.messages.ReadUserRequest;
import ar.com.intrale.messages.ReadUserResponse;
import ar.com.intrale.messages.RequestRoot;
import ar.com.intrale.messages.SignInRequest;
import ar.com.intrale.messages.SignInResponse;
import ar.com.intrale.messages.SignUpRequest;
import ar.com.intrale.messages.SignUpResponse;
import ar.com.intrale.messages.UpdateUserRequest;
import ar.com.intrale.messages.User;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MicronautTest;


@MicronautTest(rebuildContext = true)
@Property(name = IntraleFactory.FACTORY, value = "true")
@Property(name = IntraleFactory.PROVIDER, value = "true")
public class UsersIntegrationTest extends ar.com.intrale.Test{
	
	@Inject
	private TemporaryPasswordConfig temporaryPasswordConfig;
	
	@Inject
	private CredentialsGenerator credentialGenerator;
	
	@Override
    public void beforeEach() {
    }
    
	@Override
	public void afterEach() {
	}
    
    @Test
    public void test() throws Exception {

    	APIGatewayProxyResponseEvent responseEvent = null;

		deleteUser(); 
    	
    	// Registramos un email
    	SignUpRequest signUpRequest = new SignUpRequest();
    	signUpRequest.setRequestId(DUMMY_VALUE);
    	signUpRequest.setEmail(DUMMY_EMAIL);

    	APIGatewayProxyRequestEvent requestEvent = makeRequestEvent(signUpRequest, SignUpFunction.FUNCTION_NAME);
		Map<String, String> headers = requestEvent.getHeaders();
		headers.remove(FunctionBuilder.HEADER_BUSINESS_NAME);
		responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(requestEvent);
		assertEquals(HttpStatus.NOT_FOUND.getCode(), responseEvent.getStatusCode());
		
		requestEvent = makeRequestEvent(signUpRequest, SignUpFunction.FUNCTION_NAME);
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(requestEvent);
        SignUpResponse signupResponse  = readEncodedValue(responseEvent.getBody(), SignUpResponse.class);
    	
        assertEquals(DUMMY_EMAIL.toLowerCase(), signupResponse.getEmail().toLowerCase());
        
        // Hacemos el primer Signin con el email registrado
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setRequestId(DUMMY_VALUE);
        signInRequest.setEmail(DUMMY_EMAIL);
        signInRequest.setPassword(signupResponse.getTemporaryPassword());
        signInRequest.setFamilyName(DUMMY_VALUE);
        signInRequest.setName(DUMMY_VALUE);

        // signin que luego requerira cambio de password
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
    	assertEquals(HttpStatus.UPGRADE_REQUIRED.getCode(), responseEvent.getStatusCode());
    	
    	// Cambio de password durante el signin
    	String newPassword =  credentialGenerator.generate(temporaryPasswordConfig.length);
    	signInRequest.setNewPassword(newPassword);
    	
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
        SignInResponse signinResponse  = readEncodedValue(responseEvent.getBody(), SignInResponse.class);
        
        assertNotNull(signinResponse.getAccessToken());
        assertNotNull(signinResponse.getIdToken());
        assertNotNull(signinResponse.getRefreshToken());
        
        // signin con la nueva password
        signInRequest.setPassword(newPassword);
        
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
        signinResponse  = readEncodedValue(responseEvent.getBody(), SignInResponse.class);
        
        assertNotNull(signinResponse.getAccessToken());
        assertNotNull(signinResponse.getIdToken());
        assertNotNull(signinResponse.getRefreshToken());
        
        // signin con password incorrecta
        signInRequest.setPassword(DUMMY_VALUE);

       	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
        assertEquals(HttpStatus.UNAUTHORIZED.getCode(), responseEvent.getStatusCode());
        
        // se trata de hacer signin con otro negocio
        signInRequest.setPassword(newPassword);
        requestEvent = makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME);
        requestEvent.getHeaders().put(FunctionBuilder.HEADER_BUSINESS_NAME, DUMMY_VALUE + "_OTHER2");
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(requestEvent);

    	assertEquals(HttpStatus.UNAUTHORIZED.getCode(), responseEvent.getStatusCode());
    	
        requestEvent = makeRequestEvent(signUpRequest, SignUpFunction.FUNCTION_NAME);
        requestEvent.getHeaders().put(FunctionBuilder.HEADER_BUSINESS_NAME, DUMMY_VALUE + "_OTHER");
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(requestEvent);

    	signupResponse  = readEncodedValue(responseEvent.getBody(), SignUpResponse.class);
    	
        assertEquals(DUMMY_EMAIL.toLowerCase(), signupResponse.getEmail().toLowerCase());
        
    	APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = makeRequestEvent(new RequestRoot(), ReadGroupFunction.FUNCTION_NAME);
    	apiGatewayProxyRequestEvent.getHeaders().put(FunctionBuilder.HEADER_ID_TOKEN, signinResponse.getIdToken());
    	apiGatewayProxyRequestEvent.getHeaders().put(FunctionBuilder.HEADER_AUTHORIZATION, signinResponse.getAccessToken());
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(apiGatewayProxyRequestEvent);
    	ReadGroupResponse readGroupResponse = readEncodedValue(responseEvent.getBody(), ReadGroupResponse.class);

    	assertTrue(!readGroupResponse.getGroups().isEmpty());

    	
    	List<String> groups = new ArrayList<String>();
    	readGroupResponse.getGroups().forEach(new Consumer<Group>() {

			@Override
			public void accept(Group group) {
				groups.add(group.getName());
			}
		});
    	
    	UpdateUserRequest updateUserRequest = new UpdateUserRequest();
    	updateUserRequest.setRequestId(DUMMY_VALUE);
    	updateUserRequest.setEmail(DUMMY_EMAIL);
    	updateUserRequest.setGroups(groups);
    	updateUserRequest.setFamilyName(DUMMY_VALUE);
    	updateUserRequest.setName(DUMMY_VALUE);
    	
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(makeRequestEvent(updateUserRequest, FunctionConst.UPDATE));
    	assertEquals(HttpStatus.OK.getCode(), responseEvent.getStatusCode());
    	
    	updateUserRequest.setGroups(groups.subList(1, groups.size()));
    	
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(makeRequestEvent(updateUserRequest, FunctionConst.UPDATE));
    	assertEquals(HttpStatus.OK.getCode(), responseEvent.getStatusCode());
        
    	
    	ReadUserRequest readUserRequest = new ReadUserRequest();
    	readUserRequest.setRequestId(DUMMY_VALUE);
    	
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(makeRequestEvent(readUserRequest, FunctionConst.READ));
    	ReadUserResponse readUserResponse = readEncodedValue(responseEvent.getBody(), ReadUserResponse.class);   
    
    	assertEquals(1, readUserResponse.getUsers().size());
    	
    	readUserRequest.setEmail(signInRequest.getEmail());
    	
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(makeRequestEvent(readUserRequest, FunctionConst.READ));
    	readUserResponse = readEncodedValue(responseEvent.getBody(), ReadUserResponse.class);
    	
    	assertEquals(1, readUserResponse.getUsers().size());
    	
    	User user = readUserResponse.getUsers().iterator().next();
    	assertEquals(DUMMY_VALUE, user.getName());
    	assertEquals(DUMMY_VALUE, user.getFamilyName());
    	
    	PasswordRecoveryRequest passwordRecoveryRequest = new PasswordRecoveryRequest();
    	passwordRecoveryRequest.setRequestId("001");
    	passwordRecoveryRequest.setEmail(DUMMY_EMAIL);
    	
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(makeRequestEvent(passwordRecoveryRequest, PasswordRecoveryFunction.FUNCTION_NAME));
    	
    	deleteUser();   
        
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
    	
    	assertEquals(HttpStatus.UNAUTHORIZED.getCode(), responseEvent.getStatusCode());
    	
    	RequestRoot request = new RequestRoot();
    	request.setRequestId(DUMMY_VALUE);
    	apiGatewayProxyRequestEvent = makeRequestEvent(request, ValidateTokenFunction.FUNCTION_NAME);
    	apiGatewayProxyRequestEvent.getHeaders().put(FunctionBuilder.HEADER_ID_TOKEN, signinResponse.getIdToken());
    	apiGatewayProxyRequestEvent.getHeaders().put(FunctionBuilder.HEADER_AUTHORIZATION, signinResponse.getAccessToken());
    	
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(apiGatewayProxyRequestEvent);
    	assertEquals(HttpStatus.OK.getCode(), responseEvent.getStatusCode());
    	
    }

	private void deleteUser() throws Exception, JsonProcessingException, JsonMappingException {
		APIGatewayProxyResponseEvent responseEvent;
		DeleteRequest deleteRequest = new DeleteRequest();
    	deleteRequest.setRequestId(DUMMY_VALUE);
    	deleteRequest.setEmail(DUMMY_EMAIL);
    	
    	APIGatewayProxyRequestEvent requestEvent = makeRequestEvent(deleteRequest, DeleteFunction.FUNCTION_NAME);
    	requestEvent.getHeaders().put(FunctionBuilder.HEADER_BUSINESS_NAME, DUMMY_VALUE);
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(requestEvent);

    	requestEvent = makeRequestEvent(deleteRequest, DeleteFunction.FUNCTION_NAME);
    	requestEvent.getHeaders().put(FunctionBuilder.HEADER_BUSINESS_NAME, DUMMY_VALUE + "_OTHER");
    	responseEvent = (APIGatewayProxyResponseEvent) lambda.execute(requestEvent);
	}




    
    



}
