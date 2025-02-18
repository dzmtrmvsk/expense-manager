package com.expensemanager.service;

import com.expensemanager.dao.ExpenseDao;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.model.Expense;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

	private final ExpenseDao expenseDao;

	@Autowired
	public ExpenseService(ExpenseDao expenseDao) {
		this.expenseDao = expenseDao;
	}

	public List<Expense> getAllExpenses() {
		return expenseDao.findAll();
	}

	public Expense getExpenseById(Long id) {
		Expense expense = expenseDao.findById(id);
		if (expense.getId() == null) {
			throw new ResourceNotFoundException("Expense with ID " + id + " not found");
		}
		return expense;
	}

	public List<Expense> getExpensesByCategory(String category) {
		List<Expense> expenses = expenseDao.getExpensesByCategory(category);
		if (expenses.isEmpty()) {
			throw new ResourceNotFoundException("No expenses found for category " + category);
		}
		return expenseDao.getExpensesByCategory(category);
	}
}
