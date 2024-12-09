package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.ApplicationDTO;
import com.example.FPTLSPlatform.dto.CertificateDTO;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApplicationService implements IApplicationService {

    private final ApplicationRepository applicationRepository;
    private final IEmailService emailService;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              IEmailService emailService,
                              TeacherRepository teacherRepository,
                              UserRepository userRepository,
                              CloudinaryService cloudinaryService) {
        this.applicationRepository = applicationRepository;
        this.emailService = emailService;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public ApplicationDTO createApplication(
            ApplicationDTO applicationDTO,
            List<MultipartFile> certificateFiles,
            List<String> certificateNames,
            HttpSession session) throws IOException {

        Teacher teacher = (Teacher) session.getAttribute("teacher");
        if (teacher == null) {
            throw new ResourceNotFoundException("Teacher not found");
        }

        if (applicationDTO.getStatus() == null) {
            applicationDTO.setStatus("PENDING");
        }

        if (applicationDTO.getStatus().equalsIgnoreCase("APPROVED")) {
            throw new ApplicationAlreadyApprovedException("Application has already been approved and cannot be modified.");
        }

        Optional<Application> existingApplication = applicationRepository
                .findByTeacher_TeacherNameAndStatusNot(teacher.getTeacherName(), "APPROVED");

        Application application;
        if (existingApplication.isPresent()) {
            application = existingApplication.get();

            if (application.getStatus().equalsIgnoreCase("REJECTED")) {
                application.setDescription(applicationDTO.getDescription());
                application.setStatus("PENDING");
                application.setRejectionReason("");
                application.setAssignedStaff(null);
                List<Certificate> certificates = application.getCertificates();
                certificates.clear();
                saveCertificate(certificateFiles, certificateNames, teacher, application, certificates);
            } else {
                throw new RuntimeException("A previous application exists and cannot be modified.");
            }
        } else {
            application = Application.builder()
                    .description(applicationDTO.getDescription())
                    .teacher(teacherRepository.getTeacherByTeacherName(teacher.getTeacherName()))
                    .status("PENDING")
                    .build();

            // Thêm chứng chỉ mới từ file lên
            List<Certificate> certificates = new ArrayList<>();
            saveCertificate(certificateFiles, certificateNames, teacher, application, certificates);
        }

        application = applicationRepository.save(application);

        return convertToDTO(application);
    }

    private void saveCertificate(List<MultipartFile> certificateFiles, List<String> certificateNames, Teacher teacher, Application application, List<Certificate> certificates) throws IOException {
        for (int i = 0; i < certificateFiles.size(); i++) {
            MultipartFile file = certificateFiles.get(i);
            String name = certificateNames.get(i);

            if (!file.isEmpty()) {
                String uploadedUrl = cloudinaryService.uploadImage(file);

                Certificate certificate = Certificate.builder()
                        .name(name)
                        .fileUrl(uploadedUrl)
                        .application(application)
                        .teacher(teacher)
                        .build();

                certificates.add(certificate);
            }
            application.setCertificates(certificates);
        }
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
                    teacherRepository.save(teacher);
                }

                // Update application status to APPROVED
                application.setStatus("APPROVED");

                // Avoid changing the certificates collection reference to prevent orphan deletion
                applicationRepository.save(application);

                context.setVariable("teacherName", application.getTeacher().getTeacherName());
                emailService.sendEmail(application.getTeacher().getEmail(), "Application Approved", "approval-email", context);

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

    private ApplicationDTO convertToDTO(Application application) {
        List<CertificateDTO> certificateDTOList = new ArrayList<>();

        if (application.getCertificates() != null) {
            certificateDTOList = application.getCertificates().stream()
                    .map(cert -> new CertificateDTO(cert.getId(), cert.getName(), cert.getFileUrl()))
                    .collect(Collectors.toList());
        }

        return ApplicationDTO.builder()
                .applicationId(application.getApplicationId())
                .description(application.getDescription())
                .certificate(certificateDTOList)
                .status(application.getStatus())
                .teacherName(application.getTeacher().getTeacherName())
                .assignedStaff(application.getAssignedStaff() == null ? null : application.getAssignedStaff().getUserName())
                .rejectionReason(application.getRejectionReason() == null ? "" : application.getRejectionReason())
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
    @Scheduled(cron = "0 0 0 * * ?")
    public void assignApplicationsToAllStaff() {
        List<User> allStaff = userRepository.findByRole(Role.STAFF);

        if (allStaff.isEmpty()) {
            throw new ResourceNotFoundException("No staff members available for assignment.");
        }

        List<Application> pendingApplications = applicationRepository.findByStatus("PENDING");

        if (pendingApplications.isEmpty()) {
            throw new ResourceNotFoundException("No pending applications available for assignment.");
        }

        int index = 0;
        for (Application application : pendingApplications) {
            Set<Category> applicationMajors = application.getTeacher().getMajor();

            List<User> eligibleStaff = allStaff.stream()
                    .filter(staff -> staff.getMajor().stream()
                            .anyMatch(applicationMajors::contains))
                    .toList();

            if (eligibleStaff.isEmpty()) {
                throw new ResourceNotFoundException(
                        "No staff members available for application with majors: " + applicationMajors
                );
            }

            User assignedStaff = eligibleStaff.get(index % eligibleStaff.size());

            application.setAssignedStaff(assignedStaff);
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

        context.setVariable("teacherName", application.getTeacher().getTeacherName());
        context.setVariable("rejectionReason", rejectionReason);
        emailService.sendEmail(application.getTeacher().getEmail(), "Application Rejected", "reject-email", context);

        return convertToDTO(application);

    }

}
