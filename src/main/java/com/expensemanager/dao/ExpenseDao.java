package com.expensemanager.dao;

import com.expensemanager.model.Expense;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ExpenseDao {

	private final List<Expense> mockExpenses = new ArrayList<>();

	public ExpenseDao() {
		mockExpenses.add(new Expense(1L, "Groceries", "Food", 45.50));
		mockExpenses.add(new Expense(2L, "Cinema Ticket", "Entertainment", 12.00));
		mockExpenses.add(new Expense(3L, "Bus Pass", "Transport", 20.00));
		mockExpenses.add(new Expense(4L, "Restaurant", "Food", 30.00));
	}

	public List<Expense> findAll() {
		return mockExpenses;
	}

	public Expense findById(Long id) {
		for (Expense expense : mockExpenses) {
			if (expense.getId().equals(id)) {
				return expense;
			}
		}
		return new Expense();
	}

	public List<Expense> getExpensesByCategory(String category) {
		List<Expense> filteredExpenses = new ArrayList<>();

		for (Expense expense : mockExpenses) {
			if (expense.getCategory().equalsIgnoreCase(category)) {
				filteredExpenses.add(expense);
			}
		}

		return filteredExpenses;
	}
}
