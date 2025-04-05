package com.expensemanager.service;

import com.expensemanager.exception.InvalidInputException;
import com.expensemanager.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class LogServiceTest {

	private LogService logService;
	private static final String TEST_LOG_DIR = "logs";
	private static final Path LOG_FILE_PATH = Paths.get(TEST_LOG_DIR, "app.log");

	@BeforeEach
	void setUp() throws IOException {
		logService = new LogService();
		Files.createDirectories(LOG_FILE_PATH.getParent());
		Files.deleteIfExists(LOG_FILE_PATH);
	}

	@Test
	void testGetDownloadFileName_ValidDate() {
		String result = logService.getDownloadFileName("02.04.2024");
		assertThat(result).isEqualTo("app-2024-04-02.log");
	}

	@Test
	void testGetDownloadFileName_InvalidFormat() {
		assertThatThrownBy(() -> logService.getDownloadFileName("2024-04-02"))
				.isInstanceOf(InvalidInputException.class)
				.hasMessageContaining("Incorrect date format");
	}

	@Test
	void testGetLogFileForDate_FileMissing() {
		assertThatThrownBy(() -> logService.getLogFileForDate("05.04.2024"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Log file does not exist");
	}

	@Test
	void testGetLogFileForDate_NoLinesMatching() throws IOException {
		List<String> lines = List.of(
				"2024-04-04 INFO Something happened",
				"2024-04-03 ERROR Something failed"
		);
		Files.write(LOG_FILE_PATH, lines, StandardCharsets.UTF_8);

		assertThatThrownBy(() -> logService.getLogFileForDate("05.04.2024"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("No logs found for this date");
	}

	@Test
	void testGetLogFileForDate_MatchingLinesFound() throws IOException {
		List<String> lines = List.of(
				"2024-04-05 INFO Application started",
				"2024-04-05 WARN Low disk space",
				"2024-04-04 INFO Other log"
		);
		Files.write(LOG_FILE_PATH, lines, StandardCharsets.UTF_8);

		Resource result = logService.getLogFileForDate("05.04.2024");
		String content = new String(result.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

		assertThat(content)
				.contains("2024-04-05 INFO Application started", "2024-04-05 WARN Low disk space")
				.doesNotContain("2024-04-04 INFO Other log");

	}

	@Test
	void testGetLogFileForDate_InvalidDateFormat() {
		assertThatThrownBy(() -> logService.getLogFileForDate("2024-04-05"))
				.isInstanceOf(InvalidInputException.class)
				.hasMessageContaining("Incorrect date format");
	}

}
