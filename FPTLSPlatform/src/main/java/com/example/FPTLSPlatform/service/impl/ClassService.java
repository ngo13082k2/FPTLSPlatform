package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.Course;
import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.repository.ClassRepository;
import com.example.FPTLSPlatform.repository.CourseRepository;
import com.example.FPTLSPlatform.repository.TeacherRepository;
import com.example.FPTLSPlatform.service.IClassService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClassService implements IClassService {

        private final ClassRepository classRepository;
        private final CourseRepository courseRepository;
        private final TeacherRepository teacherRepository;
        public ClassService(ClassRepository classRepository, CourseRepository courseRepository, TeacherRepository teacherRepository) {
            this.classRepository = classRepository;
            this.courseRepository = courseRepository;
            this.teacherRepository = teacherRepository;
        }

    public ClassDTO createClass(ClassDTO classDTO) {
        if (classDTO.getName() == null || classDTO.getCode() == null || classDTO.getDescription() == null ||
                classDTO.getStatus() == null || classDTO.getLocation() == null || classDTO.getMaxStudents() == null ||
                classDTO.getPrice() == null || classDTO.getEndDate() == null ||
                classDTO.getCourseCode() == null) {
            throw new RuntimeException("All fields must be provided and cannot be null");
        }

        String teacherName = getCurrentUsername();
        Teacher teacher = teacherRepository.findByTeacherName(teacherName)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        classDTO.setStartDate(LocalDateTime.now());
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
                .startDate(classDTO.getStartDate())
                .endDate(classDTO.getEndDate())
                .courses(course)
                .build();
    }

    private ClassDTO mapEntityToDTO(Class clazz) {
        return ClassDTO.builder()
                .classId(clazz.getClassId())
                .name(clazz.getName())
                .code(clazz.getCode())
                .description(clazz.getDescription())
                .status(clazz.getStatus())
                .location(clazz.getLocation())
                .maxStudents(clazz.getMaxStudents())
                .price(clazz.getPrice())
                .teacherName(clazz.getTeacher().getTeacherName())
                .fullName(clazz.getTeacher().getFullName())
                .startDate(clazz.getStartDate())
                .endDate(clazz.getEndDate())
                .courseCode(clazz.getCourses().getCourseCode())
                .build();
    }
}
