package com.otpone.otpone.util.data_mgt_framework;

/**
 * Created by DJ on 5/28/2017.
 */

public class IllegalMappingException extends RuntimeException {

    public IllegalMappingException() {
    }

    public IllegalMappingException(String message) {
        super(message);
    }

    public IllegalMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalMappingException(Throwable cause) {
        super(cause);
    }
}
