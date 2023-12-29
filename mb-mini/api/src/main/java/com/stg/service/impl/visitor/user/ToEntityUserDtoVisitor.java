
package com.stg.service.impl.visitor.user;

import com.stg.entity.credential.Credential;
import com.stg.entity.user.User;
import com.stg.service.dto.credential.CredentialDto;
import com.stg.service.dto.user.UserDto;
import com.stg.service.dto.user.UserDtoVisitor;
import com.stg.service.impl.visitor.credential.ToEntityCredentialDtoVisitor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates the corresponding user entity from a user dto object.
 */
@Component
@RequiredArgsConstructor
public class ToEntityUserDtoVisitor implements UserDtoVisitor<User> {

    private final ToEntityCredentialDtoVisitor toEntityCredentialDtoVisitor;
    public static final Map<String, User.Role> ROLES;

    static {
        Map<String, User.Role> initialMap = new HashMap<>();
        initialMap.put(UserDto.ROLE_SUPER_ADMIN, User.Role.SUPER_ADMIN);
        initialMap.put(UserDto.ROLE_ADMIN, User.Role.ADMIN);

        ROLES = Collections.unmodifiableMap(initialMap);
    }


    @Override
    public User visit(UserDto userDto) {
        User user = new User();

        setUserFields(userDto, user);

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setFullName(user.getFirstName() + " " + user.getLastName());
        user.setNonLocked(userDto.getNonLocked());
        user.setRole(ROLES.get(userDto.getRole()));

        return user;
    }

    private void setUserFields(UserDto dto, User user) {
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());

        Set<Credential> credentials = toCredentials(dto.getCredentials());
        user.setEnabled(dto.getEnabled());
        user.setCredentials(credentials);
    }

    private Set<Credential> toCredentials(Set<CredentialDto> credentialDtos) {
        return credentialDtos.stream()
                .map(credentialDto -> credentialDto.accept(toEntityCredentialDtoVisitor))
                .collect(Collectors.toSet());
    }

}
