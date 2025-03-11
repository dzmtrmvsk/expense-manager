package com.expensemanager.service;

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
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ExpenseService {

	private final ExpenseRepository expenseRepository;
	private final CategoryRepository categoryRepository;
	private final TagRepository tagRepository;
	private final ExchangeRateService exchangeRateService;
	private final ExpenseService self;

	private static final String CAT_PR = "Category '";
	private static final String CAT_PSF = "' not found";

	public ExpenseService(ExpenseRepository expenseRepository,
	                      CategoryRepository categoryRepository,
	                      TagRepository tagRepository,
	                      ExchangeRateService exchangeRateService,
	                      @Lazy ExpenseService self) {
		this.expenseRepository = expenseRepository;
		this.categoryRepository = categoryRepository;
		this.tagRepository = tagRepository;
		this.exchangeRateService = exchangeRateService;
		this.self = self;
	}

	@Transactional
	public Expense createExpense(ExpenseDTO expenseDTO) {
		Category category = categoryRepository.findByNameIgnoreCase(expenseDTO.getCategory())
				.orElseThrow(() -> new ResourceNotFoundException(CAT_PR + expenseDTO.getCategory() +  CAT_PSF));
		Expense expense = new Expense();
		expense.setName(expenseDTO.getName());
		expense.setAmount(expenseDTO.getAmount());
		expense.setCurrency(expenseDTO.getCurrency());
		expense.setCategory(category);
		if (expenseDTO.getTags() != null && !expenseDTO.getTags().isEmpty()) {
			expense.getTags().addAll(resolveTags(new ArrayList<>(expenseDTO.getTags())));
		}
		return expenseRepository.save(expense);
	}

	@Transactional(readOnly = true)
	public List<Expense> getAllExpenses() {
		return expenseRepository.findAllWithAssociations();
	}

	@Transactional(readOnly = true)
	public Expense getExpenseById(Long id) {
		return expenseRepository.findByIdWithAssociations(id)
				.orElseThrow(() -> new ResourceNotFoundException("Expense with ID " + id + " not found"));
	}

	@Transactional
	public Expense updateExpense(Long id, ExpenseDTO expenseDTO) {
		Expense existingExpense = self.getExpenseById(id);
		existingExpense.setName(expenseDTO.getName());
		existingExpense.setAmount(expenseDTO.getAmount());
		existingExpense.setCurrency(expenseDTO.getCurrency());
		if (expenseDTO.getCategory() != null && !expenseDTO.getCategory().isBlank()) {
			Category category = categoryRepository.findByNameIgnoreCase(expenseDTO.getCategory())
					.orElseThrow(() -> new ResourceNotFoundException(CAT_PR + expenseDTO.getCategory() + CAT_PSF));
			existingExpense.setCategory(category);
		}
		if (expenseDTO.getTags() != null) {
			existingExpense.getTags().clear();
			existingExpense.getTags().addAll(resolveTags(new ArrayList<>(expenseDTO.getTags())));
		}
		return expenseRepository.save(existingExpense);
	}

	@Transactional
	public Expense updateExpensePartial(Long id, ExpenseUpdateDTO expenseUpdateDTO) {
		Expense existingExpense = self.getExpenseById(id);
		if (isNotBlank(expenseUpdateDTO.getName())) {
			existingExpense.setName(expenseUpdateDTO.getName());
		}
		if (expenseUpdateDTO.getAmount() != null) {
			existingExpense.setAmount(expenseUpdateDTO.getAmount());
		}
		if (isNotBlank(expenseUpdateDTO.getCurrency())) {
			existingExpense.setCurrency(expenseUpdateDTO.getCurrency());
		}
		if (isNotBlank(expenseUpdateDTO.getCategory())) {
			Category category = categoryRepository.findByNameIgnoreCase(expenseUpdateDTO.getCategory())
					.orElseThrow(() -> new ResourceNotFoundException(CAT_PR
							+ expenseUpdateDTO.getCategory()
							+ CAT_PSF));
			existingExpense.setCategory(category);
		}
		if (expenseUpdateDTO.getTags() != null) {
			existingExpense.getTags().clear();
			existingExpense.getTags().addAll(resolveTags(new ArrayList<>(expenseUpdateDTO.getTags())));
		}
		return expenseRepository.save(existingExpense);
	}

	@Transactional
	public void deleteExpense(Long id) {
		Expense expense = self.getExpenseById(id);
		expenseRepository.delete(expense);
	}

	@Transactional(readOnly = true)
	public List<Expense> getExpensesByCategory(String categoryName) {
		List<Expense> expenses = expenseRepository.findByCategoryName(categoryName);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found for category '" + categoryName + "'");
		}
		return expenses;
	}

	@Transactional(readOnly = true)
	public List<Expense> getExpensesByAmountRange(double min, double max) {
		List<Expense> expenses = expenseRepository.findByAmountRange(min, max);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found in amount range [" + min + ", " + max + "]");
		}
		return expenses;
	}

	@Transactional(readOnly = true)
	public List<Expense> searchByNamePart(String namePart) {
		List<Expense> expenses = expenseRepository.searchByNamePart(namePart);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found matching name part '" + namePart + "'");
		}
		return expenses;
	}

	@Transactional(readOnly = true)
	public List<Expense> getExpensesByTag(String tagName) {
		List<Expense> expenses = expenseRepository.findByTagName(tagName);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found for tag '" + tagName + "'");
		}
		return expenses;
	}

	@Transactional(readOnly = true)
	public Double getExpenseAmountInCurrency(Long expenseId, String targetCurrency) {
		Expense expense = self.getExpenseById(expenseId);
		double rate = exchangeRateService.getExchangeRate(expense.getCurrency(), targetCurrency);
		return expense.getAmount() * rate;
	}

	private List<Tag> resolveTags(List<String> tagNames) {
		List<Tag> tags = new ArrayList<>();
		for (String tagName : tagNames) {
			Tag tag = tagRepository.findByNameIgnoreCase(tagName)
					.orElseGet(() -> tagRepository.save(new Tag(tagName)));
			tags.add(tag);
		}
		return tags;
	}

	private boolean isNotBlank(String value) {
		return value != null && !value.isBlank();
	}
}
