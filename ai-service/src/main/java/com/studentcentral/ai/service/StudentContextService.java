package com.studentcentral.ai.service;

import com.studentcentral.ai.client.*;
import org.springframework.stereotype.Service;

@Service
public class StudentContextService {

    private final StudentServiceClient studentServiceClient;
    private final AdmissionServiceClient admissionServiceClient;
    private final CourseServiceClient courseServiceClient;
    private final RegistrationServiceClient registrationServiceClient;
    private final ScheduleServiceClient scheduleServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public StudentContextService(StudentServiceClient studentServiceClient,
                                 AdmissionServiceClient admissionServiceClient,
                                 CourseServiceClient courseServiceClient,
                                 RegistrationServiceClient registrationServiceClient,
                                 ScheduleServiceClient scheduleServiceClient,
                                 NotificationServiceClient notificationServiceClient) {
        this.studentServiceClient = studentServiceClient;
        this.admissionServiceClient = admissionServiceClient;
        this.courseServiceClient = courseServiceClient;
        this.registrationServiceClient = registrationServiceClient;
        this.scheduleServiceClient = scheduleServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    public StudentContext buildFullContext(String userId, String jwtToken) {
        StudentContext context = new StudentContext();
        studentServiceClient.getProfile(userId, jwtToken).ifPresent(context::setProfile);
        admissionServiceClient.getMyApplication(userId, jwtToken).ifPresent(context::setAdmission);
        context.setRegistrations(registrationServiceClient.getMyCourses(userId, jwtToken));
        context.setAvailableCourses(courseServiceClient.getAllCourses(jwtToken));
        context.setSchedule(scheduleServiceClient.getMyTimetable(userId, jwtToken));
        context.setNotifications(notificationServiceClient.getUnreadNotifications(userId, jwtToken));
        context.setUnreadNotificationCount(notificationServiceClient.getUnreadCount(userId, jwtToken));
        return context;
    }

    public StudentContext buildCoursePathfinderContext(String userId, String jwtToken) {
        StudentContext context = new StudentContext();
        studentServiceClient.getProfile(userId, jwtToken).ifPresent(context::setProfile);
        context.setRegistrations(registrationServiceClient.getMyCourses(userId, jwtToken));
        context.setAvailableCourses(courseServiceClient.getAllCourses(jwtToken));
        context.setSchedule(scheduleServiceClient.getMyTimetable(userId, jwtToken));
        return context;
    }

    public StudentContext buildAdmissionContext(String userId, String jwtToken) {
        StudentContext context = new StudentContext();
        admissionServiceClient.getMyApplication(userId, jwtToken).ifPresent(context::setAdmission);
        return context;
    }

    public StudentContext buildScheduleContext(String userId, String jwtToken) {
        StudentContext context = new StudentContext();
        context.setSchedule(scheduleServiceClient.getMyTimetable(userId, jwtToken));
        return context;
    }
}
