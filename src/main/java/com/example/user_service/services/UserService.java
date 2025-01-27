package com.example.user_service.services;

import com.example.user_service.dtos.NewUser;
import com.example.user_service.dtos.UpdateUser;
import com.example.user_service.dtos.UserDTO;
import com.example.user_service.exceptions.NoUsersFoundException;
import com.example.user_service.models.RoleType;
import com.example.user_service.models.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    UserEntity getUserById (Long id) throws NoUsersFoundException;
    UserDTO getUserDTOById (Long id) throws NoUsersFoundException;
    List<UserDTO> getAllUsers() throws NoUsersFoundException;

    String getEmailById(Long id) throws NoUsersFoundException;

    void createNewUser(NewUser newUser) throws Exception;
    UserEntity saveUser(UserEntity user);

    UserDTO updateUserById(UpdateUser updatedUser, Long id) throws Exception;

    List<RoleType> getAllRoles();
}
