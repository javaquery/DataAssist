package com.hibernateassist.bean;

public class Message {
	private int id;
	private int userID;
	private int messageText;
	private String Username;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public int getMessageText() {
		return messageText;
	}
	public void setMessageText(int messageText) {
		this.messageText = messageText;
	}
	public String getUsername() {
		return Username;
	}
	public void setUsername(String username) {
		Username = username;
	}
}
