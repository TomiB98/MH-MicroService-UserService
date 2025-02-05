package com.example.user_service.controllers;

import com.example.user_service.dtos.UpdateUser;
import com.example.user_service.dtos.UpdateUserRoleOrVerified;
import com.example.user_service.dtos.UserAllDataDTO;
import com.example.user_service.dtos.UserDTO;
import com.example.user_service.exceptions.*;
import com.example.user_service.services.TokenDataServiceImpl;
import com.example.user_service.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenDataServiceImpl tokenDataService;

    @GetMapping("/")
    public ResponseEntity<String> invalidPath() {
        return ResponseEntity.badRequest().body("The url provided is invalid.");
    }


    @GetMapping("/{id}")
    @Operation(summary = "Gets the user data with the id", description = "Receives an id and returns all the data of the specified user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully received."),
            @ApiResponse(responseCode = "403", description = "Forbidden access to another users data."),
            @ApiResponse(responseCode = "404", description = "Bad request, invalid id.")
    })
    public ResponseEntity<?> getUserById(@PathVariable Long id, HttpServletRequest request) throws NoUsersFoundException {

        try {
            String authenticatedUserRole = tokenDataService.getRole(request);

            if (authenticatedUserRole.equals("ADMIN")) {
                UserAllDataDTO userAllDataDTO = userService.getUserDTOByIdWithRole(id);
                return ResponseEntity.ok(userAllDataDTO);

            } else return new ResponseEntity<>("Forbidden: You cannot access another user's data.", HttpStatus.FORBIDDEN);

        } catch (NoUsersFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while searching the users data, try again later.", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }


    @GetMapping("/info")
    @Operation(summary = "Gets the user logged data with the id", description = "Receives an id and returns all the data of the specified user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully received."),
            @ApiResponse(responseCode = "404", description = "Bad request, invalid id.")
    })
    public ResponseEntity<?> getLoggedUserById(HttpServletRequest request) throws NoUsersFoundException {

        try {
            Long authenticatedUserId = tokenDataService.getId(request);

            UserDTO userDTO = userService.getUserDTOById(authenticatedUserId);
            return ResponseEntity.ok(userDTO);

        } catch (NoUsersFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while searching the users data, try again later.", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }


//    @GetMapping("/email/{id}")
//    @Operation(summary = "Gets the user email with the id", description = "Receives an id and returns the specified user email.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Data successfully received."),
//            //@ApiResponse(responseCode = "403", description = "Forbidden access to another users data."),
//            @ApiResponse(responseCode = "404", description = "Bad request, invalid id.")
//    })
//    public ResponseEntity<String> getUserEmail(@PathVariable Long id) throws NoUsersFoundException {
//        try {
//            String userEmail = userService.getEmailById(id);
//            return ResponseEntity.ok(userEmail);
//
//        } catch (NoUsersFoundException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
//
//        } catch (Exception e) {
//            return new ResponseEntity<>("An error occurred while searching the user email, try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
//
//        }
//    }

    @GetMapping("/users")
    @Operation(summary = "Gets all the user data in the db.", description = "Returns all the data of all the users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully received."),
            @ApiResponse(responseCode = "403", description = "Forbidden access to another users data."),
            @ApiResponse(responseCode = "404", description = "Bad request there are no users.")
    })
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) throws NoUsersFoundException {
        try {
            String authenticatedUserRole = tokenDataService.getRole(request);

            if (!authenticatedUserRole.equals("ADMIN")) {
                return new ResponseEntity<>("Forbidden: You cannot access another user's data.", HttpStatus.FORBIDDEN);
            }

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
            @ApiResponse(responseCode = "403", description = "Forbidden access to this data, must be admin."),
            @ApiResponse(responseCode = "400", description = "Bad request.")
    })
    public ResponseEntity<?> getAllRoles(HttpServletRequest request) {

        String authenticatedUserRole = tokenDataService.getRole(request);

        if (!authenticatedUserRole.equals("ADMIN")) {
            return new ResponseEntity<>("Forbidden: You cannot access this data.", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(userService.getAllRoles());
    }


    @PutMapping("update")
    @Operation(summary = "Updates an user data with the id", description = "Updates the user data independently or all at once.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully updated."),
            @ApiResponse(responseCode = "403", description = "Forbidden access to update another users data."),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid id.")
    })
    public ResponseEntity<?> updateUserById(@RequestBody UpdateUser updateUser, HttpServletRequest request) throws Exception {

        try {
            Long authenticatedUserId = tokenDataService.getId(request);

//            if (!authenticatedUserId.equals(id)) {
//                return new ResponseEntity<>("Forbidden: You cannot update another user's data.", HttpStatus.FORBIDDEN);
//            }

            UserDTO updatedUser = userService.updateUserById(updateUser, authenticatedUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);

        } catch (NoUsersFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (UserNameException | PasswordException | EmailException | RoleException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while updating the user, try again later.", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }


    @PutMapping("update/roleOrVerification/{id}")
    @Operation(summary = "Updates an user data with the id", description = "Updates the user data independently or all at once.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User role successfully updated."),
            @ApiResponse(responseCode = "403", description = "Forbidden access to another users data, must be admin."),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid id.")
    })
    public ResponseEntity<?> updateUserRoleById(@RequestBody UpdateUserRoleOrVerified updateUserRoleOrVerified, @PathVariable Long id, HttpServletRequest request) throws Exception {

        try {
            String authenticatedUserRole = tokenDataService.getRole(request);

            if (!authenticatedUserRole.equals("ADMIN")) {
                return new ResponseEntity<>("Forbidden: You cannot access this data.", HttpStatus.FORBIDDEN);
            }

            userService.updateUserRoleVerifiedById(updateUserRoleOrVerified, id);
            String userEmail = userService.getEmailById(id);
            return ResponseEntity.status(HttpStatus.CREATED).body("User successfully updated with email: " + userEmail + " - and id: " + id + ".");

        } catch (NoUsersFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

        } catch ( RoleException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while updating the user role, try again later.", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
}


//    @PostMapping("/users")
//    @Operation(summary = "Creates a user in the db", description = "Receives an email, username password, role and creates an.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "User successfully created."),
//            //@ApiResponse(responseCode = "403", description = "Forbidden access to another users data."),
//            @ApiResponse(responseCode = "400", description = "Bad request, invalid data.")
//    })
//    public ResponseEntity<?> createNewUser(@RequestBody NewUser newUser) throws Exception {
//
//        try {
//            userService.createNewUser(newUser);
//            return new ResponseEntity<>("User crated succesfully", HttpStatus.CREATED);
//
//        } catch (UserNameException | PasswordException | EmailException | RoleException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//
//        } catch (Exception e) {
//            return new ResponseEntity<>("An error occurred while creating the user, try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
//
//        }
//    }
