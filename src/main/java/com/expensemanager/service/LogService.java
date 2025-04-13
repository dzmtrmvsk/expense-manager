package com.expensemanager.service;

import com.expensemanager.exception.InvalidInputException;
import com.expensemanager.exception.LoggingException;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.model.LogTask;
import com.expensemanager.model.TaskStatus;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LogService {

	private static final String LOGS_DIR = "logs";
	private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	public static final DateTimeFormatter LOG_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final ConcurrentHashMap<String, LogTask> taskMap = new ConcurrentHashMap<>();

	public Resource getLogFileForDate(String date) {
		LocalDate parsedDate = parseDate(date);
		String formattedDate = parsedDate.format(LOG_DATE_FORMATTER);

		Path logFilePath = Paths.get(LOGS_DIR, "app.log");
		if (!Files.exists(logFilePath)) {
			throw new ResourceNotFoundException("Log file does not exist.");
		}

		List<String> filteredLines = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(logFilePath, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(formattedDate)) {
					filteredLines.add(line);
				}
			}
		} catch (IOException ex) {
			log.error("Error reading log file", ex);
			throw new LoggingException("Error reading log file");
		}

		if (filteredLines.isEmpty()) {
			throw new ResourceNotFoundException("No logs found for date: " + date);
		}

		String fileContent = String.join(System.lineSeparator(), filteredLines);
		return new ByteArrayResource(fileContent.getBytes(StandardCharsets.UTF_8));
	}

	public String getDownloadFileName(String date) {
		LocalDate parsedDate = parseDate(date);
		return String.format("app-%s.log", parsedDate.format(LOG_DATE_FORMATTER));
	}

	private LocalDate parseDate(String date) {
		try {
			return LocalDate.parse(date, INPUT_DATE_FORMATTER);
		} catch (DateTimeParseException ex) {
			throw new InvalidInputException("Incorrect date format. Use dd.MM.yyyy.");
		}
	}

	public String createLogFileAsync(String date) {
		String taskId = UUID.randomUUID().toString();
		LogTask logTask = new LogTask(taskId, date, TaskStatus.IN_PROGRESS);
		taskMap.put(taskId, logTask);
		log.info("Task {} created for date {}", taskId, date);

		executor.submit(() -> {
			try {
				Resource resource = getLogFileForDate(date);
				logTask.setResource(resource);
				logTask.setStatus(TaskStatus.COMPLETED);
				log.info("Task {} completed", taskId);
			} catch (Exception ex) {
				logTask.setErrorMessage(ex.getMessage());
				logTask.setStatus(TaskStatus.FAILED);
				log.error("Task {} failed: {}", taskId, ex.getMessage());
			}
		});
		return taskId;
	}

	public LogTask getTask(String taskId) {
		LogTask logTask = taskMap.get(taskId);
		if (logTask == null) {
			throw new ResourceNotFoundException("Task not found for id: " + taskId);
		}
		return logTask;
	}

	public Resource getLogFileByTask(String taskId) {
		LogTask logTask = getTask(taskId);
		if (logTask.getStatus() != TaskStatus.COMPLETED) {
			throw new LoggingException("Log file is not ready yet. Current status: " + logTask.getStatus());
		}
		return logTask.getResource();
	}
}
