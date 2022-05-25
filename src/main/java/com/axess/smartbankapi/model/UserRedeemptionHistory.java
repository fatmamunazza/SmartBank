package com.axess.smartbankapi.model;

import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("RewardsCatalogue")
public class UserRedeemptionHistory {

	@Id
	private long id;

	private RewardsCatalogue catalogue;
	private int quantity;

	private CCUser ccUser;
	private LocalDate orderdate;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public RewardsCatalogue getCatalogue() {
		return catalogue;
	}
	public void setCatalogue(RewardsCatalogue catalogue) {
		this.catalogue = catalogue;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public CCUser getCcUser() {
		return ccUser;
	}
	public void setCcUser(CCUser ccUser) {
		this.ccUser = ccUser;
	}
	public LocalDate getOrderdate() {
		return orderdate;
	}
	public void setOrderdate(LocalDate orderdate) {
		this.orderdate = orderdate;
	}
	
	

}
