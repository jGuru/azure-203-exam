package com.tcs.internal.userdomainservice.exception;

/**
 * Exception class for all the service related errors
 *
 * @author Neeraj Sharma
 */
public class UserDomainServiceException extends RuntimeException {
    private static final long serialVersionUID = 8905093374341521256L;

    /**
     * UserDomainServiceException with message
     *
     * @param message the message
     */
    public UserDomainServiceException(String message) {
        this(message, null);
    }

    /**
     * UserDomainServiceException with message and cause
     *
     * @param message the message
     * @param cause the cause
     */
    public UserDomainServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
