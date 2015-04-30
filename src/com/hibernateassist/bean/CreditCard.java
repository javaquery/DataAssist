package com.hibernateassist.bean;

public class CreditCard {
	private int id;
	private int userID;
	private long CreditCardNumber;
	
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
	public long getCreditCardNumber() {
		return CreditCardNumber;
	}
	public void setCreditCardNumber(long creditCardNumber) {
		CreditCardNumber = creditCardNumber;
	}
}
