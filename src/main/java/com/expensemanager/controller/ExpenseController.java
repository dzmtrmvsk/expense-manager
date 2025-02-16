package com.expensemanager.controller;

import com.expensemanager.model.Expense;
import com.expensemanager.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
			summary = "Получить все расходы",
			description = "Возвращает список всех расходов"
	)
	public List<Expense> getAllExpenses() {
		return expenseService.getAllExpenses();
	}

	@GetMapping("/search")
	@Operation(
			summary = "Найти расходы по категории",
			description = "Фильтрует расходы по категории"
	)
	public List<Expense> getExpensesByCategory(@RequestParam String category) {
		return expenseService.getExpensesByCategory(category);
	}
}
