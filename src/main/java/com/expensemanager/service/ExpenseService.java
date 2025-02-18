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
			throw new ResourceNotFoundException("Расход с ID " + id + " не найден");
		}
		return expense;
	}

	public List<Expense> getExpensesByCategory(String category) {
		return expenseDao.getExpensesByCategory(category);
	}
}
