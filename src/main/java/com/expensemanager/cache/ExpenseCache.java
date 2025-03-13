package com.expensemanager.cache;

import com.expensemanager.model.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseCache extends LFUCache<Expense> {

	public ExpenseCache() {
		super(100);
	}
}
