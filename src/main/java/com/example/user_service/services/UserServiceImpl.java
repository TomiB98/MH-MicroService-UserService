package com.example.user_service.services;

import com.example.user_service.dtos.*;
import com.example.user_service.exceptions.*;
import com.example.user_service.models.RoleType;
import com.example.user_service.models.UserEntity;
import com.example.user_service.rabbitmq.RabbitMQProducer2;
import com.example.user_service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.example.user_service.utils.ValidationUtils.EMAIL_PATTERN;
import static com.example.user_service.utils.ValidationUtils.PASSWORD_PATTERN;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RabbitMQProducer2 rabbitMQProducer2;

    @Override
    public UserEntity getUserById(Long id) throws NoUsersFoundException {
        return userRepository.findById(id).orElseThrow( () -> new NoUsersFoundException("User with ID " + id + " not found."));
    }


    @Override
    public UserEntity getUserByEmail(String email) throws NoUsersFoundException {
        return userRepository.findByEmail(email).orElseThrow( () -> new NoUsersFoundException("User with Email " + email + " not found."));
    }


    @Override
    public String validateEmailExist(String email) {
        return userRepository.findByEmail(email)
                .map(UserEntity::getEmail)
                .orElse(null);
    }


    @Override
    public UserDTO getUserDTOById(Long id) throws NoUsersFoundException {
        return new UserDTO(getUserById(id));
    }


    @Override
    public String getEmailById(Long id) throws NoUsersFoundException {
        UserEntity user = userRepository.findById(id).orElseThrow( () -> new NoUsersFoundException("User with ID " + id + " not found."));
        return user.getEmail();
    }


    @Override
    public UserAllDataDTO getUserDTOByIdWithRole(Long id) throws NoUsersFoundException {
        return new UserAllDataDTO(getUserById(id));
    }


    @Override
    public List<UserAllDataDTO> getAllUsers() throws NoUsersFoundException {

        List<UserAllDataDTO> users = userRepository.findAll().stream()
                .map(UserAllDataDTO::new)
                .collect(Collectors.toList());

        if (users.isEmpty()) {
            throw new NoUsersFoundException("There are no registered users.");
        }

        return users;
    }


    @Override
    public void createNewUser(NewUser newUser) throws Exception {
        validateNewUser(newUser);
        // Transforms the role from string to enum
        RoleType role = RoleType.valueOf(newUser.role());
        // Encodes the password
        String encodedPassword = passwordEncoder.encode(newUser.password());
        // Generates a token randomly to verify the user email
        String verificationToken = UUID.randomUUID().toString();

        UserEntity user = new UserEntity(newUser.email(), newUser.username(), encodedPassword, role);
        // Sets the random token to the user
        user.setVerificationToken(verificationToken);

        VerificationEmailDTO verificationEmailDTO = new VerificationEmailDTO(newUser.email(), verificationToken);
        saveUser(user);
        rabbitMQProducer2.sendVerificationEmail(verificationEmailDTO);
        //rabbitMQProducer2.sendWelcomeEmail(newUser.email());
    }


    @Override
    public UserEntity findByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token);
    }


    @Override
    public void welcomeEmail (String email) {
        rabbitMQProducer2.sendWelcomeEmail(email);
    }


    @Override
    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }


    @Override
    public UserDTO updateUserById(UpdateUser updatedUser, Long id) throws Exception {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new NoUsersFoundException("User with ID " + id + " not found."));

        validateAllBlanks(updatedUser.username(), updatedUser.password());

        if (!updatedUser.username().isBlank()) {
            user.setUsername(updatedUser.username());
        }

        if (!updatedUser.password().isBlank()) {
            validateUpdatedUser(updatedUser, user);
            user.setPassword(passwordEncoder.encode(updatedUser.password()));
        }

        userRepository.save(user);
        return new UserDTO(user);
    }


    @Override
    public UserDTO updateUserRoleVerifiedById(UpdateUserRoleOrVerified updateUserRoleOrVerified, Long id) throws Exception {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new NoUsersFoundException("User with ID " + id + " not found."));

        if (!updateUserRoleOrVerified.role().isBlank()) {
            validateIfRoleIsCorrect(updateUserRoleOrVerified.role());
            RoleType role = RoleType.valueOf(updateUserRoleOrVerified.role());
            user.setRole(role);
        }

        if(!updateUserRoleOrVerified.isVerified().isBlank()) {
            validateIfVerifiedIsCorrect(updateUserRoleOrVerified.isVerified());
            if(updateUserRoleOrVerified.isVerified().equals("true")) {
                user.setVerified(true);
            }
            if(updateUserRoleOrVerified.isVerified().equals("false")) {
                user.setVerified(false);
            }
        }

        userRepository.save(user);
        return new UserDTO(user);
    }


    @Override
    public List<RoleType> getAllRoles() {
        return userRepository.findAllRoles();
    }


    @Override
    public void deleteUnverifiedUsers() throws NoUsersFoundException {
        long count = userRepository.countUnverifiedUsers();
        if (count == 0) {
            throw new NoUsersFoundException("There are no unverified users.");
        }
        userRepository.deleteUnverifiedUsers();
    }


    //Validations
    public void validateUpdatedUser (UpdateUser updateUser, UserEntity user) throws Exception {
        validateEqualPassword(updateUser.password(), user.getPassword());
        validateUpdatedPassword(updateUser.password());
    }

    public static void validateAllBlanks (String username, String password) throws UserNameException {
        if (username.isBlank() && password.isBlank()) {
            throw new UserNameException("At least one value has to be modified.");
        }
    }

    public static void validateUpdatedPassword (String password) throws PasswordException {
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        if(!matcher.matches()) {
            throw new PasswordException("Password must have at lest: one digit, a lower and upper case letter, a special character, 8 characters and no whitespace.");
        }
    }

    public void validateEqualPassword (String updatedPassword, String password) throws PasswordException {
        if (passwordEncoder.matches(updatedPassword, password)) {
            throw new PasswordException("New password must be different to the old one.");
        }
    }

    public void validateNewUser (NewUser newUser) throws Exception {
        validateUserName(newUser.username());
        validatePassword(newUser.password());
        validateUserEmail(newUser.email());
        validateIfEmailExist(newUser.email());
        validateIfRoleIsCorrect(newUser.role());
    }

    public static void validateUserName (String username) throws UserNameException {
        if (username == null || username.isBlank()) {
            throw new UserNameException("Username cannot be null or blank.");
        }
    }

    public static void validatePassword (String password) throws PasswordException {
        if (password == null || password.isBlank()) {
            throw new PasswordException("Password cannot be null or blank.");
        }
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        if(!matcher.matches()) {
            throw new PasswordException("Password must have at lest: one digit, a lower and upper case letter, a special character, 8 characters and no whitespace.");
        }
    }

    public static void validateUserEmail (String email) throws EmailException {
        if (email == null || email.isBlank()) {
            throw new EmailException("Email cannot be null or blank.");
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if(!matcher.matches()) {
            throw new EmailException("Invalid email format: must have at least 8 characters and a '@'.");
        }
    }

    public void validateIfEmailExist (String email) throws EmailException {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new EmailException ("This email is already registered.");
        }
    }

    public static void validateIfRoleIsCorrect(String role) throws RoleException {
        if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw new RoleException("Role must be ADMIN or USER.");
        }
    }

    public static void validateIfVerifiedIsCorrect(String verified) throws RoleException {
        if (!verified.equals("true") && !verified.equals("false")) {
            throw new RoleException("IsVerified must be true or false.");
        }
    }
}
