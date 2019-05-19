package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.UserAdminService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/")
public class AdminController {


    private static final String USER_SUCCESSFULLY_DELETED = "USER SUCCESSFULLY DELETED";

    private final
    UserAdminService userAdminService;

    @Autowired
    public AdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/admin/user/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDeleteResponse> userDelete(@PathVariable("userId") final String userUuid, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UserNotFoundException {
        //Check user to be deleted
        final UserEntity userEntity = userAdminService.getUser(userUuid, authorization, true);
        userAdminService.deleteUser(userUuid);

        UserDeleteResponse userDeleteResponse = new UserDeleteResponse().id(userEntity.getUuid()).status(USER_SUCCESSFULLY_DELETED);
        return new ResponseEntity<>(userDeleteResponse, HttpStatus.OK);
    }


}
