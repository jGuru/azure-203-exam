package com.tcs.internal.userdomainservice.domain;

import java.io.Serializable;

/**
 * Domain class to hold the error response
 *
 * @author Neeraj Sharma
 */
public class ErrorResponse implements Serializable {
    private static final long serialVersionUID = -7203973328647136543L;

    private String title;
    private String details;

    /**
     * Default constructor
     */
    public ErrorResponse() {
        // Enables the default constructor
    }

    /**
     * ErrorResponse constructor to construct error title and details
     *
     * @param title   The error title
     * @param details The error details
     */
    private ErrorResponse(String title, String details) {
        this.title = title;
        this.details = details;
    }

    /**
     * Builds the error response
     *
     * @param title   The error title
     * @param details The error details
     * @return ErrorResponse
     */
    public static ErrorResponse buildErrorResponse(String title, String details) {
        return new ErrorResponse(title, details);
    }


    /**
     * Gets the title
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title
     *
     * @param title The error tile
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the details
     *
     * @return details
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets the details
     *
     * @param details The error details
     */
    public void setDetails(String details) {
        this.details = details;
    }
}
