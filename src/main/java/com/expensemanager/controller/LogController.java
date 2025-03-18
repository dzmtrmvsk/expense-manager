package com.expensemanager.controller;

import com.expensemanager.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/logs")
@Tag(name = "Log API", description = "Get API Logs")
public class LogController {

	private final LogService logService;

	public LogController(LogService logService) {
		this.logService = logService;
	}

	@GetMapping("/download")
	@Operation(summary = "Create and download log file for current date")
	public ResponseEntity<Resource> downloadLogFile(@RequestParam String date) {
		Resource resource = logService.getLogFileForDate(date);
		String downloadFileName = logService.getDownloadFileName(date);

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
				.body(resource);
	}
}
