#!/usr/bin/env python3
"""
Student Central - Comprehensive Campus Data Seeder
Populates Students, Profiles, Admissions, Courses, Faculty, Schedules, Registrations & Notifications.
"""

import json
import urllib.request
import urllib.error
import time

GATEWAY_URL = "http://localhost:8080"

def request_json(method, endpoint, body=None, token=None):
    url = f"{GATEWAY_URL}{endpoint}"
    data = json.dumps(body).encode("utf-8") if body else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    try:
        with urllib.request.urlopen(req, timeout=15) as res:
            res_body = res.read().decode("utf-8")
            return json.loads(res_body) if res_body else {}
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode("utf-8")
        try:
            return {"_error_status": e.code, "_error": json.loads(err_msg)}
        except:
            return {"_error_status": e.code, "_error": err_msg}
    except Exception as e:
        return {"_error_status": 500, "_error": str(e)}

def main():
    print("==========================================================")
    print("      Student Central — Seeding Realistic Campus Data     ")
    print("==========================================================")

    # 1. Admin Login
    print("\n[1/6] Authenticating as Admin...")
    admin_login = request_json("POST", "/api/auth/login", {
        "email": "admin@studentcentral.com",
        "password": "Admin@12345"
    })
    admin_token = admin_login.get("token")
    if not admin_token:
        print(f"FAILED to authenticate admin: {admin_login}")
        return
    print(f"  -> Admin authenticated (Token received: {admin_token[:15]}...)")

    # 2. Register Students
    print("\n[2/6] Registering Students...")
    students = [
        {"name": "Hemanth Sai", "email": "student@studentcentral.com", "password": "Student@12345", "role": "STUDENT"},
        {"name": "Alex Turner", "email": "alex.turner@studentcentral.com", "password": "Student@12345", "role": "STUDENT"},
        {"name": "Priya Sharma", "email": "priya.sharma@studentcentral.com", "password": "Student@12345", "role": "STUDENT"},
        {"name": "David Chen", "email": "david.chen@studentcentral.com", "password": "Student@12345", "role": "STUDENT"}
    ]

    student_tokens = {}
    for s in students:
        reg_res = request_json("POST", "/api/auth/register", s)
        if "_error_status" not in reg_res:
            print(f"  -> Created student user: {s['email']}")
        else:
            print(f"  -> Student already registered or exists: {s['email']}")

        login_res = request_json("POST", "/api/auth/login", {
            "email": s["email"],
            "password": s["password"]
        })
        if "token" in login_res:
            student_tokens[s["email"]] = {
                "token": login_res["token"],
                "userId": login_res["userId"],
                "name": s["name"]
            }

    # 3. Create Student Profile for student@studentcentral.com
    print("\n[3/6] Setting Up Student Profile & Admission Application...")
    main_student = student_tokens.get("student@studentcentral.com")
    if main_student:
        s_tok = main_student["token"]
        profile_data = {
            "firstName": "Hemanth",
            "lastName": "Sai",
            "phone": "+91 98765 43210",
            "dateOfBirth": "2004-05-15",
            "address": "Campus Hostel Block C, Room 304, University Avenue",
            "program": "Bachelor of Technology in Computer Science & Engineering",
            "department": "Computer Science",
            "enrollmentYear": 2024,
            "semester": 5,
            "academicRecord": {
                "previousInstitution": "Delhi Public School",
                "board": "CBSE",
                "percentage": 88.5,
                "cgpa": 8.85,
                "graduationYear": 2024
            }
        }
        prof_res = request_json("POST", "/api/students/profile", profile_data, token=s_tok)
        if "_error_status" not in prof_res:
            print("  -> Created profile for Hemanth Sai (Student ID: " + prof_res.get("studentId", "N/A") + ")")
        else:
            request_json("PUT", "/api/students/profile", profile_data, token=s_tok)
            print("  -> Profile already existed, updated successfully")

        # Admission Application
        admission_data = {
            "program": "Bachelor of Technology in Computer Science & Engineering",
            "department": "Computer Science",
            "personalDetails": {
                "fullName": "Hemanth Sai",
                "email": "student@studentcentral.com",
                "phone": "+91 98765 43210",
                "dateOfBirth": "2004-05-15",
                "address": "Campus Hostel Block C, Room 304",
                "nationality": "Indian"
            },
            "academicDetails": {
                "highestQualification": "High School Senior Secondary",
                "institution": "Delhi Public School",
                "board": "CBSE",
                "yearOfPassing": 2024,
                "percentage": 88.5,
                "cgpa": 8.85
            }
        }
        adm_res = request_json("POST", "/api/admissions/apply", admission_data, token=s_tok)
        if "_error_status" not in adm_res:
            print("  -> Created Admission Application for Hemanth Sai (" + adm_res.get("applicationId", "") + ")")
            app_id = adm_res.get("id")
            if app_id:
                request_json("PUT", f"/api/admissions/{app_id}/status", {
                    "status": "APPROVED",
                    "remarks": "Academic records verified. Approved with Merit Scholarship."
                }, token=admin_token)
                print("  -> Updated admission status to APPROVED")
        else:
            print("  -> Admission application already on file")

    # Also submit an application for Alex Turner
    alex = student_tokens.get("alex.turner@studentcentral.com")
    if alex:
        request_json("POST", "/api/admissions/apply", {
            "program": "B.Tech in Artificial Intelligence & Data Science",
            "department": "Artificial Intelligence",
            "personalDetails": {
                "fullName": "Alex Turner",
                "email": "alex.turner@studentcentral.com",
                "phone": "+91 98111 22334",
                "dateOfBirth": "2004-08-20",
                "address": "Greenwood Valley Apt 402",
                "nationality": "Indian"
            },
            "academicDetails": {
                "highestQualification": "High School Senior Secondary",
                "institution": "St. Xavier Senior Secondary",
                "board": "ICSE",
                "yearOfPassing": 2024,
                "percentage": 86.0,
                "cgpa": 8.6
            }
        }, token=alex["token"])
        print("  -> Created Under-Review Admission for Alex Turner")

    # 4. Create Rich Course Catalog with Faculty & Credits
    print("\n[4/6] Creating University Courses with Faculty & Credits...")
    courses = [
        {
            "courseCode": "CS301",
            "courseName": "Database Management Systems",
            "description": "Relational models, SQL optimization, ACID transactions, B-Trees, normalization, indexing, and distributed storage engines.",
            "department": "Computer Science",
            "semester": 5,
            "credits": 4,
            "capacity": 60,
            "courseType": "CORE",
            "faculty": "Prof. Dr. Rajesh Sharma"
        },
        {
            "courseCode": "CS302",
            "courseName": "Design and Analysis of Algorithms",
            "description": "Asymptotic complexity, divide-and-conquer, dynamic programming, greedy methods, graph algorithms, and NP-completeness.",
            "department": "Computer Science",
            "semester": 5,
            "credits": 4,
            "capacity": 60,
            "courseType": "CORE",
            "faculty": "Dr. Anita Deshmukh"
        },
        {
            "courseCode": "CS303",
            "courseName": "Operating Systems & Kernel Architecture",
            "description": "Process scheduling, IPC, memory management, virtual memory, paging, POSIX synchronization, and file system internals.",
            "department": "Computer Science",
            "semester": 5,
            "credits": 4,
            "capacity": 55,
            "courseType": "CORE",
            "faculty": "Prof. Vikram Rao"
        },
        {
            "courseCode": "CS304",
            "courseName": "Software Engineering & SOA Microservices",
            "description": "Service-oriented architecture, API gateways, service discovery, circuit breakers, containerization, and distributed tracing.",
            "department": "Computer Science",
            "semester": 5,
            "credits": 3,
            "capacity": 65,
            "courseType": "CORE",
            "faculty": "Dr. Priya Nair"
        },
        {
            "courseCode": "CS305",
            "courseName": "Cloud Computing & DevOps Engineering",
            "description": "Virtualization, AWS, Docker containers, Kubernetes orchestration, CI/CD pipelines, and microservice infrastructure.",
            "department": "Computer Science",
            "semester": 5,
            "credits": 3,
            "capacity": 50,
            "courseType": "ELECTIVE",
            "faculty": "Prof. David Miller"
        },
        {
            "courseCode": "AI301",
            "courseName": "Foundations of AI & Machine Learning",
            "description": "Supervised & unsupervised learning, gradient descent, linear models, decision trees, support vector machines, and neural nets.",
            "department": "Artificial Intelligence",
            "semester": 5,
            "credits": 4,
            "capacity": 50,
            "courseType": "CORE",
            "faculty": "Dr. Aris Thorne"
        },
        {
            "courseCode": "AI302",
            "courseName": "Natural Language Processing & LLMs",
            "description": "Statistical NLP, word embeddings, transformer architectures, attention mechanisms, fine-tuning, and retrieval-augmented generation.",
            "department": "Artificial Intelligence",
            "semester": 5,
            "credits": 3,
            "capacity": 45,
            "courseType": "ELECTIVE",
            "faculty": "Dr. Elena Rostova"
        },
        {
            "courseCode": "CS308",
            "courseName": "Computer Networks & Cyber Defense",
            "description": "OSI and TCP/IP models, routing protocols, TLS/SSL cryptographic handshakes, DNS, firewalls, and modern network security.",
            "department": "Computer Science",
            "semester": 5,
            "credits": 3,
            "capacity": 60,
            "courseType": "CORE",
            "faculty": "Prof. Sanjay Verma"
        },
        {
            "courseCode": "MA301",
            "courseName": "Probability, Statistics & Stochastic Models",
            "description": "Random variables, joint distributions, expectation, hypothesis testing, Bayesian inference, and Markov chains.",
            "department": "Mathematics",
            "semester": 5,
            "credits": 3,
            "capacity": 70,
            "courseType": "CORE",
            "faculty": "Prof. K. Ramanathan"
        },
        {
            "courseCode": "CS310",
            "courseName": "Full-Stack Web Engineering with React",
            "description": "Modern TypeScript, reactive state, component lifecycles, REST APIs, GraphQL, caching, and responsive user experience design.",
            "department": "Information Technology",
            "semester": 5,
            "credits": 3,
            "capacity": 50,
            "courseType": "ELECTIVE",
            "faculty": "Prof. Sarah Jenkins"
        }
    ]

    for c in courses:
        res = request_json("POST", "/api/courses", c, token=admin_token)
        if "_error_status" not in res:
            print(f"  -> Added Course: {c['courseCode']} - {c['courseName']} ({c['credits']} cr, {c['faculty']})")
        else:
            print(f"  -> Course {c['courseCode']} already present")

    # Fetch updated list of courses
    all_courses = request_json("GET", "/api/courses", token=admin_token)
    if isinstance(all_courses, list):
        print(f"  -> Active Courses in Catalog: {len(all_courses)}")
        course_map = {c["courseCode"]: c for c in all_courses}
    else:
        course_map = {}

    # 5. Create Weekly Timetable Schedule Slots
    print("\n[5/6] Creating Weekly Timetable Schedules...")
    schedules = [
        {"code": "CS301", "day": "MONDAY", "start": "09:00", "end": "10:30", "room": "CS-101", "building": "Computing Wing A", "sec": "A"},
        {"code": "CS302", "day": "MONDAY", "start": "11:00", "end": "12:30", "room": "CS-204", "building": "Alan Turing Hall", "sec": "A"},
        {"code": "CS304", "day": "TUESDAY", "start": "09:00", "end": "10:30", "room": "Lab-301", "building": "Software Lab Complex", "sec": "A"},
        {"code": "AI301", "day": "TUESDAY", "start": "14:00", "end": "15:30", "room": "AI-Auditorium", "building": "Innovation Center", "sec": "A"},
        {"code": "CS303", "day": "WEDNESDAY", "start": "10:00", "end": "11:30", "room": "CS-102", "building": "Systems Wing", "sec": "A"},
        {"code": "CS305", "day": "WEDNESDAY", "start": "11:45", "end": "13:15", "room": "Cloud-Lab", "building": "Data Center Wing", "sec": "A"},
        {"code": "CS308", "day": "THURSDAY", "start": "09:00", "end": "10:30", "room": "Net-Lab 2", "building": "Telecom Tower", "sec": "A"},
        {"code": "MA301", "day": "THURSDAY", "start": "11:00", "end": "12:30", "room": "Hall-B", "building": "Main Lecture Complex", "sec": "A"},
        {"code": "CS310", "day": "FRIDAY", "start": "09:30", "end": "11:00", "room": "Studio-A", "building": "Design & Tech Hub", "sec": "A"}
    ]

    for sch in schedules:
        c_obj = course_map.get(sch["code"])
        if c_obj:
            sch_payload = {
                "courseId": c_obj["id"],
                "courseCode": c_obj["courseCode"],
                "courseName": c_obj["courseName"],
                "section": sch["sec"],
                "dayOfWeek": sch["day"],
                "startTime": sch["start"],
                "endTime": sch["end"],
                "classroom": sch["room"],
                "building": sch["building"],
                "faculty": c_obj.get("faculty", "Faculty Staff"),
                "semester": 5,
                "academicYear": "2026-2027",
                "scheduleType": "LECTURE"
            }
            res = request_json("POST", "/api/schedules", sch_payload, token=admin_token)
            if "_error_status" not in res:
                print(f"  -> Scheduled {sch['code']} on {sch['day']} ({sch['start']}-{sch['end']} in {sch['room']})")
            else:
                print(f"  -> Schedule slot already set for {sch['code']} on {sch['day']}")

    # 6. Enroll Student in 5 Core & Elective Courses
    print("\n[6/6] Enrolling Student in Courses...")
    if main_student:
        s_tok = main_student["token"]
        enroll_codes = ["CS301", "CS302", "CS303", "CS304", "AI301"]
        for code in enroll_codes:
            c_obj = course_map.get(code)
            if c_obj:
                reg_payload = {"courseId": c_obj["id"]}
                res = request_json("POST", "/api/registrations", reg_payload, token=s_tok)
                if "_error_status" not in res:
                    print(f"  -> Enrolled Hemanth Sai into {code} ({c_obj['courseName']})")
                else:
                    print(f"  -> Already enrolled in {code}")

    # 7. Campus Announcements & Notifications
    print("\n[Bonus] Broadcasting Campus Announcements...")
    announcements = [
        {
            "title": "Mid-Term Examination Schedule Released (Fall 2026)",
            "message": "The mid-term examination timetable for Semester 5 core engineering subjects has been published. Check your student timetable and room allocations.",
            "type": "SCHEDULE_UPDATE",
            "priority": "HIGH"
        },
        {
            "title": "Course Add/Drop Window Ends This Friday at 5:00 PM",
            "message": "Students are requested to finalize their elective course registrations before the registrar cutoff. Maximum credit ceiling is 24 units.",
            "type": "REGISTRATION_CONFIRMATION",
            "priority": "MEDIUM"
        },
        {
            "title": "Campus Placement & Internship Prep Drive Announced",
            "message": "Pre-placement training sessions by alumni and industry partners will commence next week in the Innovation Center Auditorium.",
            "type": "SYSTEM_ANNOUNCEMENT",
            "priority": "INFO"
        },
        {
            "title": "Library Digital IEEE & ACM Portal Access Restored",
            "message": "Institutional access to digital libraries and research repositories is fully operational through your campus credentials.",
            "type": "SYSTEM_ANNOUNCEMENT",
            "priority": "INFO"
        }
    ]

    if main_student:
        for a in announcements:
            notif_payload = {
                "userId": main_student["userId"],
                "title": a["title"],
                "message": a["message"],
                "type": a["type"],
                "priority": a["priority"],
                "link": "/dashboard"
            }
            res = request_json("POST", "/api/notifications", notif_payload, token=admin_token)
            if "_error_status" not in res:
                print(f"  -> Sent notification: {a['title']}")

    print("\n==========================================================")
    print("  Database Seeding Completed Successfully!                ")
    print("  Student Login: student@studentcentral.com / Student@12345")
    print("  Admin Login:   admin@studentcentral.com   / Admin@12345  ")
    print("==========================================================")

if __name__ == "__main__":
    main()
