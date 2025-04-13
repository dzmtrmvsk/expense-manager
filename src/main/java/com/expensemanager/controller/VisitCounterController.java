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
@RequestMapping("/api/visits")
@Tag(name = "Visit Counter API", description = "Visit counter URL")
@RequiredArgsConstructor
public class VisitCounterController {

	private final VisitCounterService visitCounterService;

	@GetMapping
	@Operation(summary = "Get url statistics")
	public Map<String, Long> getAllVisits() {
		return visitCounterService.getAllVisitCounts();
	}
}
