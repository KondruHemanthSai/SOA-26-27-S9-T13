package com.studentcentral.ai.agent.tools;

import com.studentcentral.ai.agent.AgentTool;
import com.studentcentral.ai.client.AdmissionServiceClient;
import com.studentcentral.ai.client.dto.ClientAdmissionDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdmissionStatusTool implements AgentTool {

    private final AdmissionServiceClient admissionServiceClient;

    public AdmissionStatusTool(AdmissionServiceClient admissionServiceClient) {
        this.admissionServiceClient = admissionServiceClient;
    }

    @Override
    public String getName() {
        return "getAdmissionStatus";
    }

    @Override
    public String getDescription() {
        return "Retrieves the authenticated student's admission application status, remarks, and verification documents.";
    }

    @Override
    public String execute(String userId, String jwtToken, String parameter) {
        Optional<ClientAdmissionDto> appOpt = admissionServiceClient.getMyApplication(userId, jwtToken);
        if (appOpt.isEmpty()) {
            return "No admission application found for this user account.";
        }
        ClientAdmissionDto app = appOpt.get();
        return String.format("Admission Application ID: %s, Status: %s, Program: %s, Remarks: %s, Documents Submitted: %d",
                app.getApplicationId(), app.getStatus(), app.getProgram(),
                app.getRemarks() != null ? app.getRemarks() : "None",
                app.getDocuments() != null ? app.getDocuments().size() : 0);
    }
}
