package com.tcs.internal.userservice.model;

/**
 * Represents a User. A user that can be registered in the database using any
 * rest client
 */
public class User {

	/**
	 * The Email id of the user this serves as primary key in database
	 */
	private String emailId;
	/**
	 * The first Name of the user
	 */
	private String name;
	/**
	 * The surname or family name of the user
	 */
	private String surname;
	/**
	 * The password of the user currently it is a plain password no encryption
	 */

	private String password;

	public User() {

	}

	public User(String emailId, String name, String surname, String password) {
		super();
		this.emailId = emailId;
		this.name = name;
		this.surname = surname;
		this.password = password;
	}

	public String getEmailId() {
		return emailId;
	}

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
