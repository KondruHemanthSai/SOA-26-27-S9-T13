package com.studentcentral.student.dto;

import com.studentcentral.student.model.AcademicRecord;

public class AcademicRecordResponse {

    private String previousInstitution;
    private String board;
    private Double percentage;
    private Double cgpa;
    private Integer graduationYear;

    public AcademicRecordResponse() {
    }

    public AcademicRecordResponse(String previousInstitution, String board, Double percentage, Double cgpa, Integer graduationYear) {
        this.previousInstitution = previousInstitution;
        this.board = board;
        this.percentage = percentage;
        this.cgpa = cgpa;
        this.graduationYear = graduationYear;
    }

    public static AcademicRecordResponse fromModel(AcademicRecord record) {
        if (record == null) {
            return null;
        }
        return new AcademicRecordResponse(
                record.getPreviousInstitution(),
                record.getBoard(),
                record.getPercentage(),
                record.getCgpa(),
                record.getGraduationYear()
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
