package com.example.user_service.controllers;

import com.example.user_service.dtos.NewUser;
import com.example.user_service.dtos.UpdateUser;
import com.example.user_service.dtos.UserDTO;
import com.example.user_service.exceptions.*;
import com.example.user_service.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Gets the user data with the id", description = "Receives an id and returns all the data of the specified user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully received."),
            //@ApiResponse(responseCode = "403", description = "Forbidden access to another users data."),
            @ApiResponse(responseCode = "404", description = "Bad request, invalid id.")
    })
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

    @GetMapping("/email/{id}")
    @Operation(summary = "Gets the user email with the id", description = "Receives an id and returns the specified user email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully received."),
            //@ApiResponse(responseCode = "403", description = "Forbidden access to another users data."),
            @ApiResponse(responseCode = "404", description = "Bad request, invalid id.")
    })
    public ResponseEntity<String> getUserEmail(@PathVariable Long id) throws NoUsersFoundException {
        try {
            String userEmail = userService.getEmailById(id);
            return ResponseEntity.ok(userEmail);

        } catch (NoUsersFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while searching the user email, try again later.", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @GetMapping("/users")
    @Operation(summary = "Gets all the user data in the db.", description = "Returns all the data of all the users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully received."),
            //@ApiResponse(responseCode = "403", description = "Forbidden access to another users data."),
            @ApiResponse(responseCode = "404", description = "Bad request there are no users.")
    })
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
    @Operation(summary = "Gets all the user roles.", description = "Returns all the different roles in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully received."),
            //@ApiResponse(responseCode = "403", description = "Forbidden access to another users data."),
            @ApiResponse(responseCode = "400", description = "Bad request.")
    })
    public ResponseEntity<?> getAllRoles() {
        return ResponseEntity.ok(userService.getAllRoles());
    }


    @PostMapping("/users")
    @Operation(summary = "Creates a user in the db", description = "Receives an email, username password, role and creates an.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully created."),
            //@ApiResponse(responseCode = "403", description = "Forbidden access to another users data."),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid data.")
    })
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
    @Operation(summary = "Updates an user data with the id", description = "Updates the user data independently or all at once.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully updated."),
            //@ApiResponse(responseCode = "403", description = "Forbidden access to another users data."),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid id.")
    })
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
