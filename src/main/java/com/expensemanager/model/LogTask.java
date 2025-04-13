package com.expensemanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

@Data
@NoArgsConstructor
public class LogTask {

	private String taskId;
	private String date;
	private TaskStatus status;
	private Resource resource;
	private String errorMessage;

	public LogTask(String taskId, String date, TaskStatus status) {
		this.taskId = taskId;
		this.date = date;
		this.status = status;
	}
}
