package com.expensemanager.cache;

import com.expensemanager.model.Category;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CategoryCache {

	private final Map<Long, Category> cache = new HashMap<>();

	public Category get(Long id) {
		return cache.get(id);
	}

	public void put(Long id, Category category) {
		cache.put(id, category);
	}

	public void remove(Long id) {
		cache.remove(id);
	}

	public void clear() {
		cache.clear();
	}
}
