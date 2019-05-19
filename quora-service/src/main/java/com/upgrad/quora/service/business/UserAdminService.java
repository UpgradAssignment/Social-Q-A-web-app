package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import com.upgrad.quora.service.type.ActionType;
import com.upgrad.quora.service.type.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminService {

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Autowired
    private UserDao userDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity createUser(final UserEntity userEntity) throws SignUpRestrictedException {
        UserEntity userNameEntity = userDao.checkUserName(userEntity.getUsername());
        UserEntity emailEntity = userDao.checkEmailid(userEntity.getEmail());
        // Go for user creation only if username are unique
        if (userNameEntity != null && userEntity.getUsername().equals(userNameEntity.getUsername())) {
            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        }

        // Go for user creation only if email id are unique
        if (emailEntity != null && userEntity.getEmail().equals(emailEntity.getEmail())) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        }

        String[] encryptedText = cryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        return userDao.createUser(userEntity);
    }

    //get user details
    public UserEntity getUser(String userUuid, String authorizationToken, boolean admin) throws UserNotFoundException, AuthorizationFailedException {
        UserAuthEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);

        if (admin) {
            //If role of user is admin then check if the requesting user has admin role.If not then throw exception.
            if (userAuthTokenEntity.getUser().getRole().equals(RoleType.nonadmin.toString())) {
                throw new AuthorizationFailedException("ATHR-003", "Unauthorized Access, Entered user is not an admin");
            }
        }

        //if no auth token throw exception
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        //if user has signed out throw exception
        if (userAuthTokenEntity.getLogoutAt() != null) {
            if (userAuthTokenEntity.getUser().getRole().equals(RoleType.admin.toString())) {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.");
            } else {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
            }
        }
        UserEntity userEntity = userDao.getUser(userUuid);
        if (userEntity == null) {
            throw new UserNotFoundException("USR-001", "User with entered uuid does not exist");
        } else {
            return userEntity;
        }
    }



    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteUser(String userUuid) throws UserNotFoundException {

        UserEntity user = userDao.getUser(userUuid);
        if (user == null) {
            throw new UserNotFoundException("USR-001", "User with entered uuid to be deleted does not exist");
        } else {
            userDao.deleteUser(user);
        }
    }


    public UserAuthEntity getUserByAccessToken(String authorizationToken, ActionType actionType) throws AuthorizationFailedException {
        UserAuthEntity userAuthTokenEntity = userDao.checkAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if (userAuthTokenEntity.getLogoutAt() != null) {
            if (actionType.equals(ActionType.CREATE_QUESTION)) {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");
            } else if (actionType.equals(ActionType.ALL_QUESTION)) {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get all questions");
            } else if (actionType.equals(ActionType.EDIT_QUESTION)) {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit the question");
            } else if (actionType.equals(ActionType.DELETE_QUESTION)) {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete a question");
            } else if (actionType.equals(ActionType.ALL_QUESTION_FOR_USER)) {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get all questions posted by a specific user");
            } else if (actionType.equals(ActionType.CREATE_ANSWER)) {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post an answer");
            } else if (actionType.equals(ActionType.EDIT_ANSWER)) {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit an answer");
            } else if (actionType.equals(ActionType.DELETE_ANSWER)) {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete an answer");
            } else if (actionType.equals(ActionType.GET_ALL_ANSWER_TO_QUESTION)) {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get the answers");
            }
        }
        return userAuthTokenEntity;
    }


}
