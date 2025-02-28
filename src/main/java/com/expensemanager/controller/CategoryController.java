package com.expensemanager.controller;

import com.expensemanager.dto.CategoryDTO;
import com.expensemanager.model.Category;
import com.expensemanager.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Category API", description = "Operations on categories")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

	private final CategoryService categoryService;

	@Autowired
	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	@Operation(summary = "Get all categories")
	public List<Category> getAllCategories() {
		return categoryService.getAllCategories();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get category by ID")
	public Category getCategoryById(@PathVariable("id") Long id) {
		return categoryService.getCategoryById(id);
	}

	@PostMapping
	@Operation(summary = "Create new category")
	public Category createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
		return categoryService.createCategory(categoryDTO);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update category by ID")
	public Category updateCategory(@PathVariable("id") Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
		return categoryService.updateCategory(id, categoryDTO);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete category by ID")
	public void deleteCategory(@PathVariable("id") Long id) {
		categoryService.deleteCategory(id);
	}
}
