package com.studentcentral.schedule.repository;

import com.studentcentral.schedule.model.DayOfWeek;
import com.studentcentral.schedule.model.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, String> {

    List<Schedule> findByCourseId(String courseId);

    List<Schedule> findByCourseIdIn(Collection<String> courseIds);

    List<Schedule> findByCourseCode(String courseCode);

    List<Schedule> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<Schedule> findBySemester(Integer semester);

    List<Schedule> findByAcademicYear(String academicYear);

    List<Schedule> findByClassroom(String classroom);

    List<Schedule> findByFaculty(String faculty);

    List<Schedule> findByClassroomAndDayOfWeek(String classroom, DayOfWeek dayOfWeek);

    List<Schedule> findByFacultyAndDayOfWeek(String faculty, DayOfWeek dayOfWeek);

    List<Schedule> findByCourseIdAndDayOfWeek(String courseId, DayOfWeek dayOfWeek);
}
