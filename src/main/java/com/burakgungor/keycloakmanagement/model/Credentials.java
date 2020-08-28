package com.burakgungor.keycloakmanagement.model;

import lombok.Data;

@Data
public class Credentials {
    private String clientId;
    private String clientSecret;
}
