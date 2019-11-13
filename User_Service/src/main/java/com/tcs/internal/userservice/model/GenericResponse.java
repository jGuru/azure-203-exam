package com.tcs.internal.userservice.model;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * Domain class for generic response
 *
 * @author Neeraj Sharma
 */
public class GenericResponse extends ErrorResponse implements Serializable {
    private static final long serialVersionUID = -7203973328647138754L;

    @ApiModelProperty(notes = "The success flag", example = "true")
    private boolean success;

    /**
     * Is success
     *
     * @return true if response is success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets the success
     *
     * @param success The flag success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
