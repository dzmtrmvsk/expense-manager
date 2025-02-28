package com.expensemanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.Set;
import lombok.Data;

@Data
public class ExpenseDTO {

	@NotBlank(message = "Expense name cannot be blank")
	private String name;

	@NotNull(message = "Amount is required")
	@PositiveOrZero(message = "Amount must be non-negative")
	private Double amount;

	@NotBlank(message = "Currency cannot be blank")
	private String currency;

	@NotBlank(message = "Category cannot be blank")
	private String category;

	private Set<String> tags;
}
