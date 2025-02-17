package com.expensemanager.controller;

import com.expensemanager.model.Expense;
import com.expensemanager.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

	private final ExpenseService expenseService;

	@Autowired
	public ExpenseController(ExpenseService expenseService) {
		this.expenseService = expenseService;
	}

	@GetMapping
	@Operation(
			summary = "Get all expenses",
			description = "Return list of all expenses"
	)
	public List<Expense> getAllExpenses() {
		return expenseService.getAllExpenses();
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Get expense by ID",
			description = "Get expense by its ID"
	)
	public Expense getExpenseById(@PathVariable Long id) {
		return expenseService.getExpenseById(id);
	}

	@GetMapping("/search")
	@Operation(
			summary = "Find expenses by category",
			description = "Filter expenses by their category"
	)
	public List<Expense> getExpensesByCategoryQuery(@RequestParam String category) {
		return expenseService.getExpensesByCategory(category);
	}
}
