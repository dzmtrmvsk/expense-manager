package com.expensemanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagDTO {
	@NotBlank(message = "Tag name cannot be blank")
	private String name;
}
