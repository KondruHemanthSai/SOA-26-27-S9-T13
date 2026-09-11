package com.studentcentral.admission.dto;

import com.studentcentral.admission.model.AcademicDetails;

public class AcademicDetailsResponse {

    private String previousInstitution;
    private String board;
    private Double percentage;
    private Double cgpa;
    private Integer graduationYear;

    public AcademicDetailsResponse() {
    }

    public AcademicDetailsResponse(String previousInstitution, String board, Double percentage, Double cgpa, Integer graduationYear) {
        this.previousInstitution = previousInstitution;
        this.board = board;
        this.percentage = percentage;
        this.cgpa = cgpa;
        this.graduationYear = graduationYear;
    }

    public static AcademicDetailsResponse fromModel(AcademicDetails details) {
        if (details == null) {
            return null;
        }
        return new AcademicDetailsResponse(
                details.getPreviousInstitution(),
                details.getBoard(),
                details.getPercentage(),
                details.getCgpa(),
                details.getGraduationYear()
        );
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
