package com.stg.entity.user;

import com.stg.errors.ApplicationException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

@Data
@Accessors(chain = true)
public class CustomerIdentifier {
    private static final int NUMBER_OF_FRAGMENTS = 2;

    private static final int POSITION_MB_TOKEN = 0;
    private static final int POSITION_MB_ID = 1;

    private String mbToken;
    private String mbId;

    public static CustomerIdentifier of(String subject) {
        String[] fragments = subject.split(":");

        if (fragments.length != NUMBER_OF_FRAGMENTS) {
            throw new ApplicationException("Invalid subject=" + subject);
        }

        if (Arrays.stream(fragments).anyMatch(StringUtils::isEmpty)) {
            throw new ApplicationException("Invalid subject=" + subject);
        }

        return new CustomerIdentifier()
                .setMbToken(fragments[POSITION_MB_TOKEN])
                .setMbId(fragments[POSITION_MB_ID]);
    }
}
