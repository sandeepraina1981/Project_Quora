package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SigninResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
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
    @Autowired
    private UserBusinessService userBusinessService;

    /**
     * This api endpoint is used to signup a new user
     *
     * @param signupUserRequest - user details for signup new user in SignupUserRequest model
     *
     * @return JSON response with user uuid and message
     *
     * @throws SignUpRestrictedException if username or email id already exists
     */
    @RequestMapping(method = RequestMethod.POST, path = "/user/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> signUp(final SignupUserRequest signupUserRequest) throws SignUpRestrictedException, SignUpRestrictedException {
        final UserEntity userEntity = new UserEntity();

        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstname(signupUserRequest.getFirstName());
        userEntity.setLastname(signupUserRequest.getLastName());
        userEntity.setUsername(signupUserRequest.getUserName());
        userEntity.setEmail(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setSalt("1234abc");
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setAboutme(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setRole("nonadmin");
        userEntity.setContactnumber(signupUserRequest.getContactNumber());

        final UserEntity createdUserEntity = userBusinessService.signUp(userEntity);
        SignupUserResponse userResponse = new SignupUserResponse().id(createdUserEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);
    }

    /**
     * This api endpoint is used to signout the user
     *
     * @RequestHeader accessToken - access token of the signed in user in authorization request header
     *
     * @return JSON response with user uuid and message
     *
     * @throws SignOutRestrictedException if user is not signed in
     */
    @RequestMapping(method = RequestMethod.POST, path = "/user/signout", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signOut(@RequestHeader("authorization") final String accessToken) throws SignOutRestrictedException, SignOutRestrictedException {
        final UserAuthEntity userAuthEntity = userBusinessService.signOut(accessToken);
        final UserEntity userEntity = userAuthEntity.getUser();

        SignoutResponse signoutResponse = new SignoutResponse().id(userEntity.getUuid())
                .message("SIGNED OUT SUCCESSFULLY");
        return new ResponseEntity<SignoutResponse>(signoutResponse, HttpStatus.OK);
    }

    /**
     * This api endpoint is used to signin the user
     *
     * @RequestHeader accessToken - user credentials as a part of Basic authentication and to pass as "Basic username:password" (where username:password of the String is encoded to Base64 format)
     *
     * @return JSON response with user uuid and message
     *
     * @throws AuthenticationFailedException if username does not exist or password provided does not match
     * */
    @RequestMapping(method = RequestMethod.POST, path = "/user/signin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signIn(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException, AuthenticationFailedException {
        String[] decodeAuthorization = authorization.split("Basic ");

        byte[] decode = Base64.getDecoder().decode(decodeAuthorization[1]);
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");

        final UserAuthEntity userAuthToken = userBusinessService.signIn(decodedArray[0], decodedArray[1]);
        final UserEntity user = userAuthToken.getUser();

        SigninResponse authorizedUserResponse = new SigninResponse().id(user.getUuid())
                .message("SIGNED IN SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", userAuthToken.getAccessToken());
        return new ResponseEntity<SigninResponse>(authorizedUserResponse, headers, HttpStatus.OK);
    }
}