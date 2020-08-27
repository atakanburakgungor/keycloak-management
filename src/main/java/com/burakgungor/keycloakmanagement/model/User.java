package com.burakgungor.keycloakmanagement.model;

import lombok.Data;

import java.util.List;

@Data
public class User {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;

}
