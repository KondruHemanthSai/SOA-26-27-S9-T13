package com.studentcentral.ai.agent.tools;

import com.studentcentral.ai.agent.AgentTool;
import com.studentcentral.ai.client.ScheduleServiceClient;
import com.studentcentral.ai.client.dto.ClientScheduleDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentScheduleTool implements AgentTool {

    private final ScheduleServiceClient scheduleServiceClient;

    public StudentScheduleTool(ScheduleServiceClient scheduleServiceClient) {
        this.scheduleServiceClient = scheduleServiceClient;
    }

    @Override
    public String getName() {
        return "getStudentSchedule";
    }

    @Override
    public String getDescription() {
        return "Retrieves the authenticated student's weekly class timetable, including days of week, start/end times, classrooms, and faculty.";
    }

    @Override
    public String execute(String userId, String jwtToken, String parameter) {
        List<ClientScheduleDto> schedule = scheduleServiceClient.getMyTimetable(userId, jwtToken);
        if (schedule.isEmpty()) {
            return "You have no scheduled class sessions for this week.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Weekly Timetable (").append(schedule.size()).append(" sessions):\n");
        for (ClientScheduleDto s : schedule) {
            sb.append(String.format("- %s %s-%s: %s (%s, Room: %s, Faculty: %s)\n",
                    s.getDayOfWeek(), s.getStartTime(), s.getEndTime(),
                    s.getCourseCode(), s.getCourseName(), s.getClassroom(), s.getFaculty()));
        }
        return sb.toString();
    }
}
