package com.example.user_service.dtos;

import com.example.user_service.models.UserEntity;

public class UserDTO {

    private Long id;
    private String username, email;
//    private List<>;


    public UserDTO(UserEntity user) {
        id = user.getId();
        username = user.getUsername();
        email = user.getEmail();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
