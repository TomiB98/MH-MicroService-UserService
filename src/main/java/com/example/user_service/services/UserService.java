package com.example.user_service.services;

import com.example.user_service.dtos.*;
import com.example.user_service.exceptions.NoUsersFoundException;
import com.example.user_service.models.RoleType;
import com.example.user_service.models.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    UserEntity getUserById (Long id) throws NoUsersFoundException;
    UserEntity getUserByEmail(String email) throws NoUsersFoundException;
    UserDTO getUserDTOById (Long id) throws NoUsersFoundException;
    String getEmailById(Long id) throws NoUsersFoundException;
    String validateEmailExist(String email);
    UserAllDataDTO getUserDTOByIdWithRole(Long id) throws NoUsersFoundException;
    List<UserAllDataDTO> getAllUsers() throws NoUsersFoundException;

    void createNewUser(NewUser newUser) throws Exception;
    UserEntity findByVerificationToken(String token);
    void welcomeEmail (String email);
    UserEntity saveUser(UserEntity user);

    UserDTO updateUserById(UpdateUser updatedUser, Long id) throws Exception;
    UserDTO updateUserRoleById(UpdateUserRole updatedUserRole, Long id) throws Exception;

    List<RoleType> getAllRoles();
}
