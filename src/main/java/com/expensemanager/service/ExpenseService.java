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
	private final ExpenseCache expenseCache;
	private final ExpenseService self;

	private static final String CAT_PR = "Category '";
	private static final String CAT_PSF = "' not found";

	public ExpenseService(ExpenseRepository expenseRepository,
	                      CategoryRepository categoryRepository,
	                      TagRepository tagRepository,
	                      ExchangeRateService exchangeRateService,
	                      ExpenseCache expenseCache,
	                      @Lazy ExpenseService self) {
		this.expenseRepository = expenseRepository;
		this.categoryRepository = categoryRepository;
		this.tagRepository = tagRepository;
		this.exchangeRateService = exchangeRateService;
		this.expenseCache = expenseCache;
		this.self = self;
	}

	private boolean isDTONull(ExpenseDTO expenseDTO) {
		return expenseDTO.getTags() != null;
	}

	@Transactional
	public Expense createExpense(ExpenseDTO expenseDTO) {
		Expense expense = buildExpenseFromDTO(expenseDTO);
		Expense saved = expenseRepository.save(expense);
		expenseCache.put(saved.getId(), saved);
		log.info("Expense with id {} created and cached", saved.getId());
		return saved;
	}

	@Transactional(readOnly = true)
	public List<Expense> getAllExpenses() {
		List<Expense> expenses = expenseRepository.findAllWithAssociations();
		for (Expense expense : expenses) {
			if (expenseCache.get(expense.getId()) == null) {
				expenseCache.put(expense.getId(), expense);
				log.info("Expense with id {} added to cache from getAllExpenses", expense.getId());
			} else {
				log.info("Expense with id {} already present in cache (getAllExpenses)", expense.getId());
			}
		}
		return expenses;
	}

	@Transactional(readOnly = true)
	public Expense getExpenseById(Long id) {
		Expense cachedExpense = expenseCache.get(id);
		if (cachedExpense != null) {
			log.info("Expense with id {} retrieved from cache", id);
			return cachedExpense;
		}
		Expense expense = expenseRepository.findByIdWithAssociations(id)
				.orElseThrow(() -> new ResourceNotFoundException("Expense with ID " + id + " not found"));
		expenseCache.put(id, expense);
		log.info("Expense with id {} retrieved from repository and cached", id);
		return expense;
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
		if (isDTONull(expenseDTO)) {
			existingExpense.getTags().clear();
			existingExpense.getTags().addAll(resolveTags(new ArrayList<>(expenseDTO.getTags())));
		}
		Expense updatedExpense = expenseRepository.save(existingExpense);
		expenseCache.put(id, updatedExpense);
		log.info("Expense with id {} updated and cache refreshed in updateExpense", id);
		return updatedExpense;
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
		Expense updatedExpense = expenseRepository.save(existingExpense);
		expenseCache.put(id, updatedExpense);
		log.info("Expense with id {} partially updated and cache refreshed", id);
		return updatedExpense;
	}

	@Transactional
	public void deleteExpense(Long id) {
		Expense expense = self.getExpenseById(id);
		expenseRepository.delete(expense);
		expenseCache.remove(id);
		log.info("Expense with id {} deleted and removed from cache", id);
	}

	@Transactional(readOnly = true)
	public List<Expense> getExpensesByCategory(String categoryName) {
		List<Expense> expenses = expenseRepository.findByCategoryName(categoryName);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found for category '" + categoryName + "'");
		}
		for (Expense expense : expenses) {
			if (expenseCache.get(expense.getId()) == null) {
				expenseCache.put(expense.getId(), expense);
				log.info("Expense with id {} added to cache from getExpensesByCategory", expense.getId());
			} else {
				log.info("Expense with id {} already in cache (getExpensesByCategory)", expense.getId());
			}
		}
		return expenses;
	}

	@Transactional(readOnly = true)
	public List<Expense> getExpensesByAmountRange(double min, double max) {
		List<Expense> expenses = expenseRepository.findByAmountRange(min, max);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found in amount range [" + min + ", " + max + "]");
		}
		for (Expense expense : expenses) {
			if (expenseCache.get(expense.getId()) == null) {
				expenseCache.put(expense.getId(), expense);
				log.info("Expense with id {} added to cache from getExpensesByAmountRange", expense.getId());
			} else {
				log.info("Expense with id {} already in cache (getExpensesByAmountRange)", expense.getId());
			}
		}
		return expenses;
	}

	@Transactional(readOnly = true)
	public List<Expense> searchByNamePart(String namePart) {
		List<Expense> expenses = expenseRepository.searchByNamePart(namePart);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found matching name part '" + namePart + "'");
		}
		for (Expense expense : expenses) {
			if (expenseCache.get(expense.getId()) == null) {
				expenseCache.put(expense.getId(), expense);
				log.info("Expense with id {} added to cache from searchByNamePart", expense.getId());
			} else {
				log.info("Expense with id {} already in cache (searchByNamePart)", expense.getId());
			}
		}
		return expenses;
	}

	@Transactional(readOnly = true)
	public List<Expense> getExpensesByTag(String tagName) {
		List<Expense> expenses = expenseRepository.findByTagName(tagName);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found for tag '" + tagName + "'");
		}
		for (Expense expense : expenses) {
			if (expenseCache.get(expense.getId()) == null) {
				expenseCache.put(expense.getId(), expense);
				log.info("Expense with id {} added to cache from getExpensesByTag", expense.getId());
			} else {
				log.info("Expense with id {} already in cache (getExpensesByTag)", expense.getId());
			}
		}
		return expenses;
	}

	@Transactional(readOnly = true)
	public Double getExpenseAmountInCurrency(Long expenseId, String targetCurrency) {
		Expense expense = self.getExpenseById(expenseId);
		double rate = exchangeRateService.getExchangeRate(expense.getCurrency(), targetCurrency);
		log.info("Converted expense id {} from {} to {} with rate {}", expenseId, expense.getCurrency(), targetCurrency, rate);
		return expense.getAmount() * rate;
	}

	private List<Tag> resolveTags(List<String> tagNames) {
		List<Tag> tags = new ArrayList<>();
		for (String tagName : tagNames) {
			Tag tag = tagRepository.findByNameIgnoreCase(tagName)
					.orElseGet(() -> {
						Tag newTag = new Tag(tagName);
						Tag savedTag = tagRepository.save(newTag);
						log.info("New tag with name '{}' created and saved", tagName);
						return savedTag;
					});
			tags.add(tag);
		}
		return tags;
	}

	private boolean isNotBlank(String value) {
		return value != null && !value.isBlank();
	}

	@Transactional
	public List<Expense> createExpensesBulk(List<ExpenseDTO> expenseDTOs) {
		List<Expense> expenses = expenseDTOs.stream()
				.map(this::buildExpenseFromDTO)
				.toList();

		List<Expense> savedExpenses = expenseRepository.saveAll(expenses);

		savedExpenses.forEach(exp -> {
			expenseCache.put(exp.getId(), exp);
			log.info("Expense with id {} created in bulk and cached", exp.getId());
		});

		return savedExpenses;
	}

	private Expense buildExpenseFromDTO(ExpenseDTO dto) {
		Category category = categoryRepository
				.findByNameIgnoreCase(dto.getCategory())
				.orElseThrow(() -> new ResourceNotFoundException(
						CAT_PR + dto.getCategory() + CAT_PSF));

		Expense e = new Expense();
		e.setName(dto.getName());
		e.setAmount(dto.getAmount());
		e.setCurrency(dto.getCurrency());
		e.setCategory(category);

		if (dto.getTags() != null && !dto.getTags().isEmpty()) {
			e.getTags().addAll(resolveTags(new ArrayList<>(dto.getTags())));
		}
		return e;
	}
}
