package com.taskmanager.controller;

import com.taskmanager.dto.TaskDTOs;
import com.taskmanager.model.User;
import com.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<?> createTask(@PathVariable Long projectId,
                                        @RequestBody TaskDTOs.CreateTaskRequest request,
                                        @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.createTask(projectId, request, currentUser));
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<?> getTasksByProject(@PathVariable Long projectId,
                                               @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId, currentUser));
    }

    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long taskId,
                                          @RequestBody TaskDTOs.UpdateStatusRequest request,
                                          @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.updateStatus(taskId, request, currentUser));
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable Long taskId,
                                        @RequestBody TaskDTOs.CreateTaskRequest request,
                                        @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request, currentUser));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId,
                                        @AuthenticationPrincipal User currentUser) {
        taskService.deleteTask(taskId, currentUser);
        return ResponseEntity.ok(Map.of("message", "Task deleted"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.getDashboardStats(currentUser));
    }
}
