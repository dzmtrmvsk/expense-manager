package com.expensemanager.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterService {
	private final ConcurrentHashMap<String, LongAdder> visitCounters = new ConcurrentHashMap<>();

	public void recordVisit(String url) {
		LongAdder counter = visitCounters.computeIfAbsent(url, key -> new LongAdder());
		counter.increment();
	}

	public long getVisitCount(String url) {
		LongAdder counter = visitCounters.get(url);
		return (counter != null) ? counter.sum() : 0;
	}

	public Map<String, Long> getAllVisitCounts() {
		return visitCounters.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().sum()));
	}
}
