package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommonBussinessService {
    @Autowired
    UserDao userDao;

    /**
     * This method helps to fetch details of any user
     *
     * @param userId uuid of the user
     * @param accessToken access token of the signed in user
     *
     * @return user details of the user provided
     * @throws AuthorizationFailedException if user is not signed-in or user is signed out
     * @throws UserNotFoundException if user does not exist
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity UserProfileEntity(final String userId, final  String accessToken) throws AuthorizationFailedException, UserNotFoundException {
        final UserAuthEntity userAuthEntity = userDao.getUserByAccessToken(accessToken);

        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        final UserEntity userEntity = userAuthEntity.getUser();

        if (userAuthEntity != null && userAuthEntity.getLogout_at() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
        }

        if (!userEntity.getUuid().equals(userId)) {
            throw new UserNotFoundException("USR-001", "User with entered uuid does not exist");
        }

        return userEntity;
    }
}