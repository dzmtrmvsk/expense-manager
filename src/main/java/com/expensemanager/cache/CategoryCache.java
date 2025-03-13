package com.expensemanager.cache;

import com.expensemanager.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryCache extends LFUCache<Category> {

	public CategoryCache() {
		super(100);
	}
}
