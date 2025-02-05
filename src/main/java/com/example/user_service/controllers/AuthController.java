package com.example.user_service.controllers;

import com.example.user_service.config.JwtUtils;
import com.example.user_service.dtos.LoginUser;
import com.example.user_service.dtos.NewUser;
import com.example.user_service.exceptions.NoUsersFoundException;
import com.example.user_service.models.UserEntity;
import com.example.user_service.repositories.UserRepository;
import com.example.user_service.services.UserService;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;

import static com.example.user_service.utils.ValidationUtils.EMAIL_PATTERN;
import static com.example.user_service.utils.ValidationUtils.PASSWORD_PATTERN;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    UserService userService;


    @PostMapping("/login")
    public ResponseEntity<String> authenticateUser(@RequestBody LoginUser loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserEntity user = userService.getUserByEmail(authentication.getName());
            assert user != null;

            if (!user.isVerified()) {
                return new ResponseEntity<>("Your email is not verified. Check your inbox.", HttpStatus.UNAUTHORIZED);
            }

            String jwt = jwtUtil.createToken(authentication.getName(), user.getId(), user.getRole());
            return ResponseEntity.ok(jwt);

        } catch (Exception ex) {
            return  new ResponseEntity<>("Password or email invalid.", HttpStatus.UNAUTHORIZED);

        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody NewUser newUser) {

        try {

            validateNewUser(newUser);

            userService.createNewUser(newUser);
            return new ResponseEntity<>("User registered successfully, check your inbox to validate your email.", HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        }
    }


    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        UserEntity user = userService.findByVerificationToken(token);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        userService.saveUser(user);
        userService.welcomeEmail(user.getEmail());

        return ResponseEntity.ok("Email verified successfully! You can now log in!");
    }


    // Validations
    public void validateNewUser (NewUser newUser) throws Exception {
        validateUserName(newUser.username());
        validatePassword(newUser.password());
        validateUserEmail(newUser.email());
        validateIfEmailExist(newUser.email());
        validateIfRoleIsCorrect(newUser.role());
    }

    public static void validateUserName (String username) throws ValidationException {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username cannot be null or blank.");
        }
    }

    public static void validatePassword (String password) throws ValidationException {
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password cannot be null or blank.");
        }
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        if(!matcher.matches()) {
            throw new ValidationException("Password must have at lest: one digit, a lower and upper case letter, a special character, 8 characters and no whitespace.");
        }
    }

    public static void validateUserEmail (String email) throws ValidationException {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email cannot be null or blank.");
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if(!matcher.matches()) {
            throw new ValidationException("Invalid email format: must have at least 8 characters and a '@'.");
        }
    }

    public void validateIfEmailExist (String email) {
        if (userService.validateEmailExist(email) != null) {
            throw new ValidationException ("This email is already registered.");
        }
    }

    public static void validateIfRoleIsCorrect(String role) {
        if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw new ValidationException("Role must be ADMIN or USER.");
        }
    }
}

//    @Autowired
//    private UserRepository userRepository;

//            UserEntity user = userRepository.findByEmail(authentication.getName()).orElse(null);

//        if(userRepository.findByEmail(email).isPresent()) {
