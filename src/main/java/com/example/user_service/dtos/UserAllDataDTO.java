package com.example.user_service.dtos;

import com.example.user_service.models.RoleType;
import com.example.user_service.models.UserEntity;

public class UserAllDataDTO {

    private Long id;
    private String username, email;
    private RoleType role;
//    private List<>;


    public UserAllDataDTO(UserEntity user) {
        id = user.getId();
        username = user.getUsername();
        email = user.getEmail();
        role = user.getRole();
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

    public RoleType getRole() {
        return role;
    }
}
