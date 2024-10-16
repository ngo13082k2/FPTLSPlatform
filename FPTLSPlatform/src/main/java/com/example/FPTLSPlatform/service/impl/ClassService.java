package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.StudentDTO;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.*;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.enums.OrderStatus;
import com.example.FPTLSPlatform.repository.OrderDetailRepository;
import com.example.FPTLSPlatform.util.OAuth2Util;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.repository.ClassRepository;
import com.example.FPTLSPlatform.repository.CourseRepository;
import com.example.FPTLSPlatform.repository.TeacherRepository;
import com.example.FPTLSPlatform.service.IClassService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClassService implements IClassService {

    private final ClassRepository classRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ClassService(ClassRepository classRepository, CourseRepository courseRepository, TeacherRepository teacherRepository, OrderDetailRepository orderDetailRepository) {
        this.classRepository = classRepository;
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public ClassDTO createClass(ClassDTO classDTO) throws GeneralSecurityException, IOException {
        if (classDTO.getName() == null || classDTO.getCode() == null || classDTO.getDescription() == null ||
                classDTO.getStatus() == null || classDTO.getMaxStudents() == null ||
                classDTO.getPrice() == null || classDTO.getEndDate() == null ||
                classDTO.getCourseCode() == null || classDTO.getStartDate() == null) {
            throw new RuntimeException("All fields must be provided and cannot be null");
        }

//        String meetLink = createGoogleMeetLink(classDTO.getName(), classDTO.getStartDate(), classDTO.getEndDate());
//        classDTO.setLocation(meetLink);
        String teacherName = getCurrentUsername();
        Teacher teacher = teacherRepository.findByTeacherName(teacherName)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        classDTO.setCreateDate(LocalDateTime.now());
        if (classDTO.getEndDate().isBefore(classDTO.getStartDate())) {
            throw new RuntimeException("End date and time cannot be before start date and time.");
        }
        Optional<Course> course = courseRepository.findById(classDTO.getCourseCode());
        if (course.isEmpty()) {
            throw new RuntimeException("Course with code " + classDTO.getCourseCode() + " not found");
        }

        classDTO.setTeacherName(teacherName);

        Class newClass = mapDTOToEntity(classDTO, course.get(), teacher);
        Class savedClass = classRepository.save(newClass);
        return mapEntityToDTO(savedClass);
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private String createGoogleMeetLink(String className, LocalDateTime startDateTime, LocalDateTime endDateTime) throws IOException, GeneralSecurityException, GeneralSecurityException {
        Calendar service = OAuth2Util.getCalendarService(); // Sử dụng OAuth2Util để khởi tạo Calendar service

        Event event = new Event()
                .setSummary(className)
                .setDescription("Online class: " + className);

        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(startDateTime.toInstant(ZoneOffset.UTC).toString()))
                .setTimeZone("UTC");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(endDateTime.toInstant(ZoneOffset.UTC).toString()))
                .setTimeZone("UTC");
        event.setEnd(end);

        ConferenceData conferenceData = new ConferenceData()
                .setCreateRequest(new CreateConferenceRequest()
                        .setRequestId(UUID.randomUUID().toString())
                        .setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet")));

        event.setConferenceData(conferenceData);

        // Insert event into Google Calendar and generate Google Meet link
        Event createdEvent = service.events().insert("primary", event)
                .setConferenceDataVersion(1)
                .execute();

        return createdEvent.getHangoutLink();
    }

    @Override
    public ClassDTO confirmClassCompletion(Long classId, String teacherUsername) throws Exception {
        Class scheduledClass = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        if (!scheduledClass.getTeacher().getTeacherName().equals(teacherUsername)) {
            throw new Exception("You are not the teacher of this class.");
        }

        if (scheduledClass.getEndDate() == null || scheduledClass.getEndDate().isAfter(LocalDateTime.now())) {
            throw new Exception("Class cannot be confirmed yet as it has not ended.");
        }

        scheduledClass.setStatus(String.valueOf(OrderStatus.COMPLETE));
        classRepository.save(scheduledClass);

        return mapEntityToDTO(scheduledClass);
    }

    public ClassDTO updateClass(Long classId, ClassDTO classDTO) {
        Class existingClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class with id " + classId + " not found"));
        if (classDTO.getEndDate() != null && classDTO.getEndDate().isBefore(existingClass.getStartDate())) {
            throw new RuntimeException("End date and time cannot be before start date and time.");
        }
        if (classDTO.getDescription() != null) existingClass.setDescription(classDTO.getDescription());
        if (classDTO.getMaxStudents() != null) existingClass.setMaxStudents(classDTO.getMaxStudents());

        Class updatedClass = classRepository.save(existingClass);
        return mapEntityToDTO(updatedClass);
    }

    public List<ClassDTO> getClassesByCourse(String courseCode) {
        List<Class> classes = classRepository.findByCoursesCourseCode(courseCode);

        if (classes.isEmpty()) {
            throw new RuntimeException("No classes found for course code: " + courseCode);
        }

        return classes.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public ClassDTO getClassById(Long classId) {
        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class with id " + classId + " not found"));

        return mapEntityToDTO(clazz);
    }

    public List<ClassDTO> getAllClasses() {
        List<Class> classes = classRepository.findAll();
        if (classes.isEmpty()) {
            throw new IllegalStateException("No classes found in the system.");
        }
        return classes.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public List<ClassDTO> getClassesByTeacherName(String teacherName) {
        Teacher teacher = teacherRepository.findByTeacherName(teacherName)
                .orElseThrow(() -> new IllegalArgumentException("Teacher with name '" + teacherName + "' not found."));

        List<Class> classes = classRepository.findByTeacherTeacherName(teacherName);
        if (classes.isEmpty()) {
            throw new IllegalStateException("No classes found for teacher '" + teacherName + "'.");
        }

        return classes.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public Page<StudentDTO> getAllStudentsInClass(Long classId, Pageable pageable) {
        Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(classId, pageable);
        return orderDetails.map(orderDetail -> {
            User student = orderDetail.getOrder().getUser();
            return new StudentDTO(student.getUserName(), student.getPhoneNumber(), student.getEmail(), student.getEmail(), student.getAddress());
        });
    }

    private Class mapDTOToEntity(ClassDTO classDTO, Course course, Teacher teacher) {
        return Class.builder()
                .name(classDTO.getName())
                .code(classDTO.getCode())
                .description(classDTO.getDescription())
                .status(classDTO.getStatus())
                .location(classDTO.getLocation())
                .maxStudents(classDTO.getMaxStudents())
                .price(classDTO.getPrice())
                .teacher(teacher)
                .createDate(classDTO.getCreateDate())
                .startDate(classDTO.getStartDate())
                .endDate(classDTO.getEndDate())
                .courses(course)
                .build();
    }

    private ClassDTO mapEntityToDTO(Class clazz) {
        Page<OrderDetail> orderDetails = orderDetailRepository.findByClasses_ClassId(clazz.getClassId(), Pageable.unpaged());
        List<StudentDTO> studentDTOList = orderDetails.getContent().stream()
                .map(orderDetail -> {
                    User user = orderDetail.getOrder().getUser();
                    return StudentDTO.builder()
                            .userName(user.getUserName())
                            .fullName(user.getFullName())
                            .email(user.getEmail())
                            .phoneNumber(user.getPhoneNumber())
                            .address(user.getAddress())
                            .build();
                })
                .distinct()
                .toList();

        return ClassDTO.builder()
                .classId(clazz.getClassId())
                .name(clazz.getName())
                .code(clazz.getCode())
                .description(clazz.getDescription())
                .status(clazz.getStatus())
                .location(clazz.getLocation())
                .maxStudents(clazz.getMaxStudents())
                .createDate(clazz.getCreateDate())
                .price(clazz.getPrice())
                .teacherName(clazz.getTeacher().getTeacherName())
                .fullName(clazz.getTeacher().getFullName())
                .startDate(clazz.getStartDate())
                .endDate(clazz.getEndDate())
                .courseCode(clazz.getCourses().getCourseCode())
                .students(studentDTOList)
                .build();
    }
}
