package com.expensemanager.controller;

import com.expensemanager.dto.LogTaskStatusDTO;
import com.expensemanager.model.LogTask;
import com.expensemanager.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/logs")
@Tag(name = "Log API", description = "API for work with logs")
@RequiredArgsConstructor
public class LogController {

	private final LogService logService;

	@GetMapping("/download")
	@Operation(summary = "Get sync logfile for date dd.mm.yyyy")
	public ResponseEntity<Resource> downloadLogFile(@RequestParam String date) {
		Resource resource = logService.getLogFileForDate(date);
		String downloadFileName = logService.getDownloadFileName(date);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
				.body(resource);
	}

	@PostMapping("/createAsync")
	@Operation(summary = "Async creating of LogFile. Return taskId")
	public ResponseEntity<String> createLogFileAsync(@RequestParam String date) {
		String taskId = logService.createLogFileAsync(date);
		return ResponseEntity.ok(taskId);
	}

	@GetMapping("/status")
	@Operation(summary = "Get task status by taskId")
	public ResponseEntity<LogTaskStatusDTO> getTaskStatus(@RequestParam String taskId) {
		LogTask logTask = logService.getTask(taskId);
		LogTaskStatusDTO statusDTO = new LogTaskStatusDTO(
				logTask.getTaskId(),
				logTask.getDate(),
				logTask.getStatus(),
				logTask.getErrorMessage()
		);
		return ResponseEntity.ok(statusDTO);
	}

	@GetMapping("/downloadAsync")
	@Operation(summary = "Download asynchronously created file")
	public ResponseEntity<Resource> downloadLogFileAsync(@RequestParam String taskId) {
		Resource resource = logService.getLogFileByTask(taskId);
		LogTask logTask = logService.getTask(taskId);
		String downloadFileName = logService.getDownloadFileName(logTask.getDate());
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
				.body(resource);
	}
}
