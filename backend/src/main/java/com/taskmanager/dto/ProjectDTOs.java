package com.taskmanager.dto;

import lombok.Data;

import java.util.List;

public class ProjectDTOs {

    @Data
    public static class CreateProjectRequest {
        private String name;
        private String description;
    }

    @Data
    public static class ProjectResponse {
        private Long id;
        private String name;
        private String description;
        private String createdByName;
        private int memberCount;
        private int taskCount;
        private List<MemberInfo> members;

        @Data
        public static class MemberInfo {
            private Long id;
            private String name;
            private String email;
        }
    }

    @Data
    public static class AddMemberRequest {
        private Long userId;
    }
}
