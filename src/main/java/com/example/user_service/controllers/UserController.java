package com.example.user_service.controllers;

import com.example.user_service.dtos.NewUser;
import com.example.user_service.dtos.UpdateUser;
import com.example.user_service.dtos.UserDTO;
import com.example.user_service.exceptions.*;
import com.example.user_service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public ResponseEntity<String> invalidPath() {
        return ResponseEntity.badRequest().body("The url provided is invalid.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) throws NoUsersFoundException {

        try {
            UserDTO userDTO = userService.getUserDTOById(id);
            return ResponseEntity.ok(userDTO);

        } catch (NoUsersFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while searching the users data, try again later.", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }


    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() throws NoUsersFoundException {
        try {
            return ResponseEntity.ok(userService.getAllUsers());

        } catch (NoUsersFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while searching the users data, try again later.", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }


    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        return ResponseEntity.ok(userService.getAllRoles());
    }


    @PostMapping("/users")
    public ResponseEntity<?> createNewUser(@RequestBody NewUser newUser) throws Exception {

        try {
            userService.createNewUser(newUser);
            return new ResponseEntity<>("User crated succesfully", HttpStatus.CREATED);

        } catch (UserNameException | PasswordException | EmailException | RoleException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while creating the user, try again later.", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }


    @PutMapping("update/{id}")
    public ResponseEntity<?> updateUserById(@RequestBody UpdateUser updateUser, @PathVariable Long id) throws Exception {

        try {
            UserDTO updatedUser = userService.updateUserById(updateUser, id);
            return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);

        } catch (NoUsersFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (UserNameException | PasswordException | EmailException | RoleException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while updating the user, try again later.", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
}
