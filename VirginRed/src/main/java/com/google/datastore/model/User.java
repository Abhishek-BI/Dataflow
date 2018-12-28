package com.google.datastore.model;

import java.io.Serializable;

public class User implements Serializable{

	private static final long serialVersionUID = 1L;
	private String email;
	private String encryptedEmail;
	private Long userId;
	
	public User() {
	}
	
	public User(String email, String encryptedEmail, Long userId) {
		this.email = email;
		this.encryptedEmail = encryptedEmail;
		this.userId = userId;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEncryptedEmail() {
		return encryptedEmail;
	}
	public void setEncryptedEmail(String encryptedEmail) {
		this.encryptedEmail = encryptedEmail;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(userId);
		builder.append(",").append(email);
		builder.append(",").append(encryptedEmail);
		return builder.toString();
	}
	
	
}
