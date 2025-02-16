package com.expensemanager.model;

public class Expense {
	private Long id;
	private String name;
	private String category;
	private Double amount;

	public Expense() {
	}

	public Expense(Long id, String name, String category, Double amount) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.amount = amount;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public Double getAmount() {
		return amount;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}
}