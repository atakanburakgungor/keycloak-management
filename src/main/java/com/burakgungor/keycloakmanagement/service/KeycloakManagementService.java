package com.burakgungor.keycloakmanagement.service;

import com.burakgungor.keycloakmanagement.model.ClientArgs;
import com.burakgungor.keycloakmanagement.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.Response;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KeycloakManagementService {
    @Value("${keycloak.url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public Keycloak getKeycloak() {
        return getKeycloak(clientId, clientSecret);
    }

    private Keycloak getKeycloak(String clientId, String clientSecret) {
        return KeycloakBuilder.builder().serverUrl(serverUrl).realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS).clientId(clientId).clientSecret(clientSecret).build();
    }

    public AccessTokenResponse getClientAccessToken(ClientArgs clientArgs) {
        Keycloak keycloak = getKeycloak(clientArgs.getClientId(), clientArgs.getClientSecret());
        return keycloak.tokenManager().getAccessToken();
    }

    public User createUser(User user) {
        UsersResource userResource = getUsersResource();
        UserRepresentation userRepresentation = new UserRepresentation();
        List<UserRepresentation> userRepresentations = userResource.search(user.getUsername());
        try {
            if (userRepresentations.size() > 0) {
                throw new RuntimeException("User already created in keycloak.Contact with admin !");
            }
            userRepresentation.setEnabled(true);
            userRepresentation.setUsername(user.getUsername());
            userRepresentation.setLastName(user.getLastName());
            userRepresentation.setEmail(user.getEmail());
            Response response = userResource.create(userRepresentation);

            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            log.info("User created in keycloak service : " + user);

            UserResource userRes = userResource.get(userId);
            CredentialRepresentation credentialRep = new CredentialRepresentation();
            credentialRep.setType(CredentialRepresentation.PASSWORD);
            credentialRep.setValue(user.getPassword());
            credentialRep.setTemporary(true);

            userRes.resetPassword(credentialRep);
            return user;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private UsersResource getUsersResource() {
        return getKeycloak().realm(realm).users();
    }

    public List<UserRepresentation> getUserRepresentationByUserName(String username) {
        UsersResource userResource = getUsersResource();
        List<UserRepresentation> userRepresentations = userResource.search(username);
        if (CollectionUtils.isEmpty(userRepresentations)) {
            throw new RuntimeException("User not found with this username !");
        }
        return userRepresentations;
    }
}
