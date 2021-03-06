package com.burakgungor.keycloakmanagement.resource;

import com.burakgungor.keycloakmanagement.model.Credentials;
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
    public AccessTokenResponse getClientAccessToken(@RequestBody Credentials credentials) {
        return keycloakManagementService.getClientAccessToken(credentials);
    }

    @PostMapping("/create-user")
    public User createKeyclakUser(@RequestBody User user) {
        return keycloakManagementService.createUser(user);
    }

    @DeleteMapping("/delete-user/{username}")
    public Boolean deleteUser(@PathVariable("username") String username) {
        return keycloakManagementService.deleteUser(username);
    }

    @GetMapping("/user/{username}")
    public List<UserRepresentation> getUserRepresentationByUserName(@PathVariable("username") String username) {
        return keycloakManagementService.getUserRepresentationByUserName(username);
    }

    @PostMapping("/add-to-group/{username}/{group}")
    public void addUserToGroup(@PathVariable("group") String group, @PathVariable("username") String username) {
        keycloakManagementService.addUserToSubGroup(username, group);
    }

}
