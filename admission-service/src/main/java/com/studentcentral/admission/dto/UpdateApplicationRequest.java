package com.studentcentral.admission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public class UpdateApplicationRequest {

    @Size(min = 2, max = 100, message = "Program must be between 2 and 100 characters")
    private String program;

    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    private String department;

    @Valid
    private AcademicDetailsRequest academicDetails;

    public UpdateApplicationRequest() {
    }

    public UpdateApplicationRequest(String program, String department, AcademicDetailsRequest academicDetails) {
        this.program = program;
        this.department = department;
        this.academicDetails = academicDetails;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public AcademicDetailsRequest getAcademicDetails() {
        return academicDetails;
    }

    public void setAcademicDetails(AcademicDetailsRequest academicDetails) {
        this.academicDetails = academicDetails;
    }
}
