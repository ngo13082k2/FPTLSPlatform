package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.ApplicationDTO;
import com.example.FPTLSPlatform.model.Application;
import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.repository.ApplicationRepository;
import com.example.FPTLSPlatform.repository.TeacherRepository;
import com.example.FPTLSPlatform.service.IApplicationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApplicationService implements IApplicationService {

    private final ApplicationRepository applicationRepository;

    private final TeacherRepository teacherRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              TeacherRepository teacherRepository) {
        this.applicationRepository = applicationRepository;
        this.teacherRepository = teacherRepository;
    }

    @Override
    public ResponseEntity<?> createApplication(ApplicationDTO applicationDTO, HttpSession session) {
        try {
            // Lấy đối tượng Teacher từ session
            Teacher teacher = (Teacher) session.getAttribute("teacher");

            // Kiểm tra nếu teacher không tồn tại trong session
            if (teacher == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Teacher not logged in");
            }

            // Xử lý nếu trạng thái là "PENDING"
            if (applicationDTO.getStatus().contains("PENDING")) {
                Application application = Application.builder()
                        .title(applicationDTO.getTitle())
                        .description(applicationDTO.getDescription())
                        .teacher(teacherRepository.getTeacherByTeacherName(teacher.getTeacherName()))
                        .status("PENDING")
                        .build();

                applicationRepository.save(application);
                applicationDTO.setTeacherName(teacher.getTeacherName());
                session.invalidate();
                return ResponseEntity.ok().body(applicationDTO);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @Override
    public ResponseEntity<ApplicationDTO> updateApplication(Long id) {
        Optional<Application> optionalApplication = applicationRepository.findById(id);

        if (optionalApplication.isPresent()) {
            Application application = optionalApplication.get();

            if ("PENDING".equals(application.getStatus())) {

                Optional<Teacher> optionalTeacher = teacherRepository.findByTeacherName(application.getTeacher().getTeacherName());

                if (optionalTeacher.isPresent()) {
                    Teacher teacher = optionalTeacher.get();

                    teacher.setStatus("ACTIVE");
                    teacherRepository.save(teacher);
                }

                application.setStatus("APPROVE");
                applicationRepository.save(application);

                ApplicationDTO applicationDTO = ApplicationDTO.builder()
                        .applicationId(application.getApplicationId())
                        .title(application.getTitle())
                        .status(application.getStatus())
                        .description(application.getDescription())
                        .teacherName(application.getTeacher().getTeacherName())
                        .build();

                return ResponseEntity.ok().body(applicationDTO);
            } else {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.notFound().build();
    }



}
