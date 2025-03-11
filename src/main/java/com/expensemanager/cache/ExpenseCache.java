package com.expensemanager.cache;

import com.expensemanager.model.Expense;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ExpenseCache {
	private final Map<Long, Expense> cache = new HashMap<>();

	public Expense get(Long id) {
		return cache.get(id);
	}

	public void put(Long id, Expense expense) {
		cache.put(id, expense);
	}

	public void remove(Long id) {
		cache.remove(id);
	}

	public void clear() {
		cache.clear();
	}
}
