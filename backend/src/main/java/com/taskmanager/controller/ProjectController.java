package com.taskmanager.controller;

import com.taskmanager.dto.ProjectDTOs;
import com.taskmanager.model.User;
import com.taskmanager.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectDTOs.CreateProjectRequest request,
                                           @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.createProject(request, currentUser));
    }

    @GetMapping
    public ResponseEntity<?> getAllProjects(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.getAllProjects(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.getProject(id, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Project deleted"));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable Long id,
                                       @RequestBody ProjectDTOs.AddMemberRequest request,
                                       @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.addMember(id, request.getUserId(), currentUser));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long id,
                                          @PathVariable Long userId,
                                          @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.removeMember(id, userId, currentUser));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        List<Map<String, Object>> users = projectService.getAllUsers().stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("email", u.getEmail());
            map.put("role", u.getRole().name());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }
}
