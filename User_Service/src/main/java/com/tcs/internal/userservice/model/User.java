package com.tcs.internal.userservice.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;

/**
 * Domain class for User
 *
 * @author Neeraj Sharma
 */
public class User extends ErrorResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(notes = "The user email", example = "test@gmail.com")
	private String emailId;
	
	@ApiModelProperty(notes = "The user first name", example = "Neeraj")
	private String name;
	
	@ApiModelProperty(notes = "The user last name", example = "Sharma")
	private String surname;
	/**
	 * The password of the user currently it is a plain password no encryption
	 */
	@ApiModelProperty(notes = "The user password", example = "shdg@37267")
	private String password;

	/**
	 * The no argument constructor 
	 */
	
	public User() {}

	
	/**
	 * The constructor with all the details of the user 
	 * @param emailId the email id of the user
	 * @param name the first name of the user
	 * @param surname the family name of the user
	 * @param password a non encrypted password of the user
	 */
	public User(String emailId, String name, String surname, String password) {
		super();
		this.emailId = emailId;
		this.name = name;
		this.surname = surname;
		this.password = password;
	}
	
	/**
	 * @return emailId of the user
	 */

	public String getEmailId() {
		return emailId;
	}
	
	/**
	 * @param emailId
	 */

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String toString() {
		return name + " : " + surname + " : " + emailId;
	}

}
