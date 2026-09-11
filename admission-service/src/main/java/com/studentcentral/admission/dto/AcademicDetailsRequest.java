package com.studentcentral.admission.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class AcademicDetailsRequest {

    private String previousInstitution;
    private String board;

    @DecimalMin(value = "0.0", message = "Percentage cannot be less than 0")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100")
    private Double percentage;

    @DecimalMin(value = "0.0", message = "CGPA cannot be less than 0.0")
    @DecimalMax(value = "10.0", message = "CGPA cannot exceed 10.0")
    private Double cgpa;

    @Min(value = 1950, message = "Graduation year must be reasonable")
    @Max(value = 2100, message = "Graduation year must be reasonable")
    private Integer graduationYear;

    public AcademicDetailsRequest() {
    }

    public AcademicDetailsRequest(String previousInstitution, String board, Double percentage, Double cgpa, Integer graduationYear) {
        this.previousInstitution = previousInstitution;
        this.board = board;
        this.percentage = percentage;
        this.cgpa = cgpa;
        this.graduationYear = graduationYear;
    }

    public String getPreviousInstitution() {
        return previousInstitution;
    }

    public void setPreviousInstitution(String previousInstitution) {
        this.previousInstitution = previousInstitution;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }
}
