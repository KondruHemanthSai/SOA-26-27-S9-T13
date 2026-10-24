# ==============================================================================
# Student Central — Comprehensive Database Seeder Script
# Seeds Students, Profiles, Admissions, Courses, Faculty, Schedules, Registrations & Notifications
# ==============================================================================
param(
    [string]$GatewayUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "    Student Central — Seeding Realistic Campus Data       " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Admin Login
Write-Host "`n[1/6] Authenticating as Admin..." -ForegroundColor Yellow
$adminLogin = Invoke-RestMethod -Uri "$GatewayUrl/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"admin@studentcentral.com","password":"Admin@12345"}'
$adminToken = $adminLogin.token
$adminId = $adminLogin.userId
Write-Host "  -> Admin authenticated (Token received: $($adminToken.Substring(0, 15))...)" -ForegroundColor Green

$adminHeaders = @{
    Authorization = "Bearer $adminToken"
    "Content-Type" = "application/json"
}

# 2. Register Students
Write-Host "`n[2/6] Registering Students..." -ForegroundColor Yellow
$students = @(
    @{ Name = "Hemanth Sai"; Email = "student@studentcentral.com"; Password = "Student@12345"; Role = "STUDENT" },
    @{ Name = "Alex Turner"; Email = "alex.turner@studentcentral.com"; Password = "Student@12345"; Role = "STUDENT" },
    @{ Name = "Priya Sharma"; Email = "priya.sharma@studentcentral.com"; Password = "Student@12345"; Role = "STUDENT" },
    @{ Name = "David Chen"; Email = "david.chen@studentcentral.com"; Password = "Student@12345"; Role = "STUDENT" }
)

$studentTokens = @{}
foreach ($s in $students) {
    try {
        $regBody = @{
            name = $s.Name
            email = $s.Email
            password = $s.Password
            role = $s.Role
        } | ConvertTo-Json
        $null = Invoke-RestMethod -Uri "$GatewayUrl/api/auth/register" -Method Post -ContentType "application/json" -Body $regBody
        Write-Host "  -> Created student user: $($s.Email)" -ForegroundColor Green
    } catch {
        Write-Host "  -> Student already exists or registered: $($s.Email)" -ForegroundColor Gray
    }

    # Log in to get token
    try {
        $loginBody = @{ email = $s.Email; password = $s.Password } | ConvertTo-Json
        $loginRes = Invoke-RestMethod -Uri "$GatewayUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
        $studentTokens[$s.Email] = @{
            Token = $loginRes.token
            UserId = $loginRes.userId
            Name = $s.Name
        }
    } catch {
        Write-Host "  [WARN] Failed to login as $($s.Email)" -ForegroundColor Red
    }
}

# 3. Create Student Profile for student@studentcentral.com
Write-Host "`n[3/6] Setting Up Student Profiles..." -ForegroundColor Yellow
$mainStudent = $studentTokens["student@studentcentral.com"]
if ($mainStudent) {
    $stdHeaders = @{
        Authorization = "Bearer $($mainStudent.Token)"
        "Content-Type" = "application/json"
    }

    $profileBody = @{
        firstName = "Hemanth"
        lastName = "Sai"
        phone = "+91 98765 43210"
        dateOfBirth = "2004-05-15"
        address = "Campus Hostel Block C, Room 304, University Avenue"
        program = "Bachelor of Technology in Computer Science & Engineering"
        department = "Computer Science"
        enrollmentYear = 2024
        semester = 5
        academicRecord = @{
            previousInstitution = "Delhi Public School"
            board = "CBSE"
            percentage = 88.5
            cgpa = 8.85
            graduationYear = 2024
        }
    } | ConvertTo-Json

    try {
        $null = Invoke-RestMethod -Uri "$GatewayUrl/api/students/profile" -Method Post -Headers $stdHeaders -Body $profileBody
        Write-Host "  -> Created profile for Hemanth Sai" -ForegroundColor Green
    } catch {
        Write-Host "  -> Profile already exists for Hemanth Sai, updating..." -ForegroundColor Gray
        try {
            $null = Invoke-RestMethod -Uri "$GatewayUrl/api/students/profile" -Method Put -Headers $stdHeaders -Body $profileBody
            Write-Host "  -> Updated profile for Hemanth Sai" -ForegroundColor Green
        } catch {
            Write-Host "  [INFO] Profile up to date" -ForegroundColor Gray
        }
    }

    # Submit Admission Application
    $admissionBody = @{
        program = "Bachelor of Technology in Computer Science & Engineering"
        department = "Computer Science"
        personalDetails = @{
            fullName = "Hemanth Sai"
            email = "student@studentcentral.com"
            phone = "+91 98765 43210"
            dateOfBirth = "2004-05-15"
            address = "Campus Hostel Block C, Room 304"
            nationality = "Indian"
        }
        academicDetails = @{
            highestQualification = "High School Senior Secondary"
            institution = "Delhi Public School"
            board = "CBSE"
            yearOfPassing = 2024
            percentage = 88.5
            cgpa = 8.85
        }
    } | ConvertTo-Json

    try {
        $appRes = Invoke-RestMethod -Uri "$GatewayUrl/api/admissions/apply" -Method Post -Headers $stdHeaders -Body $admissionBody
        Write-Host "  -> Created Admission Application for Hemanth Sai ($($appRes.applicationId))" -ForegroundColor Green
        
        # Admin approve application
        if ($appRes.id) {
            try {
                $approveBody = @{
                    status = "APPROVED"
                    remarks = "Academic records verified. Approved with Merit Scholarship."
                } | ConvertTo-Json
                $null = Invoke-RestMethod -Uri "$GatewayUrl/api/admissions/$($appRes.id)/status" -Method Put -Headers $adminHeaders -Body $approveBody
                Write-Host "  -> Admission status updated to APPROVED" -ForegroundColor Green
            } catch {
                Write-Host "  [INFO] Admission status update note: $_" -ForegroundColor Gray
            }
        }
    } catch {
        Write-Host "  -> Admission application already exists" -ForegroundColor Gray
    }
}

# 4. Create Rich Course Catalog with Real Faculty
Write-Host "`n[4/6] Creating University Courses with Faculty & Credits..." -ForegroundColor Yellow
$coursesToCreate = @(
    @{
        courseCode = "CS301"
        courseName = "Database Management Systems"
        description = "Relational models, SQL optimization, ACID transactions, B-Trees, normalization, indexing, and distributed storage engines."
        department = "Computer Science"
        semester = 5
        credits = 4
        capacity = 60
        courseType = "CORE"
        faculty = "Prof. Dr. Rajesh Sharma"
    },
    @{
        courseCode = "CS302"
        courseName = "Design and Analysis of Algorithms"
        description = "Asymptotic complexity, divide-and-conquer, dynamic programming, greedy methods, graph algorithms, and NP-completeness."
        department = "Computer Science"
        semester = 5
        credits = 4
        capacity = 60
        courseType = "CORE"
        faculty = "Dr. Anita Deshmukh"
    },
    @{
        courseCode = "CS303"
        courseName = "Operating Systems & Kernel Architecture"
        description = "Process scheduling, IPC, memory management, virtual memory, paging, POSIX synchronization, and file system internals."
        department = "Computer Science"
        semester = 5
        credits = 4
        capacity = 55
        courseType = "CORE"
        faculty = "Prof. Vikram Rao"
    },
    @{
        courseCode = "CS304"
        courseName = "Software Engineering & SOA Microservices"
        description = "Service-oriented architecture, API gateways, service discovery, circuit breakers, containerization, and distributed tracing."
        department = "Computer Science"
        semester = 5
        credits = 3
        capacity = 65
        courseType = "CORE"
        faculty = "Dr. Priya Nair"
    },
    @{
        courseCode = "CS305"
        courseName = "Cloud Computing & DevOps Engineering"
        description = "Virtualization, AWS, Docker containers, Kubernetes orchestration, CI/CD pipelines, and microservice infrastructure."
        department = "Computer Science"
        semester = 5
        credits = 3
        capacity = 50
        courseType = "ELECTIVE"
        faculty = "Prof. David Miller"
    },
    @{
        courseCode = "AI301"
        courseName = "Foundations of AI & Machine Learning"
        description = "Supervised & unsupervised learning, gradient descent, linear models, decision trees, support vector machines, and neural nets."
        department = "Artificial Intelligence"
        semester = 5
        credits = 4
        capacity = 50
        courseType = "CORE"
        faculty = "Dr. Aris Thorne"
    },
    @{
        courseCode = "AI302"
        courseName = "Natural Language Processing & LLMs"
        description = "Statistical NLP, word embeddings, transformer architectures, attention mechanisms, fine-tuning, and retrieval-augmented generation."
        department = "Artificial Intelligence"
        semester = 5
        credits = 3
        capacity = 45
        courseType = "ELECTIVE"
        faculty = "Dr. Elena Rostova"
    },
    @{
        courseCode = "CS308"
        courseName = "Computer Networks & Cyber Defense"
        description = "OSI and TCP/IP models, routing protocols, TLS/SSL cryptographic handshakes, DNS, firewalls, and modern network security."
        department = "Computer Science"
        semester = 5
        credits = 3
        capacity = 60
        courseType = "CORE"
        faculty = "Prof. Sanjay Verma"
    },
    @{
        courseCode = "MA301"
        courseName = "Probability, Statistics & Stochastic Models"
        description = "Random variables, joint distributions, expectation, hypothesis testing, Bayesian inference, and Markov chains."
        department = "Mathematics"
        semester = 5
        credits = 3
        capacity = 70
        courseType = "CORE"
        faculty = "Prof. K. Ramanathan"
    },
    @{
        courseCode = "CS310"
        courseName = "Full-Stack Web Engineering with React"
        description = "Modern TypeScript, reactive state, component lifecycles, REST APIs, GraphQL, caching, and responsive user experience design."
        department = "Information Technology"
        semester = 5
        credits = 3
        capacity = 50
        courseType = "ELECTIVE"
        faculty = "Prof. Sarah Jenkins"
    }
)

$createdCourses = @{}
foreach ($c in $coursesToCreate) {
    try {
        $body = $c | ConvertTo-Json
        $res = Invoke-RestMethod -Uri "$GatewayUrl/api/courses" -Method Post -Headers $adminHeaders -Body $body
        $createdCourses[$c.courseCode] = $res
        Write-Host "  -> Added Course: $($c.courseCode) - $($c.courseName) ($($c.credits) cr, $($c.faculty))" -ForegroundColor Green
    } catch {
        Write-Host "  -> Course $($c.courseCode) already exists or fetching..." -ForegroundColor Gray
    }
}

# Fetch all existing courses from server to map IDs
$allCourses = Invoke-RestMethod -Uri "$GatewayUrl/api/courses" -Method Get -Headers $adminHeaders
foreach ($c in $allCourses) {
    $createdCourses[$c.courseCode] = $c
}
Write-Host "  -> Total courses available: $($allCourses.Count)" -ForegroundColor Cyan

# 5. Create Weekly Timetable Schedule Slots
Write-Host "`n[5/6] Creating Weekly Timetable Schedules..." -ForegroundColor Yellow
$schedulesToCreate = @(
    @{ Code = "CS301"; Day = "MONDAY"; Start = "09:00"; End = "10:30"; Room = "CS-101"; Building = "Computing Wing A"; Sec = "A" },
    @{ Code = "CS302"; Day = "MONDAY"; Start = "11:00"; End = "12:30"; Room = "CS-204"; Building = "Alan Turing Hall"; Sec = "A" },
    @{ Code = "CS304"; Day = "TUESDAY"; Start = "09:00"; End = "10:30"; Room = "Lab-301"; Building = "Software Lab Complex"; Sec = "A" },
    @{ Code = "AI301"; Day = "TUESDAY"; Start = "14:00"; End = "15:30"; Room = "AI-Auditorium"; Building = "Innovation Center"; Sec = "A" },
    @{ Code = "CS303"; Day = "WEDNESDAY"; Start = "10:00"; End = "11:30"; Room = "CS-102"; Building = "Systems Wing"; Sec = "A" },
    @{ Code = "CS305"; Day = "WEDNESDAY"; Start = "11:45"; End = "13:15"; Room = "Cloud-Lab"; Building = "Data Center Wing"; Sec = "A" },
    @{ Code = "CS308"; Day = "THURSDAY"; Start = "09:00"; End = "10:30"; Room = "Net-Lab 2"; Building = "Telecom Tower"; Sec = "A" },
    @{ Code = "MA301"; Day = "THURSDAY"; Start = "11:00"; End = "12:30"; Room = "Hall-B"; Building = "Main Lecture Complex"; Sec = "A" },
    @{ Code = "CS310"; Day = "FRIDAY"; Start = "09:30"; End = "11:00"; Room = "Studio-A"; Building = "Design & Tech Hub"; Sec = "A" }
)

foreach ($sch in $schedulesToCreate) {
    $courseObj = $createdCourses[$sch.Code]
    if ($courseObj) {
        try {
            $schBody = @{
                courseId = $courseObj.id
                courseCode = $courseObj.courseCode
                courseName = $courseObj.courseName
                section = $sch.Sec
                dayOfWeek = $sch.Day
                startTime = $sch.Start
                endTime = $sch.End
                classroom = $sch.Room
                building = $sch.Building
                faculty = $courseObj.faculty
                semester = 5
                academicYear = "2026-2027"
                scheduleType = "LECTURE"
            } | ConvertTo-Json

            $null = Invoke-RestMethod -Uri "$GatewayUrl/api/schedules" -Method Post -Headers $adminHeaders -Body $schBody
            Write-Host "  -> Scheduled $($sch.Code) on $($sch.Day) ($($sch.Start)-$($sch.End) in $($sch.Room))" -ForegroundColor Green
        } catch {
            Write-Host "  -> Schedule slot already exists for $($sch.Code) on $($sch.Day)" -ForegroundColor Gray
        }
    }
}

# 6. Register Courses for Main Student
Write-Host "`n[6/6] Enrolling Student in Courses..." -ForegroundColor Yellow
if ($mainStudent) {
    $stdHeaders = @{
        Authorization = "Bearer $($mainStudent.Token)"
        "Content-Type" = "application/json"
    }

    $coursesToEnroll = @("CS301", "CS302", "CS303", "CS304", "AI301")
    foreach ($code in $coursesToEnroll) {
        $courseObj = $createdCourses[$code]
        if ($courseObj) {
            try {
                $regBody = @{
                    courseId = $courseObj.id
                } | ConvertTo-Json
                $null = Invoke-RestMethod -Uri "$GatewayUrl/api/registrations" -Method Post -Headers $stdHeaders -Body $regBody
                Write-Host "  -> Enrolled Hemanth Sai into $($code) ($($courseObj.courseName))" -ForegroundColor Green
            } catch {
                Write-Host "  -> Already enrolled in $code" -ForegroundColor Gray
            }
        }
    }
}

# 7. Broadcast Campus Notifications
Write-Host "`n[Bonus] Broadcasting Campus Announcements..." -ForegroundColor Yellow
$announcements = @(
    @{
        title = "Mid-Term Examination Schedule Released (Fall 2026)"
        message = "The mid-term examination timetable for Semester 5 core engineering subjects has been published. Check your department notice board and student timetable."
        type = "SCHEDULE_UPDATE"
        priority = "HIGH"
    },
    @{
        title = "Course Add/Drop Window Ends This Friday at 5:00 PM"
        message = "Students are requested to finalize their elective course registrations before the registrar cutoff. Maximum credit ceiling is 24 units."
        type = "REGISTRATION_CONFIRMATION"
        priority = "MEDIUM"
    },
    @{
        title = "Campus Placement & Internship Prep Drive Announced"
        message = "Pre-placement training sessions by alumni and industry partners will commence next week in the Innovation Center Auditorium."
        type = "SYSTEM_ANNOUNCEMENT"
        priority = "INFO"
    },
    @{
        title = "Library Digital IEEE & ACM Portal Access Restored"
        message = "Institutional access to digital libraries and research repositories is fully operational through your campus credentials."
        type = "SYSTEM_ANNOUNCEMENT"
        priority = "INFO"
    }
)

if ($mainStudent) {
    foreach ($a in $announcements) {
        try {
            $notifBody = @{
                userId = $mainStudent.UserId
                title = $a.title
                message = $a.message
                type = $a.type
                priority = $a.priority
                link = "/dashboard"
            } | ConvertTo-Json
            $null = Invoke-RestMethod -Uri "$GatewayUrl/api/notifications" -Method Post -Headers $adminHeaders -Body $notifBody
            Write-Host "  -> Sent notification: $($a.title)" -ForegroundColor Green
        } catch {
            Write-Host "  [INFO] Notification notice: $_" -ForegroundColor Gray
        }
    }
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "  Database Seeding Completed Successfully!                " -ForegroundColor Green
Write-Host "  Student Account: student@studentcentral.com / Student@12345" -ForegroundColor Yellow
Write-Host "  Admin Account:   admin@studentcentral.com   / Admin@12345  " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan
