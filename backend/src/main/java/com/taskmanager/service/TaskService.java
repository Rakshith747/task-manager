package com.taskmanager.service;

import com.taskmanager.dto.TaskDTOs;
import com.taskmanager.model.Project;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public TaskDTOs.TaskResponse createTask(Long projectId, TaskDTOs.CreateTaskRequest request, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admin can create tasks");
        }

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        User assignee = null;
        if (request.getAssignedToId() != null) {
            assignee = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setProject(project);
        task.setAssignedTo(assignee);
        task.setStatus(Task.Status.TODO);

        taskRepository.save(task);
        return toResponse(task);
    }

    public List<TaskDTOs.TaskResponse> getTasksByProject(Long projectId, User currentUser) {
        // check project exists
        projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        // members can only see their tasks
        if (currentUser.getRole() == User.Role.MEMBER) {
            tasks = tasks.stream()
                .filter(t -> t.getAssignedTo() != null && t.getAssignedTo().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        }

        return tasks.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TaskDTOs.TaskResponse updateStatus(Long taskId, TaskDTOs.UpdateStatusRequest request, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        // members can only update their own tasks
        if (currentUser.getRole() == User.Role.MEMBER) {
            if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentUser.getId())) {
                throw new RuntimeException("You can only update your own tasks");
            }
        }

        task.setStatus(Task.Status.valueOf(request.getStatus()));
        taskRepository.save(task);

        return toResponse(task);
    }

    public void deleteTask(Long taskId, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admin can delete tasks");
        }
        taskRepository.deleteById(taskId);
    }

    public TaskDTOs.TaskResponse updateTask(Long taskId, TaskDTOs.CreateTaskRequest request, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admin can edit tasks");
        }

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());

        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            task.setAssignedTo(assignee);
        }

        taskRepository.save(task);
        return toResponse(task);
    }

    // dashboard data
    public java.util.Map<String, Long> getDashboardStats(User currentUser) {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        LocalDate today = LocalDate.now();

        long total = taskRepository.countByAssignedTo(currentUser);
        long done = taskRepository.countByAssignedToAndStatus(currentUser, Task.Status.DONE);
        long overdue = taskRepository.countByAssignedToAndDueDateBeforeAndStatusNot(currentUser, today, Task.Status.DONE);

        stats.put("total", total);
        stats.put("completed", done);
        stats.put("pending", total - done);
        stats.put("overdue", overdue);

        return stats;
    }

    private TaskDTOs.TaskResponse toResponse(Task task) {
        TaskDTOs.TaskResponse resp = new TaskDTOs.TaskResponse();
        resp.setId(task.getId());
        resp.setTitle(task.getTitle());
        resp.setDescription(task.getDescription());
        resp.setStatus(task.getStatus().name());
        resp.setDueDate(task.getDueDate());
        resp.setProjectId(task.getProject().getId());
        resp.setProjectName(task.getProject().getName());

        if (task.getAssignedTo() != null) {
            resp.setAssignedToId(task.getAssignedTo().getId());
            resp.setAssignedToName(task.getAssignedTo().getName());
        }

        resp.setOverdue(task.getDueDate() != null
            && task.getDueDate().isBefore(LocalDate.now())
            && task.getStatus() != Task.Status.DONE);

        return resp;
    }
}
