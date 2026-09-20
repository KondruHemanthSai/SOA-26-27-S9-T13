package com.studentcentral.ai.agent.tools;

import com.studentcentral.ai.agent.AgentTool;
import com.studentcentral.ai.client.CourseServiceClient;
import com.studentcentral.ai.client.dto.ClientCourseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AvailableCoursesTool implements AgentTool {

    private final CourseServiceClient courseServiceClient;

    public AvailableCoursesTool(CourseServiceClient courseServiceClient) {
        this.courseServiceClient = courseServiceClient;
    }

    @Override
    public String getName() {
        return "getAvailableCourses";
    }

    @Override
    public String getDescription() {
        return "Retrieves the active catalog of courses offered in Student Central with department, credits, and seat availability.";
    }

    @Override
    public String execute(String userId, String jwtToken, String parameter) {
        List<ClientCourseDto> courses = courseServiceClient.getAllCourses(jwtToken);
        if (courses.isEmpty()) {
            return "No courses are currently available in the catalog.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Available Courses (").append(courses.size()).append("):\n");
        for (ClientCourseDto c : courses) {
            sb.append(String.format("- %s: %s (%s, %d credits, %d/%d seats available, status: %s)\n",
                    c.getCourseCode(), c.getCourseName(), c.getDepartment(),
                    c.getCredits(), c.getAvailableSeats() != null ? c.getAvailableSeats() : 0,
                    c.getCapacity() != null ? c.getCapacity() : 0, c.getStatus()));
        }
        return sb.toString();
    }
}
