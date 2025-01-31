package com.example.user_service.services;

import com.example.user_service.dtos.*;
import com.example.user_service.exceptions.*;
import com.example.user_service.models.RoleType;
import com.example.user_service.models.UserEntity;
import com.example.user_service.rabbitmq.RabbitMQProducer2;
import com.example.user_service.repositories.UserRepository;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public UserDTO getUserDTOById(Long id) throws NoUsersFoundException {
        return new UserDTO(getUserById(id));
    }


    @Override
    public UserAllDataDTO getUserDTOByIdWithRole(Long id) throws NoUsersFoundException {
        return new UserAllDataDTO(getUserById(id));
    }


    @Override
    public String getEmailById(Long id) throws NoUsersFoundException {
        UserEntity user = userRepository.findById(id).orElseThrow( () -> new NoUsersFoundException("User with ID " + id + " not found."));
        return user.getEmail();
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
        RoleType role = RoleType.valueOf(newUser.role());
        UserEntity user = new UserEntity(newUser.email(), newUser.username(), passwordEncoder.encode(newUser.password()), role);
        saveUser(user);
        rabbitMQProducer2.sendWelcomeEmail(newUser.email());
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
    public UserDTO updateUserRoleById(UpdateUserRole updatedUserRole, Long id) throws Exception {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(()-> new NoUsersFoundException("User with ID " + id + " not found."));

        if (!updatedUserRole.role().isBlank()) {
            validateIfRoleIsCorrect(updatedUserRole.role());
            RoleType role = RoleType.valueOf(updatedUserRole.role());
            user.setRole(role);
        }

        userRepository.save(user);
        return new UserDTO(user);
    }


    @Override
    public List<RoleType> getAllRoles() {
        return userRepository.findAllRoles();
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
}
