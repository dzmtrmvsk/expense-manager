package com.expensemanager.service;

import com.expensemanager.cache.ExpenseCache;
import com.expensemanager.dto.ExpenseDTO;
import com.expensemanager.dto.ExpenseUpdateDTO;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.integration.ExchangeRateService;
import com.expensemanager.model.Category;
import com.expensemanager.model.Expense;
import com.expensemanager.model.Tag;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

	@Mock
	private ExpenseRepository expenseRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private TagRepository tagRepository;

	@Mock
	private ExchangeRateService exchangeRateService;

	@Mock
	private ExpenseCache expenseCache;

	@Spy
	@InjectMocks
	private ExpenseService expenseService;

	@BeforeEach
	void setUp() {
		// Устанавливаем self-ссылку, чтобы методы вызывались через прокси
		ReflectionTestUtils.setField(expenseService, "self", expenseService);
	}

	// ---------- createExpense ----------

	// Ветка без тегов (dto.getTags() == null)
	@Test
	void testCreateExpenseWithoutTags() {
		ExpenseDTO dto = new ExpenseDTO();
		dto.setName("Dinner");
		dto.setAmount(20.0);
		dto.setCurrency("USD");
		dto.setCategory("Food");
		// Теги оставляем null

		Category cat = new Category();
		cat.setName("Food");
		when(categoryRepository.findByNameIgnoreCase("Food")).thenReturn(Optional.of(cat));

		Expense expense = new Expense();
		expense.setName("Dinner");
		expense.setAmount(20.0);
		expense.setCurrency("USD");
		expense.setCategory(cat);

		Expense savedExpense = new Expense();
		savedExpense.setId(100L);
		savedExpense.setName("Dinner");
		savedExpense.setAmount(20.0);
		savedExpense.setCurrency("USD");
		savedExpense.setCategory(cat);
		when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

		Expense result = expenseService.createExpense(dto);
		assertThat(result.getId()).isEqualTo(100L);
		verify(expenseCache).put(100L, savedExpense);
	}

	// Ветка с тегами (dto.getTags() != null)
	@Test
	void testCreateExpenseWithTags() {
		ExpenseDTO dto = new ExpenseDTO();
		dto.setName("Lunch");
		dto.setAmount(15.0);
		dto.setCurrency("EUR");
		dto.setCategory("Meals");
		List<String> tags = new ArrayList<>();
		tags.add("fast");
		tags.add("cheap");
		dto.setTags(new HashSet<>(tags));
		Category cat = new Category();
		cat.setName("Meals");
		when(categoryRepository.findByNameIgnoreCase("Meals")).thenReturn(Optional.of(cat));
		when(tagRepository.findByNameIgnoreCase("fast")).thenReturn(Optional.empty());
		when(tagRepository.findByNameIgnoreCase("cheap")).thenReturn(Optional.empty());
		when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
			Tag arg = invocation.getArgument(0);
			if (arg.getName().equals("fast")) {
				arg.setId(1L);
			} else if (arg.getName().equals("cheap")) {
				arg.setId(2L);
			}
			return arg;
		});

		Expense expense = new Expense();
		expense.setName("Lunch");
		expense.setAmount(15.0);
		expense.setCurrency("EUR");
		expense.setCategory(cat);

		Expense savedExpense = new Expense();
		savedExpense.setId(101L);
		savedExpense.setName("Lunch");
		savedExpense.setAmount(15.0);
		savedExpense.setCurrency("EUR");
		savedExpense.setCategory(cat);
		// Предполагаем, что после разрешения тегов добавляются два тега
		Tag t1 = new Tag("fast");
		t1.setId(1L);
		Tag t2 = new Tag("cheap");
		t2.setId(2L);
		savedExpense.getTags().add(t1);
		savedExpense.getTags().add(t2);

		when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

		Expense result = expenseService.createExpense(dto);
		assertThat(result.getId()).isEqualTo(101L);
		assertThat(result.getTags()).hasSize(2);
		verify(expenseCache).put(101L, savedExpense);
	}

	// ---------- getAllExpenses ----------

	@Test
	void testGetAllExpenses() {
		Expense e1 = new Expense();
		e1.setId(1L);
		Expense e2 = new Expense();
		e2.setId(2L);
		List<Expense> list = List.of(e1, e2);
		when(expenseRepository.findAllWithAssociations()).thenReturn(list);
		// Для первого возвращаем null (cache miss), для второго – уже есть в кеше
		when(expenseCache.get(1L)).thenReturn(null);
		when(expenseCache.get(2L)).thenReturn(e2);

		List<Expense> result = expenseService.getAllExpenses();
		assertThat(result).hasSize(2);
		verify(expenseCache).put(1L, e1);
	}

	// ---------- getExpenseById ----------

	@Test
	void testGetExpenseByIdCacheHit() {
		Expense cached = new Expense();
		cached.setId(10L);
		when(expenseCache.get(10L)).thenReturn(cached);
		Expense result = expenseService.getExpenseById(10L);
		assertThat(result).isSameAs(cached);
		verify(expenseRepository, never()).findByIdWithAssociations(anyLong());
	}

	@Test
	void testGetExpenseByIdCacheMiss() {
		when(expenseCache.get(20L)).thenReturn(null);
		Expense repoExpense = new Expense();
		repoExpense.setId(20L);
		when(expenseRepository.findByIdWithAssociations(20L)).thenReturn(Optional.of(repoExpense));
		Expense result = expenseService.getExpenseById(20L);
		assertThat(result).isSameAs(repoExpense);
		verify(expenseCache).put(20L, repoExpense);
	}

	@Test
	void testGetExpenseByIdNotFound() {
		when(expenseCache.get(30L)).thenReturn(null);
		when(expenseRepository.findByIdWithAssociations(30L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> expenseService.getExpenseById(30L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Expense with ID 30 not found");
	}

	// ---------- updateExpense ----------

	@Test
	void testUpdateExpenseSuccess() {
		Expense existing = new Expense();
		existing.setId(40L);
		existing.setName("OldName");
		when(expenseCache.get(40L)).thenReturn(existing);

		ExpenseDTO dto = new ExpenseDTO();
		dto.setName("NewName");
		dto.setAmount(50.0);
		dto.setCurrency("GBP");
		dto.setCategory("NewCat");
		List<String> tagList = List.of("tag1", "tag2");
		dto.setTags(new HashSet<>(tagList));

		Category newCat = new Category();
		newCat.setName("NewCat");
		when(categoryRepository.findByNameIgnoreCase("NewCat")).thenReturn(Optional.of(newCat));

		// Разрешение тегов
		when(tagRepository.findByNameIgnoreCase("tag1")).thenReturn(Optional.empty());
		when(tagRepository.findByNameIgnoreCase("tag2")).thenReturn(Optional.empty());
		when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
			Tag arg = invocation.getArgument(0);
			if (arg.getName().equals("tag1")) {
				arg.setId(11L);
			} else if (arg.getName().equals("tag2")) {
				arg.setId(12L);
			}
			return arg;
		});

		Expense updated = new Expense();
		updated.setId(40L);
		updated.setName("NewName");
		updated.setAmount(50.0);
		updated.setCurrency("GBP");
		updated.setCategory(newCat);
		Tag t1 = new Tag("tag1");
		t1.setId(11L);
		Tag t2 = new Tag("tag2");
		t2.setId(12L);
		updated.getTags().add(t1);
		updated.getTags().add(t2);
		when(expenseRepository.save(existing)).thenReturn(updated);

		Expense result = expenseService.updateExpense(40L, dto);
		assertThat(result.getName()).isEqualTo("NewName");
		assertThat(result.getTags()).hasSize(2);
		verify(expenseCache).put(40L, updated);
	}

	// ---------- updateExpensePartial ----------

	@Test
	void testUpdateExpensePartialSuccess() {
		Expense existing = new Expense();
		existing.setId(50L);
		existing.setName("Old");
		existing.setAmount(100.0);
		existing.setCurrency("USD");
		when(expenseCache.get(50L)).thenReturn(existing);

		ExpenseUpdateDTO updateDTO = new ExpenseUpdateDTO();
		updateDTO.setName("PartialNew");
		updateDTO.setAmount(150.0);
		updateDTO.setCurrency("EUR");
		updateDTO.setCategory("UpdatedCat");
		List<String> newTags = List.of("newTag");
		updateDTO.setTags(new HashSet<>(newTags));

		Category updatedCat = new Category();
		updatedCat.setName("UpdatedCat");
		when(categoryRepository.findByNameIgnoreCase("UpdatedCat")).thenReturn(Optional.of(updatedCat));

		when(tagRepository.findByNameIgnoreCase("newTag")).thenReturn(Optional.empty());
		Tag newTag = new Tag("newTag");
		newTag.setId(21L);
		when(tagRepository.save(any(Tag.class))).thenReturn(newTag);

		Expense updatedExpense = new Expense();
		updatedExpense.setId(50L);
		updatedExpense.setName("PartialNew");
		updatedExpense.setAmount(150.0);
		updatedExpense.setCurrency("EUR");
		updatedExpense.setCategory(updatedCat);
		updatedExpense.getTags().add(newTag);
		when(expenseRepository.save(existing)).thenReturn(updatedExpense);

		Expense result = expenseService.updateExpensePartial(50L, updateDTO);
		assertThat(result.getName()).isEqualTo("PartialNew");
		assertThat(result.getAmount()).isEqualTo(150.0);
		assertThat(result.getCurrency()).isEqualTo("EUR");
		assertThat(result.getCategory()).isEqualTo(updatedCat);
		assertThat(result.getTags()).hasSize(1);
		verify(expenseCache).put(50L, updatedExpense);
	}

	@Test
	void testUpdateExpensePartialNoUpdate() {
		Expense existing = new Expense();
		existing.setId(60L);
		existing.setName("Original");
		existing.setAmount(200.0);
		existing.setCurrency("USD");
		when(expenseCache.get(60L)).thenReturn(existing);

		ExpenseUpdateDTO updateDTO = new ExpenseUpdateDTO();
		updateDTO.setName("   ");
		updateDTO.setAmount(null);
		updateDTO.setCurrency(null);
		updateDTO.setCategory("  ");
		updateDTO.setTags(null);

		when(expenseRepository.save(existing)).thenReturn(existing);

		Expense result = expenseService.updateExpensePartial(60L, updateDTO);
		assertThat(result.getName()).isEqualTo("Original");
		assertThat(result.getAmount()).isEqualTo(200.0);
		assertThat(result.getCurrency()).isEqualTo("USD");
		verify(expenseCache).put(60L, existing);
	}

	// ---------- deleteExpense ----------

	@Test
	void testDeleteExpense() {
		Expense existing = new Expense();
		existing.setId(70L);
		when(expenseCache.get(70L)).thenReturn(existing);

		expenseService.deleteExpense(70L);
		verify(expenseRepository).delete(existing);
		verify(expenseCache).remove(70L);
	}

	// ---------- getExpensesByCategory ----------

	@Test
	void testGetExpensesByCategoryFound() {
		Expense e1 = new Expense();
		e1.setId(80L);
		Expense e2 = new Expense();
		e2.setId(81L);
		List<Expense> list = List.of(e1, e2);
		when(expenseRepository.findByCategoryName("CatA")).thenReturn(list);
		when(expenseCache.get(80L)).thenReturn(null);
		when(expenseCache.get(81L)).thenReturn(e2);

		List<Expense> result = expenseService.getExpensesByCategory("CatA");
		assertThat(result).containsExactly(e1, e2);
		verify(expenseCache).put(80L, e1);
	}

	@Test
	void testGetExpensesByCategoryNotFound() {
		when(expenseRepository.findByCategoryName("EmptyCat")).thenReturn(Collections.emptyList());
		assertThatThrownBy(() -> expenseService.getExpensesByCategory("EmptyCat"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("No expenses found for category 'EmptyCat'");
	}

	// ---------- getExpensesByAmountRange ----------

	@Test
	void testGetExpensesByAmountRangeFound() {
		Expense e1 = new Expense();
		e1.setId(90L);
		Expense e2 = new Expense();
		e2.setId(91L);
		List<Expense> list = List.of(e1, e2);
		when(expenseRepository.findByAmountRange(10.0, 50.0)).thenReturn(list);
		when(expenseCache.get(90L)).thenReturn(null);
		when(expenseCache.get(91L)).thenReturn(e2);

		List<Expense> result = expenseService.getExpensesByAmountRange(10.0, 50.0);
		assertThat(result).containsExactly(e1, e2);
		verify(expenseCache).put(90L, e1);
	}

	@Test
	void testGetExpensesByAmountRangeNotFound() {
		when(expenseRepository.findByAmountRange(1000.0, 2000.0)).thenReturn(Collections.emptyList());
		assertThatThrownBy(() -> expenseService.getExpensesByAmountRange(1000.0, 2000.0))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("No expenses found in amount range [1000.0, 2000.0]");
	}

	// ---------- searchByNamePart ----------

	@Test
	void testSearchByNamePartFound() {
		Expense e1 = new Expense();
		e1.setId(92L);
		Expense e2 = new Expense();
		e2.setId(93L);
		List<Expense> list = List.of(e1, e2);
		when(expenseRepository.searchByNamePart("part")).thenReturn(list);
		when(expenseCache.get(92L)).thenReturn(null);
		when(expenseCache.get(93L)).thenReturn(e2);

		List<Expense> result = expenseService.searchByNamePart("part");
		assertThat(result).containsExactly(e1, e2);
		verify(expenseCache).put(92L, e1);
	}

	@Test
	void testSearchByNamePartNotFound() {
		when(expenseRepository.searchByNamePart("nope")).thenReturn(Collections.emptyList());
		assertThatThrownBy(() -> expenseService.searchByNamePart("nope"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("No expenses found matching name part 'nope'");
	}

	// ---------- getExpensesByTag ----------

	@Test
	void testGetExpensesByTagFound() {
		Expense e1 = new Expense();
		e1.setId(94L);
		Expense e2 = new Expense();
		e2.setId(95L);
		List<Expense> list = List.of(e1, e2);
		when(expenseRepository.findByTagName("urgent")).thenReturn(list);
		when(expenseCache.get(94L)).thenReturn(null);
		when(expenseCache.get(95L)).thenReturn(e2);

		List<Expense> result = expenseService.getExpensesByTag("urgent");
		assertThat(result).containsExactly(e1, e2);
		verify(expenseCache).put(94L, e1);
	}

	@Test
	void testGetExpensesByTagNotFound() {
		when(expenseRepository.findByTagName("nonexistent")).thenReturn(Collections.emptyList());
		assertThatThrownBy(() -> expenseService.getExpensesByTag("nonexistent"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("No expenses found for tag 'nonexistent'");
	}

	// ---------- getExpenseAmountInCurrency ----------

	@Test
	void testGetExpenseAmountInCurrency() {
		Expense expense = new Expense();
		expense.setId(96L);
		expense.setAmount(100.0);
		expense.setCurrency("USD");
		when(expenseCache.get(96L)).thenReturn(expense);
		when(exchangeRateService.getExchangeRate("USD", "INR")).thenReturn(75.0);

		Double converted = expenseService.getExpenseAmountInCurrency(96L, "INR");
		assertThat(converted).isEqualTo(7500.0);
	}

	// ---------- createExpensesBulk ----------

	@Test
	void testCreateExpensesBulk() {
		ExpenseDTO dto1 = new ExpenseDTO();
		dto1.setName("Bulk1");
		dto1.setAmount(10.0);
		dto1.setCurrency("USD");
		dto1.setCategory("BulkCat");
		ExpenseDTO dto2 = new ExpenseDTO();
		dto2.setName("Bulk2");
		dto2.setAmount(20.0);
		dto2.setCurrency("USD");
		dto2.setCategory("BulkCat");
		List<ExpenseDTO> dtoList = List.of(dto1, dto2);

		// Симулируем разрешение категории
		Category bulkCat = new Category();
		bulkCat.setName("BulkCat");
		when(categoryRepository.findByNameIgnoreCase("BulkCat")).thenReturn(Optional.of(bulkCat));

		Expense e1 = new Expense();
		e1.setId(101L);
		e1.setName("Bulk1");
		e1.setAmount(10.0);
		e1.setCurrency("USD");
		e1.setCategory(bulkCat);
		Expense e2 = new Expense();
		e2.setId(102L);
		e2.setName("Bulk2");
		e2.setAmount(20.0);
		e2.setCurrency("USD");
		e2.setCategory(bulkCat);
		when(expenseRepository.saveAll(anyList())).thenReturn(List.of(e1, e2));

		List<Expense> result = expenseService.createExpensesBulk(dtoList);
		assertThat(result).containsExactly(e1, e2);
		verify(expenseCache).put(101L, e1);
		verify(expenseCache).put(102L, e2);
	}
}
