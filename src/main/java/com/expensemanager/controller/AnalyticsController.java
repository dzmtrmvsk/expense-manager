package com.expensemanager.controller;

import com.expensemanager.service.VisitCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics API", description = "Сбор аналитики посещений по всему приложению")
@RequiredArgsConstructor
public class AnalyticsController {

	private final VisitCounterService visitCounterService;

	@GetMapping("/visits")
	@Operation(summary = "Получить сводную аналитику количества посещений по URL")
	public Map<String, Long> getAllVisits() {
		return visitCounterService.getAllVisitCounts();
	}
}
