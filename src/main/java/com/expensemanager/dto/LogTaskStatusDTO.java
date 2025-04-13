package com.expensemanager.dto;

import com.expensemanager.model.TaskStatus;
import lombok.Data;

@Data
public class LogTaskStatusDTO {

	private String taskId;
	private String date;
	private TaskStatus status;
	private String errorMessage;

	public LogTaskStatusDTO(String taskId, String date, TaskStatus status, String errorMessage) {
		this.taskId = taskId;
		this.date = date;
		this.status = status;
		this.errorMessage = errorMessage;
	}
}
