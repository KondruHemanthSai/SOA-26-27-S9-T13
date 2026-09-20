package com.studentcentral.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientAdmissionDto {
    private String id;
    private String applicationId;
    private String studentId;
    private String userId;
    private String program;
    private String department;
    private String status;
    private String remarks;
    private List<ClientDocumentDto> documents = new ArrayList<>();

    public ClientAdmissionDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public List<ClientDocumentDto> getDocuments() { return documents; }
    public void setDocuments(List<ClientDocumentDto> documents) { this.documents = documents; }
}
