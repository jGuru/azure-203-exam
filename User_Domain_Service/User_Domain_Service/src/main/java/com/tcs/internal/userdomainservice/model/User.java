package com.tcs.internal.userdomainservice.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {

	@Id
	private String emailId;
	private String name;
	private String surname;
	private String password; // Plain password as of now so not encrypting it

	public User() {

	}

	public User(String emailId, String name, String surname) {
		super();
		this.emailId = emailId;
		this.name = name;
		this.surname = surname;
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
