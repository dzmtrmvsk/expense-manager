package com.expensemanager.service;

import com.expensemanager.cache.CategoryCache;
import com.expensemanager.dto.CategoryDTO;
import com.expensemanager.exception.CategoryAlreadyExistsException;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.model.Category;
import com.expensemanager.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private CategoryCache categoryCache;

	@InjectMocks
	private CategoryService categoryService;

	@BeforeEach
	void setup() {
		// Not require self link
	}

	// ---------- getAllCategories ----------

	@Test
	void testGetAllCategories() {
		Category cat1 = new Category();
		cat1.setId(1L);
		Category cat2 = new Category();
		cat2.setId(2L);
		List<Category> categories = List.of(cat1, cat2);
		when(categoryRepository.findAll()).thenReturn(categories);
		when(categoryCache.get(1L)).thenReturn(null);
		when(categoryCache.get(2L)).thenReturn(cat2);

		List<Category> result = categoryService.getAllCategories();
		assertThat(result).hasSize(2);
		verify(categoryCache).put(1L, cat1);
		verify(categoryCache, never()).put(eq(2L), any());
	}

	// ---------- getCategoryById ----------

	@Test
	void testGetCategoryById_CacheHit() {
		Category cat = new Category();
		cat.setId(10L);
		when(categoryCache.get(10L)).thenReturn(cat);

		Category result = categoryService.getCategoryById(10L);
		assertThat(result).isSameAs(cat);
		verify(categoryRepository, never()).findById(anyLong());
	}

	@Test
	void testGetCategoryById_CacheMissFound() {
		Category cat = new Category();
		cat.setId(20L);
		when(categoryCache.get(20L)).thenReturn(null);
		when(categoryRepository.findById(20L)).thenReturn(Optional.of(cat));

		Category result = categoryService.getCategoryById(20L);
		assertThat(result).isSameAs(cat);
		verify(categoryCache).put(20L, cat);
	}

	@Test
	void testGetCategoryById_NotFound() {
		when(categoryCache.get(30L)).thenReturn(null);
		when(categoryRepository.findById(30L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> categoryService.getCategoryById(30L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Category with ID 30 not found");
	}

	// ---------- createCategory ----------

	@Test
	void testCreateCategory_AlreadyExists() {
		CategoryDTO dto = new CategoryDTO();
		dto.setName("Food");
		when(categoryRepository.existsByName("Food")).thenReturn(true);

		assertThatThrownBy(() -> categoryService.createCategory(dto))
				.isInstanceOf(CategoryAlreadyExistsException.class)
				.hasMessageContaining("Category with name Food already exists");
	}

	@Test
	void testCreateCategory_Success() {
		CategoryDTO dto = new CategoryDTO();
		dto.setName("Drinks");
		when(categoryRepository.existsByName("Drinks")).thenReturn(false);

		Category category = new Category();
		category.setName("Drinks");

		Category savedCategory = new Category();
		savedCategory.setId(40L);
		savedCategory.setName("Drinks");

		when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

		Category result = categoryService.createCategory(dto);
		assertThat(result.getId()).isEqualTo(40L);
		assertThat(result.getName()).isEqualTo("Drinks");
		verify(categoryCache).put(40L, savedCategory);
	}

	// ---------- updateCategory ----------

	@Test
	void testUpdateCategory_Success() {
		Category existing = new Category();
		existing.setId(50L);
		existing.setName("OldName");
		when(categoryCache.get(50L)).thenReturn(existing);

		CategoryDTO dto = new CategoryDTO();
		dto.setName("NewName");

		Category updated = new Category();
		updated.setId(50L);
		updated.setName("NewName");

		when(categoryRepository.save(existing)).thenReturn(updated);

		Category result = categoryService.updateCategory(50L, dto);
		assertThat(result.getName()).isEqualTo("NewName");
		verify(categoryCache).put(50L, updated);
	}

	// ---------- deleteCategory ----------

	@Test
	void testDeleteCategory() {
		Category existing = new Category();
		existing.setId(60L);
		when(categoryCache.get(60L)).thenReturn(existing);

		categoryService.deleteCategory(60L);
		verify(categoryRepository).delete(existing);
		verify(categoryCache).remove(60L);
	}
}
