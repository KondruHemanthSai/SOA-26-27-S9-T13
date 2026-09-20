package com.studentcentral.ai.agent.tools;

import com.studentcentral.ai.agent.AgentTool;
import com.studentcentral.ai.client.CourseServiceClient;
import com.studentcentral.ai.client.dto.ClientCourseDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CoursePrerequisitesTool implements AgentTool {

    private final CourseServiceClient courseServiceClient;

    public CoursePrerequisitesTool(CourseServiceClient courseServiceClient) {
        this.courseServiceClient = courseServiceClient;
    }

    @Override
    public String getName() {
        return "getCoursePrerequisites";
    }

    @Override
    public String getDescription() {
        return "Retrieves prerequisite requirements for a specific course code.";
    }

    @Override
    public String execute(String userId, String jwtToken, String parameter) {
        if (parameter == null || parameter.isBlank()) {
            return "Please provide a course code to check prerequisites.";
        }
        String cleanCode = parameter.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        Optional<ClientCourseDto> cOpt = courseServiceClient.getCourseByCode(cleanCode, jwtToken);
        if (cOpt.isEmpty()) {
            return "Course " + cleanCode + " was not found in the catalog.";
        }
        ClientCourseDto c = cOpt.get();
        if (c.getPrerequisiteCodes() == null || c.getPrerequisiteCodes().isEmpty()) {
            return "Course " + cleanCode + " (" + c.getCourseName() + ") has no prerequisites. It can be taken directly if seats are available.";
        }
        return "Prerequisites for " + cleanCode + ": " + String.join(", ", c.getPrerequisiteCodes());
    }
}
