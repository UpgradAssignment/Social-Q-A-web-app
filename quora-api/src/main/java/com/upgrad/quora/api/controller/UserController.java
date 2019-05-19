package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.SigninResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.SignUpBusinessService;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import com.upgrad.quora.service.type.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.UUID;


@RestController
@RequestMapping("/")
public class UserController {

    //Constant for messages
    private static final String USER_SUCCESSFULLY_REGISTERED = "USER SUCCESSFULLY REGISTERED";
    private static final String SIGNED_OUT_SUCCESSFULLY = "SIGNED OUT SUCCESSFULLY";

    @Autowired
    private SignUpBusinessService signUpBusinessService;

    @Autowired
    private AuthenticationService authenticationService;

// User registration Endpoint
    @RequestMapping(method = RequestMethod.POST, path = "/user/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> signUp(final SignupUserRequest signupUserRequest) throws SignUpRestrictedException {
        UserEntity userEntity = new UserEntity();
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setUsername(signupUserRequest.getUserName());
        userEntity.setEmail(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setAboutMe(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setContactNumber(signupUserRequest.getContactNumber());
        userEntity.setRole(RoleType.getEnum(1).toString());
        UserEntity createdUserEntity = signUpBusinessService.signUp(userEntity);
        //Here since the json provided does not have message variable, hence we cannot show message in the response
        SignupUserResponse userResponse = new SignupUserResponse().id(createdUserEntity.getUuid()).status(USER_SUCCESSFULLY_REGISTERED);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }


    // User Log in Endpoint
    @RequestMapping(method = RequestMethod.POST,path = "/user/signin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signIn(@RequestHeader("authorization") final String authorization) throws
            AuthenticationFailedException{

        byte[] decodeAuth = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
        String decodedAuth = new String(decodeAuth);
        String[] decodedAuthArray = decodedAuth.split(":");

        UserAuthEntity userAuthToken = authenticationService.authenticate(decodedAuthArray[0],
                decodedAuthArray[1]);

        UserEntity userEntity = userAuthToken.getUser();

        SigninResponse signinResponse = new SigninResponse().id(userEntity.getUuid())
                .message("SIGNED IN SUCCESSFULLY");

        HttpHeaders headers = new HttpHeaders();
        headers.add("access_token", userAuthToken.getAccessToken());

        return new ResponseEntity<SigninResponse>(signinResponse, headers, HttpStatus.OK);
    }


//Enpoint for Signout
    @RequestMapping(method = RequestMethod.POST, path = "user/signout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signout(@RequestHeader final String accessToken) throws SignOutRestrictedException {
        UserEntity userEntity = authenticationService.authenticateAccessToken(accessToken);
        SignoutResponse signOutResponse = new SignoutResponse().id(userEntity.getUuid()).message(SIGNED_OUT_SUCCESSFULLY);
        return new ResponseEntity<>(signOutResponse, HttpStatus.OK);
    }


}
