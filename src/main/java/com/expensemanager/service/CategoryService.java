package com.expensemanager.service;

import com.expensemanager.cache.CategoryCache;
import com.expensemanager.dto.CategoryDTO;
import com.expensemanager.exception.CategoryAlreadyExistsException;
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
	private final CategoryCache categoryCache;

	@Autowired
	public CategoryService(CategoryRepository categoryRepository, CategoryCache categoryCache) {
		this.categoryRepository = categoryRepository;
		this.categoryCache = categoryCache;
	}

	public List<Category> getAllCategories() {
		List<Category> categories = categoryRepository.findAll();
		for (Category category : categories) {
			if (categoryCache.get(category.getId()) == null) {
				categoryCache.put(category.getId(), category);
				log.info("Category with id {} added to cache from getAllCategories", category.getId());
			} else {
				log.info("Category with id {} already present in cache (getAllCategories)", category.getId());
			}
		}
		return categories;
	}

	public Category getCategoryById(Long id) {
		Category cachedCategory = categoryCache.get(id);
		if (cachedCategory != null) {
			log.info("Category with id {} retrieved from cache", id);
			return cachedCategory;
		}
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category with ID " + id + " not found"));
		categoryCache.put(id, category);
		log.info("Category with id {} retrieved from repository and cached", id);
		return category;
	}

	public Category createCategory(CategoryDTO categoryDTO) {
		if (categoryRepository.existsByName(categoryDTO.getName())) {
			throw new CategoryAlreadyExistsException("Category with name " + categoryDTO.getName() + " already exists");
		}
		log.info("Creating category with name={}", categoryDTO.getName());
		Category category = new Category();
		category.setName(categoryDTO.getName());
		Category savedCategory = categoryRepository.save(category);
		categoryCache.put(savedCategory.getId(), savedCategory);
		log.info("Category with id {} created and cached", savedCategory.getId());
		return savedCategory;
	}

	public Category updateCategory(Long id, CategoryDTO categoryDTO) {
		Category existing = getCategoryById(id);
		existing.setName(categoryDTO.getName());
		log.info("Updating category id={} with new name={}", id, categoryDTO.getName());
		Category updatedCategory = categoryRepository.save(existing);
		categoryCache.put(id, updatedCategory);
		log.info("Category with id {} updated and cache refreshed", id);
		return updatedCategory;
	}

	public void deleteCategory(Long id) {
		Category existing = getCategoryById(id);
		log.warn("Deleting category id={}", id);
		categoryRepository.delete(existing);
		categoryCache.remove(id);
		log.info("Category with id {} removed from cache", id);
	}
}
