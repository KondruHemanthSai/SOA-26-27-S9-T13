package com.studentcentral.ai.agent.tools;

import com.studentcentral.ai.agent.AgentTool;
import com.studentcentral.ai.client.CourseServiceClient;
import com.studentcentral.ai.client.dto.ClientCourseDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CourseDetailsTool implements AgentTool {

    private final CourseServiceClient courseServiceClient;

    public CourseDetailsTool(CourseServiceClient courseServiceClient) {
        this.courseServiceClient = courseServiceClient;
    }

    @Override
    public String getName() {
        return "getCourseDetails";
    }

    @Override
    public String getDescription() {
        return "Retrieves detailed information for a specific course code (e.g. CS101, CS201) including title, credits, description, faculty, capacity, and available seats.";
    }

    @Override
    public String execute(String userId, String jwtToken, String parameter) {
        if (parameter == null || parameter.isBlank()) {
            return "Please provide a valid course code.";
        }
        String cleanCode = parameter.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        Optional<ClientCourseDto> cOpt = courseServiceClient.getCourseByCode(cleanCode, jwtToken);
        if (cOpt.isEmpty()) {
            return "Course " + cleanCode + " was not found in the course catalog. The backend is the authoritative source of truth.";
        }
        ClientCourseDto c = cOpt.get();
        return String.format("Course: %s - %s\nDepartment: %s\nCredits: %d\nAvailable Seats: %d / %d\nStatus: %s\nDescription: %s\nFaculty: %s",
                c.getCourseCode(), c.getCourseName(), c.getDepartment(), c.getCredits(),
                c.getAvailableSeats() != null ? c.getAvailableSeats() : 0,
                c.getCapacity() != null ? c.getCapacity() : 0,
                c.getStatus(), c.getDescription() != null ? c.getDescription() : "None",
                c.getFaculty() != null ? c.getFaculty() : "TBA");
    }
}
