package com.tcs.internal.userdomainservice.exception;

/**
 * Exception class for all the validation related errors
 *
 * @author Neeraj Sharma
 */
public class UserDomainServiceValidationException extends RuntimeException {
    private static final long serialVersionUID = 8905093374341527092L;

    /**
     * UserDomainServiceValidationException with message
     *
     * @param message the message
     */
    public UserDomainServiceValidationException(String message) {
        this(message, null);
    }

    /**
     * UserDomainServiceValidationException with message and cause
     *
     * @param message the message
     * @param cause the cause
     */
    public UserDomainServiceValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
