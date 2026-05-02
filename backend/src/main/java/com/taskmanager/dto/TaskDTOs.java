package com.taskmanager.dto;

import lombok.Data;

import java.time.LocalDate;

public class TaskDTOs {

    @Data
    public static class CreateTaskRequest {
        private String title;
        private String description;
        private LocalDate dueDate;
        private Long assignedToId;
    }

    @Data
    public static class UpdateStatusRequest {
        private String status; // TODO, IN_PROGRESS, DONE
    }

    @Data
    public static class TaskResponse {
        private Long id;
        private String title;
        private String description;
        private String status;
        private LocalDate dueDate;
        private Long projectId;
        private String projectName;
        private Long assignedToId;
        private String assignedToName;
        private boolean overdue;
    }
}
