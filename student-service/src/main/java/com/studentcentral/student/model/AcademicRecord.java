package com.studentcentral.student.model;

/**
 * Embedded model representing a student's prior academic performance.
 */
public class AcademicRecord {

    private String previousInstitution;
    private String board;
    private Double percentage;
    private Double cgpa;
    private Integer graduationYear;

    public AcademicRecord() {
    }

    public AcademicRecord(String previousInstitution, String board, Double percentage, Double cgpa, Integer graduationYear) {
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

    @Override
    public String toString() {
        return "AcademicRecord{" +
                "previousInstitution='" + previousInstitution + '\'' +
                ", board='" + board + '\'' +
                ", percentage=" + percentage +
                ", cgpa=" + cgpa +
                ", graduationYear=" + graduationYear +
                '}';
    }
}
