package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.ApplicationDTO;
import com.example.FPTLSPlatform.exception.ApplicationAlreadyApprovedException;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.Application;
import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.enums.Role;
import com.example.FPTLSPlatform.repository.ApplicationRepository;
import com.example.FPTLSPlatform.repository.TeacherRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.service.IApplicationService;
import com.example.FPTLSPlatform.service.IEmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService implements IApplicationService {

    private final ApplicationRepository applicationRepository;
    private final IEmailService emailService;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    public ApplicationService(ApplicationRepository applicationRepository,
                              IEmailService emailService,
                              TeacherRepository teacherRepository,
                              UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.emailService = emailService;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ApplicationDTO createApplication(ApplicationDTO applicationDTO, HttpSession session) {
            Teacher teacher = (Teacher) session.getAttribute("teacher");

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
                        .status("PENDING")
                        .build();

                applicationRepository.save(application);
                applicationDTO.setTeacherName(teacher.getTeacherName());
                session.invalidate();
                return applicationDTO;
    }

    @Override
    public ApplicationDTO updateApplication(Long id) {
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
                    teacherRepository.save(teacher);
                }

                application.setStatus("APPROVE");
                applicationRepository.save(application);


                    context.setVariable("applicationTitle", application.getTitle());
                    context.setVariable("teacherName", application.getTeacher().getTeacherName());
                    emailService.sendEmail(application.getTeacher().getTeacherName(), "Application Approved", "approval-email", context);
                return ApplicationDTO.builder()
                        .applicationId(application.getApplicationId())
                        .title(application.getTitle())
                        .status(application.getStatus())
                        .description(application.getDescription())
                        .teacherName(application.getTeacher().getTeacherName())
                        .build();
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
        return applications.map(app -> ApplicationDTO.builder()
                .applicationId(app.getApplicationId())
                .title(app.getTitle())
                .description(app.getDescription())
                .status(app.getStatus())
                .teacherName(app.getTeacher().getTeacherName())
                .build());
    }

    @Override
    public Page<ApplicationDTO> getApplicationsByStaff(String staffUsername, Pageable pageable) {
        userRepository.findByUserName(staffUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with username: " + staffUsername));
        Page<Application> applications = applicationRepository.findByAssignedStaffUserName(staffUsername, pageable);
        return applications.map(app -> ApplicationDTO.builder()
                .applicationId(app.getApplicationId())
                .title(app.getTitle())
                .description(app.getDescription())
                .status(app.getStatus())
                .teacherName(app.getTeacher().getTeacherName())
                .build());
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
        return applications.map(app -> ApplicationDTO.builder()
                .applicationId(app.getApplicationId())
                .title(app.getTitle())
                .description(app.getDescription())
                .status(app.getStatus())
                .teacherName(app.getTeacher().getTeacherName())
                .build());
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

}
