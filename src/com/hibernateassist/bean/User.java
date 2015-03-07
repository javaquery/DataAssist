package com.hibernateassist.bean;

import java.util.HashSet;
import java.util.Set;

/**
 * @author vicky.thakor
 */
public class User {
	private int id;
	private String Username;
	private String Password;
	private String Email;
	private Set<Message> Messages = new HashSet<Message>();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return Username;
	}
	public void setUsername(String username) {
		Username = username;
	}
	public String getPassword() {
		return Password;
	}
	public void setPassword(String password) {
		Password = password;
	}
	public String getEmail() {
		return Email;
	}
	public void setEmail(String email) {
		Email = email;
	}
	public Set<Message> getMessages() {
		return Messages;
	}
	public void setMessages(Set<Message> messages) {
		Messages = messages;
	}
}
