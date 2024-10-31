package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.ApplicationDTO;
import com.example.FPTLSPlatform.exception.ApplicationAlreadyApprovedException;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.*;
import com.example.FPTLSPlatform.model.enums.Role;
import com.example.FPTLSPlatform.repository.*;
import com.example.FPTLSPlatform.service.IApplicationService;
import com.example.FPTLSPlatform.service.IEmailService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.lang.System;
import java.util.*;

@Service
public class ApplicationService implements IApplicationService {

    private final ApplicationRepository applicationRepository;
    private final IEmailService emailService;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private MultipartFile certificate;


    public ApplicationService(ApplicationRepository applicationRepository,
                              IEmailService emailService,
                              TeacherRepository teacherRepository,
                              UserRepository userRepository,
                              CourseRepository courseRepository,
                              CategoryRepository categoryRepository,
                              CloudinaryService cloudinaryService) {
        this.applicationRepository = applicationRepository;
        this.emailService = emailService;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public ApplicationDTO createApplication(ApplicationDTO applicationDTO, MultipartFile certificate, HttpSession session) throws IOException {
        Teacher teacher = (Teacher) session.getAttribute("teacher");
        applicationDTO.setStatus("PENDING");
        if (teacher == null) {
            throw new ResourceNotFoundException("Teacher not found");
        }

        if (applicationDTO.getStatus().equalsIgnoreCase("APPROVED")) {
            throw new ApplicationAlreadyApprovedException("Application has already been approved and cannot be modified.");
        }

        Application application = Application.builder()
                .title(applicationDTO.getTitle())
                .description(applicationDTO.getDescription())
                .teacher(teacherRepository.getTeacherByTeacherName(teacher.getTeacherName()))
                .status(applicationDTO.getStatus())
                .build();


        String certificateUrl = null;
        if (certificate != null && !certificate.isEmpty()) {
            certificateUrl = cloudinaryService.uploadImage(certificate);
            applicationDTO.setCertificate(certificateUrl);
        }

        if (applicationDTO.getCategoryIds() != null) {
            application.setCategoriesId(new HashSet<>(applicationDTO.getCategoryIds()));
        }

        if (applicationDTO.getCourseCodes() != null) {
            application.setCourses(new HashSet<>(applicationDTO.getCourseCodes()));
        }

        applicationRepository.save(application);
        applicationDTO.setTeacherName(teacher.getTeacherName());
        session.invalidate();
        return applicationDTO;
    }


    @Override
    @Transactional
    public ApplicationDTO approveApplication(Long id) {
        Optional<Application> optionalApplication = applicationRepository.findById(id);
        Context context = new Context();

        if (optionalApplication.isPresent()) {
            Application application = optionalApplication.get();

            if (application.getStatus().equalsIgnoreCase("APPROVED")) {
                throw new ApplicationAlreadyApprovedException("Application has already been approved and cannot be modified.");
            }

            if ("ASSIGNED".equals(application.getStatus())) {
                Optional<Teacher> optionalTeacher = teacherRepository.findByTeacherName(application.getTeacher().getTeacherName());

                if (optionalTeacher.isPresent()) {
                    Teacher teacher = optionalTeacher.get();

                    teacher.setStatus("ACTIVE");

                    Set<Category> copiedCategories = new HashSet<>();
                    for (Long categoryId : application.getCategoriesId()) {
                        Category category = categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
                        copiedCategories.add(category);
                    }

                    Set<Course> copiedCourses = new HashSet<>(courseRepository.findAllByCourseCodeIn(application.getCourses()));
                    if (copiedCourses.size() != application.getCourses().size()) {
                        List<String> missingCourses = application.getCourses().stream()
                                .filter(courseCode -> copiedCourses.stream()
                                        .noneMatch(course -> course.getCourseCode().equals(courseCode)))
                                .toList();

                        throw new ResourceNotFoundException("Courses not found: " + missingCourses);
                    }

                    teacher.setCategories(copiedCategories);
                    teacher.setCourses(copiedCourses);

                    teacherRepository.save(teacher);
                }

                application.setStatus("APPROVED");
                applicationRepository.save(application);

                context.setVariable("applicationTitle", application.getTitle());
                context.setVariable("teacherName", application.getTeacher().getTeacherName());
                emailService.sendEmail(application.getTeacher().getTeacherName(), "Application Approved", "approval-email", context);

                return convertToDTO(application);
            } else {
                throw new IllegalArgumentException("Invalid status provided for application.");
            }
        } else {
            throw new ResourceNotFoundException("Application not found.");
        }
    }


    @Override
    public Page<ApplicationDTO> getAllApplications(Pageable pageable) {
        Page<Application> applications = applicationRepository.findAll(pageable);
        return applications.map(this::convertToDTO);
    }

    private ApplicationDTO convertToDTO(Application app) {
        return ApplicationDTO.builder()
                .applicationId(app.getApplicationId())
                .title(app.getTitle())
                .description(app.getDescription())
                .categoryIds(app.getCategoriesId())
                .courseCodes(app.getCourses())
                .status(app.getStatus())
                .teacherName(app.getTeacher().getTeacherName())
                .build();
    }

    @Override
    public Page<ApplicationDTO> getApplicationsByStaff(String staffUsername, Pageable pageable) {
        userRepository.findByUserName(staffUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with username: " + staffUsername));
        Page<Application> applications = applicationRepository.findByAssignedStaffUserName(staffUsername, pageable);
        return applications.map(this::convertToDTO);
    }

    @Override
    public Application assignApplicationToStaff(Long applicationId, String staffUsername) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getStatus().equals("PENDING") || application.getAssignedStaff() != null) {
            throw new IllegalStateException("Application cannot be assigned. It must be in PENDING status and not already assigned.");
        }

        User staff = userRepository.findByUserName(staffUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with username: " + staffUsername));

        application.setAssignedStaff(staff);
        application.setStatus("ASSIGNED");
        return applicationRepository.save(application);
    }

    @Override
    public Page<ApplicationDTO> getPendingApplications(Pageable pageable) {
        Page<Application> applications = applicationRepository.findByStatusAndAssignedStaffIsNull("PENDING", pageable);
        return applications.map(this::convertToDTO);
    }

    @Override
    public void assignApplicationsToAllStaff() {
        List<User> allStaff = userRepository.findByRole(Role.STAFF);

        if (allStaff.isEmpty()) {
            throw new ResourceNotFoundException("No staff members available for assignment.");
        }
        List<Application> pendingApplications = applicationRepository.findByStatus("PENDING");

        if (pendingApplications.isEmpty()) {
            throw new ResourceNotFoundException("No pending applications available for assignment.");
        }

        int staffCount = allStaff.size();
        int index = 0;
        for (Application application : pendingApplications) {
            User staff = allStaff.get(index % staffCount);

            application.setAssignedStaff(staff);
            application.setStatus("ASSIGNED");

            applicationRepository.save(application);

            index++;
        }
    }

    @Override
    public ApplicationDTO rejectApplication(Long id, String rejectionReason) {
        Context context = new Context();
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        if (application.getStatus().equals("APPROVED")) {
            throw new ApplicationAlreadyApprovedException("Application already approved, cannot reject.");
        }

        if (application.getStatus().equalsIgnoreCase("REJECTED")) {
            throw new IllegalStateException("Application has already been rejected.");
        }

        application.setStatus("REJECTED");
        application.setRejectionReason(rejectionReason);
        applicationRepository.save(application);

        Optional<Teacher> optionalTeacher = teacherRepository.findByTeacherName(application.getTeacher().getTeacherName());
        if (optionalTeacher.isPresent()) {
            Teacher teacher = optionalTeacher.get();
            teacher.setStatus("INACTIVE");
            teacherRepository.save(teacher);
        }

        context.setVariable("applicationTitle", application.getTitle());
        context.setVariable("teacherName", application.getTeacher().getTeacherName());
        context.setVariable("rejectionReason", rejectionReason);
        emailService.sendEmail(application.getTeacher().getTeacherName(), "Application Rejected", "reject-email", context);

        return convertToDTO(application);

    }

}
