package com.studentcentral.ai.agent.tools;

import com.studentcentral.ai.agent.AgentTool;
import com.studentcentral.ai.client.RegistrationServiceClient;
import com.studentcentral.ai.client.dto.ClientRegistrationDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegisteredCoursesTool implements AgentTool {

    private final RegistrationServiceClient registrationServiceClient;

    public RegisteredCoursesTool(RegistrationServiceClient registrationServiceClient) {
        this.registrationServiceClient = registrationServiceClient;
    }

    @Override
    public String getName() {
        return "getRegisteredCourses";
    }

    @Override
    public String getDescription() {
        return "Retrieves the courses the authenticated student is currently registered for, including course codes, names, credits, and total registered credits.";
    }

    @Override
    public String execute(String userId, String jwtToken, String parameter) {
        List<ClientRegistrationDto> courses = registrationServiceClient.getMyCourses(userId, jwtToken);
        if (courses.isEmpty()) {
            return "You are currently not registered for any courses.";
        }
        StringBuilder sb = new StringBuilder();
        int totalCredits = 0;
        sb.append("Registered Courses (").append(courses.size()).append("):\n");
        for (ClientRegistrationDto c : courses) {
            sb.append(String.format("- %s: %s (%d credits, status: %s)\n",
                    c.getCourseCode(), c.getCourseName(), c.getCredits(), c.getStatus()));
            if ("REGISTERED".equalsIgnoreCase(c.getStatus()) && c.getCredits() != null) {
                totalCredits += c.getCredits();
            }
        }
        sb.append("Total Registered Credits: ").append(totalCredits).append(" / 18 max allowable credits.");
        return sb.toString();
    }
}
