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
import com.example.FPTLSPlatform.util.JwtUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class ApplicationService implements IApplicationService {

    private final ApplicationRepository applicationRepository;

    @Autowired
    private JwtUtil jwtUtil;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    public ApplicationService(ApplicationRepository applicationRepository,
                              TeacherRepository teacherRepository,
                              UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
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

            if (applicationDTO.getStatus().equalsIgnoreCase("PENDING")) {
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
            } else {
                throw new IllegalArgumentException("Invalid status provided for application.");
            }
    }

    @Override
    public ApplicationDTO updateApplication(Long id) {
        Optional<Application> optionalApplication = applicationRepository.findById(id);

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
    public Page<ApplicationDTO> getApplicationsByStaff(String token, Pageable pageable) {
        String staffUsername = jwtUtil.extractUsername(token);
        Set<Role> roles = jwtUtil.extractRoles(token);

        // Check if user has a specific role (example: "STAFF")
        if (!roles.contains(Role.STAFF)) {
            throw new SecurityException("User does not have permission to access this resource.");
        }

        Page<Application> applications = applicationRepository.findByAssignedStaffUserName(staffUsername, pageable);
        return applications.map(app -> ApplicationDTO.builder()
                .applicationId(app.getApplicationId())
                .title(app.getTitle())
                .description(app.getDescription())
                .status(app.getStatus())
                .teacherName(app.getTeacher().getTeacherName())
                .build());
    }


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
}
