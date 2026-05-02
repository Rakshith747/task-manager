package com.taskmanager.service;

import com.taskmanager.dto.ProjectDTOs;
import com.taskmanager.model.Project;
import com.taskmanager.model.User;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public ProjectDTOs.ProjectResponse createProject(ProjectDTOs.CreateProjectRequest request, User currentUser) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCreatedBy(currentUser);
        project.getMembers().add(currentUser); // creator is also a member

        projectRepository.save(project);
        return toResponse(project);
    }

    public List<ProjectDTOs.ProjectResponse> getAllProjects(User currentUser) {
        List<Project> projects;

        if (currentUser.getRole() == User.Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findByMembersContaining(currentUser);
        }

        return projects.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProjectDTOs.ProjectResponse getProject(Long id, User currentUser) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        // check access
        if (currentUser.getRole() != User.Role.ADMIN && !project.getMembers().contains(currentUser)) {
            throw new RuntimeException("Access denied");
        }

        return toResponse(project);
    }

    public void deleteProject(Long id, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admin can delete projects");
        }
        projectRepository.deleteById(id);
    }

    public ProjectDTOs.ProjectResponse addMember(Long projectId, Long userId, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admin can add members");
        }

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        User userToAdd = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        project.getMembers().add(userToAdd);
        projectRepository.save(project);

        return toResponse(project);
    }

    public ProjectDTOs.ProjectResponse removeMember(Long projectId, Long userId, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only admin can remove members");
        }

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        User userToRemove = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        project.getMembers().remove(userToRemove);
        projectRepository.save(project);

        return toResponse(project);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private ProjectDTOs.ProjectResponse toResponse(Project project) {
        ProjectDTOs.ProjectResponse resp = new ProjectDTOs.ProjectResponse();
        resp.setId(project.getId());
        resp.setName(project.getName());
        resp.setDescription(project.getDescription());
        resp.setCreatedByName(project.getCreatedBy() != null ? project.getCreatedBy().getName() : "N/A");
        resp.setMemberCount(project.getMembers().size());
        resp.setTaskCount(project.getTasks().size());

        List<ProjectDTOs.ProjectResponse.MemberInfo> memberInfos = new ArrayList<>();
        for (User member : project.getMembers()) {
            ProjectDTOs.ProjectResponse.MemberInfo info = new ProjectDTOs.ProjectResponse.MemberInfo();
            info.setId(member.getId());
            info.setName(member.getName());
            info.setEmail(member.getEmail());
            memberInfos.add(info);
        }
        resp.setMembers(memberInfos);

        return resp;
    }
}
