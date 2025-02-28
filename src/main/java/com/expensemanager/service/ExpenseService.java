package com.expensemanager.service;

import com.expensemanager.dto.ExpenseDTO;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.integration.ExchangeRateService;
import com.expensemanager.model.Category;
import com.expensemanager.model.Expense;
import com.expensemanager.model.Tag;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.TagRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExpenseService {

	private final ExpenseRepository expenseRepository;
	private final CategoryRepository categoryRepository;
	private final TagRepository tagRepository;
	private final ExchangeRateService exchangeRateService;

	@Autowired
	public ExpenseService(ExpenseRepository expenseRepository,
	                      CategoryRepository categoryRepository,
	                      TagRepository tagRepository,
	                      ExchangeRateService exchangeRateService) {
		this.expenseRepository = expenseRepository;
		this.categoryRepository = categoryRepository;
		this.tagRepository = tagRepository;
		this.exchangeRateService = exchangeRateService;
	}

	public Expense createExpense(ExpenseDTO expenseDTO) {
		log.info("Creating expense name={}, category={}, tags={}",
				expenseDTO.getName(), expenseDTO.getCategory(), expenseDTO.getTags());

		Category category = categoryRepository.findByNameIgnoreCase(expenseDTO.getCategory())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Category '" + expenseDTO.getCategory() + "' not found")
				);

		Expense expense = new Expense();
		expense.setName(expenseDTO.getName());
		expense.setAmount(expenseDTO.getAmount());
		expense.setCurrency(expenseDTO.getCurrency());
		expense.setCategory(category);

		if (expenseDTO.getTags() != null && !expenseDTO.getTags().isEmpty()) {
			expenseDTO.getTags().forEach(tagName -> {
				Tag tag = tagRepository.findByNameIgnoreCase(tagName)
						.orElseGet(() -> tagRepository.save(new Tag(tagName)));
				expense.getTags().add(tag);
				tag.getExpenses().add(expense);
			});
		}
		return expenseRepository.save(expense);
	}

	public List<Expense> getAllExpenses() {
		return expenseRepository.findAllWithAssociations();
	}

	public Expense getExpenseById(Long id) {
		return expenseRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Expense with ID " + id + " not found"));
	}

	public Expense updateExpense(Long id, ExpenseDTO expenseDTO) {
		Expense existingExpense = getExpenseById(id);

		log.info("Updating expense id={}, newName={}, newCategory={}, newTags={}",
				id, expenseDTO.getName(), expenseDTO.getCategory(), expenseDTO.getTags());

		existingExpense.setName(expenseDTO.getName());
		existingExpense.setAmount(expenseDTO.getAmount());
		existingExpense.setCurrency(expenseDTO.getCurrency());

		if (expenseDTO.getCategory() != null && !expenseDTO.getCategory().isBlank()) {
			Category category = categoryRepository.findByNameIgnoreCase(expenseDTO.getCategory())
					.orElseThrow(() -> new ResourceNotFoundException(
							"Category '" + expenseDTO.getCategory() + "' not found")
					);
			existingExpense.setCategory(category);
		}

		if (expenseDTO.getTags() != null) {
			existingExpense.getTags().forEach(tag -> tag.getExpenses().remove(existingExpense));
			existingExpense.getTags().clear();

			expenseDTO.getTags().forEach(tagName -> {
				Tag tag = tagRepository.findByNameIgnoreCase(tagName)
						.orElseGet(() -> tagRepository.save(new Tag(tagName)));
				existingExpense.getTags().add(tag);
				tag.getExpenses().add(existingExpense);
			});
		}

		return expenseRepository.save(existingExpense);
	}

	public void deleteExpense(Long id) {
		Expense expense = getExpenseById(id);
		log.warn("Deleting expense id={}", id);
		expense.getTags().forEach(tag -> tag.getExpenses().remove(expense));
		expenseRepository.delete(expense);
	}

	public List<Expense> getExpensesByCategory(String categoryName) {
		List<Expense> expenses = expenseRepository.findByCategoryName(categoryName);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found for category '" + categoryName + "'");
		}
		return expenses;
	}

	public List<Expense> getExpensesByAmountRange(double min, double max) {
		List<Expense> expenses = expenseRepository.findByAmountRange(min, max);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found in amount range [" + min + ", " + max + "]");
		}
		return expenses;
	}

	public List<Expense> searchByNamePart(String namePart) {
		List<Expense> expenses = expenseRepository.searchByNamePart(namePart);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found matching name part '" + namePart + "'");
		}
		return expenses;
	}

	public List<Expense> getExpensesByTag(String tagName) {
		List<Expense> expenses = expenseRepository.findByTagName(tagName);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found for tag '" + tagName + "'");
		}
		return expenses;
	}

	public Double getExpenseAmountInCurrency(Long expenseId, String targetCurrency) {
		Expense expense = getExpenseById(expenseId);
		double rate = exchangeRateService.getExchangeRate(expense.getCurrency(), targetCurrency);
		return expense.getAmount() * rate;
	}
}
