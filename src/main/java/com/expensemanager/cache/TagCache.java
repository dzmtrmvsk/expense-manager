package com.expensemanager.cache;

import com.expensemanager.model.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TagCache {

	private final Map<Long, Tag> cache = new HashMap<>();

	public Tag get(Long id) {
		return cache.get(id);
	}

	public void put(Long id, Tag tag) {
		cache.put(id, tag);
	}

	public void remove(Long id) {
		cache.remove(id);
	}

	public void clear() {
		cache.clear();
	}
}
