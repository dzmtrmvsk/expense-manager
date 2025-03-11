package com.expensemanager.dto;

import java.util.Set;
import lombok.Data;

@Data
public class ExpenseUpdateDTO {
	private String name;
	private Double amount;
	private String currency;
	private String category;
	private Set<String> tags;
}
