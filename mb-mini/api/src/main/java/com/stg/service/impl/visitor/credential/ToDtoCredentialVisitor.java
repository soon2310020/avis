package com.stg.service.impl.visitor.credential;

import com.stg.entity.credential.Credential;
import com.stg.entity.credential.CredentialVisitor;
import com.stg.entity.credential.EmailAndPasswordCredential;
import com.stg.service.dto.credential.CredentialDto;
import com.stg.service.dto.credential.EmailAndPasswordCredentialDto;

/**
 * Creates the corresponding credential visitor from a credential entity object.
 */
public class ToDtoCredentialVisitor implements CredentialVisitor<CredentialDto> {

    @Override
    public EmailAndPasswordCredentialDto visit(EmailAndPasswordCredential usernameAndPasswordCredential) {
        EmailAndPasswordCredentialDto usernameAndPasswordCredentialDto = new EmailAndPasswordCredentialDto();
        setCredentialFields(usernameAndPasswordCredential, usernameAndPasswordCredentialDto);
        usernameAndPasswordCredentialDto.setEmail(usernameAndPasswordCredential.getEmail());

        return usernameAndPasswordCredentialDto;
    }

    private void setCredentialFields(Credential credential, CredentialDto dto) {
        dto.setNonExpired(credential.getNonExpired());
        dto.setCreationTime(credential.getCreationTime());
    }
}
