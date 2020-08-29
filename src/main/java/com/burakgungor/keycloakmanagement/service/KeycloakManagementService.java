package com.burakgungor.keycloakmanagement.service;

import com.burakgungor.keycloakmanagement.model.Credentials;
import com.burakgungor.keycloakmanagement.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KeycloakManagementService {
    @Value("${keycloak.auth-server-url}")
    private String authserverUrl;

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
        return KeycloakBuilder.builder().serverUrl(authserverUrl).realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS).clientId(clientId).clientSecret(clientSecret).build();
    }

    private RealmResource getRealmResource() {
        return getKeycloak().realm(realm);
    }

    private UsersResource getUsersResource() {
        return getKeycloak().realm(realm).users();
    }

    public AccessTokenResponse getClientAccessToken(Credentials credentials) {
        Keycloak keycloak = getKeycloak(credentials.getClientId(), credentials.getClientSecret());
        return keycloak.tokenManager().getAccessToken();
    }

    public User createUser(User user) {
        UsersResource userResource = getUsersResource();
        UserRepresentation userRepresentation = new UserRepresentation();
        try {
            getUserRepresentations(user.getUsername(), userResource);
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
            credentialRep.setTemporary(false);

            userRes.resetPassword(credentialRep);
            return user;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteUser(String username) {
        List<UserRepresentation> userRepresentations = getUserRepresentations(username, getUsersResource());
        UserResource userRes = getUsersResource().get(userRepresentations.get(0).getId());
        userRepresentations.get(0).setEnabled(false);

        userRes.update(userRepresentations.get(0));
        //keycloak.realm(realm).users().get(userRepresentations.get(0).getId()).remove();
        return true;
    }

    public List<UserRepresentation> getUserRepresentationByUserName(String username) {
        UsersResource userResource = getUsersResource();
        List<UserRepresentation> userRepresentations = getUserRepresentations(username, userResource);
        return userRepresentations;
    }

    private List<UserRepresentation> getUserRepresentations(String username, UsersResource userResource) {
        List<UserRepresentation> userRepresentations = userResource.search(username);
        if (CollectionUtils.isEmpty(userRepresentations)) {
            throw new RuntimeException("User not found with this username !");
        }
        return userRepresentations;
    }

    public void addUserToSubGroup(String username, String group) {
        List<UserRepresentation> userRepresentation = getUserRepresentationByUserName(username);
        String id = userRepresentation.get(0).getId();
        if (checkUserExistanceInGroup(id, group)) {
            throw new RuntimeException(
                    "Keycloak üzerinde bu grup bilgisine sahip kullanici bulundu. Lütfen sistem adminiyle iletişime geçiniz.");
        }
        getGroupRepresentations(getRealmResource())
                .stream()
                .filter(groupRepresentation -> groupRepresentation.getPath().equals("/SYSTEM/" + group))
                .forEach(groupRepresentation -> getRealmResource().users().get(id).joinGroup(groupRepresentation.getId()));

    }

    public boolean checkUserExistanceInGroup(String userId, String group) {
        List<GroupRepresentation> groups = getRealmResource().users().get(userId).groups();
        boolean isExist = false;
        if (!CollectionUtils.isEmpty(groups))
            isExist = groups.stream().anyMatch(groupRepresentation -> Objects.equals(groupRepresentation.getName(), group));
        return isExist;
    }

    public static List<GroupRepresentation> getGroupRepresentations(RealmResource realmResource) {
        return realmResource
                .groups()
                .groups()
                .stream()
                .filter(groupRepresentation -> groupRepresentation.getPath().equals("/SYSTEM"))
                .map(GroupRepresentation::getSubGroups)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
