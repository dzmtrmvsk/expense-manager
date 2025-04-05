package com.expensemanager.controller;

import com.expensemanager.dto.ExpenseDTO;
import com.expensemanager.dto.ExpenseUpdateDTO;
import com.expensemanager.model.Expense;
import com.expensemanager.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Expense API", description = "Operations on expenses")
public class ExpenseController {

	private final ExpenseService expenseService;

	@Autowired
	public ExpenseController(ExpenseService expenseService) {
		this.expenseService = expenseService;
	}

	@PostMapping
	@Operation(summary = "Create a new expense")
	public Expense createExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
		return expenseService.createExpense(expenseDTO);
	}

	@GetMapping
	@Operation(summary = "Get all expenses")
	public List<Expense> getAllExpenses() {
		return expenseService.getAllExpenses();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get expense by ID")
	public Expense getExpenseById(@PathVariable("id") Long id) {
		return expenseService.getExpenseById(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update expense by ID")
	public Expense updateExpense(@PathVariable("id") Long id, @Valid @RequestBody ExpenseDTO expenseDTO) {
		return expenseService.updateExpense(id, expenseDTO);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete expense by ID")
	public void deleteExpense(@PathVariable("id") Long id) {
		expenseService.deleteExpense(id);
	}

	@GetMapping("/search/category")
	@Operation(summary = "Find expenses by category name")
	public List<Expense> getExpensesByCategory(@RequestParam String category) {
		return expenseService.getExpensesByCategory(category);
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Partially update expense by ID")
	public Expense patchExpense(@PathVariable("id") Long id, @RequestBody ExpenseUpdateDTO expenseUpdateDTO) {
		return expenseService.updateExpensePartial(id, expenseUpdateDTO);
	}

	@GetMapping("/search/amount")
	@Operation(summary = "Find expenses by amount range")
	public List<Expense> getExpensesByAmountRange(@RequestParam double min, @RequestParam double max) {
		return expenseService.getExpensesByAmountRange(min, max);
	}

	@GetMapping("/search/name")
	@Operation(summary = "Find expenses by partial name")
	public List<Expense> searchByNamePart(@RequestParam String name) {
		return expenseService.searchByNamePart(name);
	}

	@GetMapping("/search/tag")
	@Operation(summary = "Find expenses by tag name")
	public List<Expense> getExpensesByTag(@RequestParam String tag) {
		return expenseService.getExpensesByTag(tag);
	}

	@GetMapping("/{id}/converted")
	@Operation(summary = "Get expense amount converted to another currency")
	public Double getExpenseConverted(@PathVariable("id") Long id, @RequestParam String currency) {
		return expenseService.getExpenseAmountInCurrency(id, currency);
	}

	@PostMapping("/bulk")
	@Operation(summary = "Create multiple expenses in bulk")
	public List<Expense> createExpensesBulk(@Valid @RequestBody List<ExpenseDTO> expenseDTOs) {
		return expenseService.createExpensesBulk(expenseDTOs);
	}
}
