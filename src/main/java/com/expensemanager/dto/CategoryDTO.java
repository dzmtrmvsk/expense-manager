package com.expensemanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {
	@NotBlank(message = "Category name cannot be blank")
	private String name;
}
