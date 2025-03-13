package com.expensemanager.cache;

import com.expensemanager.model.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagCache extends LFUCache<Tag> {

	public TagCache() {
		super(100);
	}
}
