package com.studentcentral.ai.agent.tools;

import com.studentcentral.ai.agent.AgentTool;
import com.studentcentral.ai.client.StudentServiceClient;
import com.studentcentral.ai.client.dto.ClientStudentDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StudentProfileTool implements AgentTool {

    private final StudentServiceClient studentServiceClient;

    public StudentProfileTool(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }

    @Override
    public String getName() {
        return "getStudentProfile";
    }

    @Override
    public String getDescription() {
        return "Retrieves the authenticated student's profile, program, department, semester, GPA, and completed credits.";
    }

    @Override
    public String execute(String userId, String jwtToken, String parameter) {
        Optional<ClientStudentDto> profileOpt = studentServiceClient.getProfile(userId, jwtToken);
        if (profileOpt.isEmpty()) {
            return "Student profile is not yet initialized in the system.";
        }
        ClientStudentDto p = profileOpt.get();
        return String.format("Student: %s %s (ID: %s), Program: %s, Department: %s, Semester: %s, GPA: %s, Completed Credits: %s",
                p.getFirstName(), p.getLastName(), p.getStudentId(), p.getProgram(), p.getDepartment(),
                p.getSemester(), p.getGpa() != null ? p.getGpa().toString() : "Not recorded",
                p.getCompletedCredits() != null ? p.getCompletedCredits().toString() : "0");
    }
}
