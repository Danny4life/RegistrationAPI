package com.osiki.springsecuritydemoone.controller;

import com.osiki.springsecuritydemoone.entity.User;
import com.osiki.springsecuritydemoone.entity.VerificationToken;
import com.osiki.springsecuritydemoone.event.RegistrationCompleteEvent;
import com.osiki.springsecuritydemoone.model.LoginModel;
import com.osiki.springsecuritydemoone.model.PasswordModel;
import com.osiki.springsecuritydemoone.model.UserModel;
import com.osiki.springsecuritydemoone.response.LoginResponse;
import com.osiki.springsecuritydemoone.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request){

        User user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(
                user,
                applicationUrl(request)
        ));

        return "Success";

    }

    @PostMapping("/login")

    public ResponseEntity<?> loginUser(@RequestBody LoginModel loginModel){

        LoginResponse loginResponse = userService.loginUser(loginModel);

        return ResponseEntity.ok(loginResponse);

    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token){

        String result = userService.validateVerificationToken(token);

        if(result.equalsIgnoreCase("valid")){
            return "User verifies successfully";
        }

        return "Bad user";

    }

    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken,
                                          HttpServletRequest request ){

        VerificationToken verificationToken =
                userService.generateNewVerificationToken(oldToken);

        User user = verificationToken.getUser();
        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);

        return "Verification link sent";

    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request){

        User user = userService.findUserByEmail(passwordModel.getEmail());

        String url = "";

        if(user != null){
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            url = passwordResetTokenMail(user, applicationUrl(request), token);
        }

        return url;

    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token, @RequestBody PasswordModel passwordModel){

        String result = userService.validatePasswordResetToken(token);

        if(!result.equalsIgnoreCase("valid")){
            return "Invalid token";
        }

        Optional<User> user = userService.getUserByPasswordResetToken(token);

        if(user.isPresent()){
            userService.changePassword(user.get(), passwordModel.getNewPassword());
            return "Password Reset Successful";
        } else {
            return "Invalid token";
        }

    }


    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel){
        User user = userService.findUserByEmail(passwordModel.getEmail());

        if(!userService.checkIfValidOldPassword(user, passwordModel.getOldPassword())){
            return "Invalid old password";
        }

        //save new password

        userService.changePassword(user, passwordModel.getNewPassword());
        return "Password changed successfully";

    }

    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url =
                applicationUrl
                        + "/savePassword?token="
                        + token;

        //sendVerificationEmail()
        log.info("Click the link to reset your password: {}",
                url);

        return url;
    }

    private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
        String url =
                applicationUrl
                        + "/verifyRegistration?token="
                        + verificationToken.getToken();

        //sendVerificationEmail()
        log.info("Click the link to verify your account: {}",
                url);
    }

    private String applicationUrl(HttpServletRequest request) {

        return "http://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                request.getContextPath();

    }
}
