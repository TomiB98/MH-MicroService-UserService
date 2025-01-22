package com.example.user_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class ExceptionHandlers {

    @ExceptionHandler(UserNameException.class)
    public ResponseEntity<String> userExceptionHandler(UserNameException userNameException){
        return new ResponseEntity<>(userNameException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<String> userExceptionHandler(PasswordException passwordException){
        return new ResponseEntity<>(passwordException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<String> userExceptionHandler(EmailException emailException){
        return new ResponseEntity<>(emailException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RoleException.class)
    public ResponseEntity<String> userExceptionHandler(RoleException roleException){
        return new ResponseEntity<>(roleException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoUsersFoundException.class)
    public ResponseEntity<String> userExceptionHandler(NoUsersFoundException noUsersFoundException){
        return new ResponseEntity<>(noUsersFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }
}
