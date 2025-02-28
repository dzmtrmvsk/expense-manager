package com.expensemanager.service;

import com.expensemanager.dto.CategoryDTO;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.model.Category;
import com.expensemanager.repository.CategoryRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	@Autowired
	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	public Category getCategoryById(Long id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category with ID " + id + " not found"));
	}

	public Category createCategory(CategoryDTO categoryDTO) {
		log.info("Creating category with name={}", categoryDTO.getName());
		Category category = new Category();
		category.setName(categoryDTO.getName());
		return categoryRepository.save(category);
	}

	public Category updateCategory(Long id, CategoryDTO categoryDTO) {
		Category existing = getCategoryById(id);
		existing.setName(categoryDTO.getName());
		log.info("Updating category id={} with new name={}", id, categoryDTO.getName());
		return categoryRepository.save(existing);
	}

	public void deleteCategory(Long id) {
		Category existing = getCategoryById(id);
		log.warn("Deleting category id={}", id);
		categoryRepository.delete(existing);
	}
}
