package com.osiki.springsecuritydemoone.service;

import com.osiki.springsecuritydemoone.entity.User;
import com.osiki.springsecuritydemoone.entity.VerificationToken;
import com.osiki.springsecuritydemoone.model.LoginModel;
import com.osiki.springsecuritydemoone.model.UserModel;
import com.osiki.springsecuritydemoone.response.LoginResponse;

import java.util.Optional;

public interface UserService {
    User registerUser(UserModel userModel);

    void saveVerificationTokenForUser(String token, User user);

    String validateVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    User findUserByEmail(String email);

    void createPasswordResetTokenForUser(User user, String token);

    String validatePasswordResetToken(String token);

    Optional<User> getUserByPasswordResetToken(String token);

    void changePassword(User user, String newPassword);

    boolean checkIfValidOldPassword(User user, String oldPassword);

    LoginResponse loginUser(LoginModel loginModel);
}
