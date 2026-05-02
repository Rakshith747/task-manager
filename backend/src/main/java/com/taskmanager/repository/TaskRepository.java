package com.taskmanager.repository;

import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByAssignedTo(User user);
    List<Task> findByAssignedToAndStatus(User user, Task.Status status);
    List<Task> findByAssignedToAndDueDateBeforeAndStatusNot(User user, LocalDate date, Task.Status status);
    long countByAssignedTo(User user);
    long countByAssignedToAndStatus(User user, Task.Status status);
    long countByAssignedToAndDueDateBeforeAndStatusNot(User user, LocalDate date, Task.Status status);
}
