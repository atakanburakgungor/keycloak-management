package com.burakgungor.keycloakmanagement.resource;

import com.burakgungor.keycloakmanagement.model.ClientArgs;
import com.burakgungor.keycloakmanagement.model.User;
import com.burakgungor.keycloakmanagement.service.KeycloakManagementService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/keycloak-management")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KeycloakManagementResource {

    @NonNull
    private KeycloakManagementService keycloakManagementService;

    @PostMapping("/client-access-token")
    public AccessTokenResponse getClientAccessToken(@RequestBody ClientArgs clientArgs) {
        return keycloakManagementService.getClientAccessToken(clientArgs);
    }

    @PostMapping("/create-user")
    public User createKeyclakUser(@RequestBody User user) {
        return keycloakManagementService.createUser(user);
    }

    @GetMapping("/user/{username}")
    public List<UserRepresentation> getUserRepresentationByUserName(@PathVariable("username") String username) {
        return keycloakManagementService.getUserRepresentationByUserName(username);
    }
}
