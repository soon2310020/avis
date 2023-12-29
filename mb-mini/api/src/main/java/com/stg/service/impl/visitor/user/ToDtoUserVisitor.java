package com.stg.service.impl.visitor.user;

import com.stg.entity.credential.Credential;
import com.stg.entity.user.FeatureUser;
import com.stg.entity.user.User;
import com.stg.entity.user.UserVisitor;
import com.stg.repository.FeatureUserRepository;
import com.stg.service.dto.credential.CredentialDto;
import com.stg.service.dto.user.UserDto;
import com.stg.service.impl.visitor.credential.ToDtoCredentialVisitor;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.stg.entity.user.User.Role.ADMIN;
import static com.stg.entity.user.User.Role.SUPER_ADMIN;

/**
 * Creates the corresponding user dto from a user entity object.
 */
public class ToDtoUserVisitor implements UserVisitor<UserDto> {

    public static final Map<User.Role, String> ROLES;

    static {
        ROLES = Map.of(SUPER_ADMIN, UserDto.ROLE_SUPER_ADMIN, ADMIN, UserDto.ROLE_ADMIN);
    }

    private final boolean withCredentials;
    private final FeatureUserRepository featureUserRepository;

    public ToDtoUserVisitor(boolean withCredentials,
                            FeatureUserRepository featureUserRepository) {
        this.withCredentials = withCredentials;
        this.featureUserRepository = featureUserRepository;
    }

    @Override
    public UserDto visit(User user) {
        UserDto dto = new UserDto();

        setUserFields(user, dto);

        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setNonLocked(user.getNonLocked());
        dto.setRole(ROLES.get(user.getRole()));
        return dto;
    }

    private void setUserFields(User user, UserDto dto) {
        FeatureUser featureUser = featureUserRepository.findByUserId(user.getId());
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.getEnabled());
        dto.setCreationTime(user.getCreationTime());
        if (withCredentials) {
            dto.setCredentials(toCredentialDto(user.getCredentials()));
        }
        dto.setFeatures(featureUser == null ? new ArrayList<>() : featureUser.getFeatureAsList());
    }

    private static Set<CredentialDto> toCredentialDto(Set<Credential> credentialSet) {
        return credentialSet.stream()
                .map(c -> c.accept(new ToDtoCredentialVisitor()))
                .collect(Collectors.toSet());
    }

}
