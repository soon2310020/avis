package com.stg.service.lock.base;


import com.stg.errors.ApplicationException;

import static com.stg.utils.DateUtil.convertDurationToText;

public class RedLockException extends ApplicationException {

    public RedLockException(String message) {
        super(message);
    }

    public RedLockException(String message, long duration) {
        super(message + " (" + convertDurationToText(duration) + ")");
    }

}
