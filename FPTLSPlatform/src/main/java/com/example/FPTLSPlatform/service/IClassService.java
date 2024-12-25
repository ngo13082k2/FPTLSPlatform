package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.dto.StudentDTO;
import com.example.FPTLSPlatform.model.enums.ClassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface IClassService {
    ClassDTO createClass(ClassDTO classDTO, MultipartFile image) throws GeneralSecurityException, IOException;

    ClassDTO updateClass(Long classId, ClassDTO classDTO, MultipartFile image) throws IOException;

    ClassDTO confirmClassCompletion(Long classId, String teacherUsername) throws Exception;

    List<ClassDTO> getClassesByCourse(String courseCode);

    ClassDTO getClassById(Long classId);

    List<ClassDTO> getAllClasses();

    List<ClassDTO> getClassesByTeacherName(String teacherName);

    Page<StudentDTO> getAllStudentsInClass(Long classId, Pageable pageable);
    List<ClassDTO> getAllClassesByCurrentTeacher();
    List<ClassDTO> getClassByMajor();
    List<ClassDTO> getClassesByStatusCompleted();
    Map<String, Map<YearMonth, Long>> getClassesGroupedByStatusAndMonths(Integer year);
    long getTotalClasses();
    List<ClassDTO> getClassesByStatusAndMonthDetailed(ClassStatus status, int year, Integer month);
    String cancelClass(Long classId);
    void assignTeacherToClass(Long classId) ;
    List<ClassDTO> getClassesByTeacherName();
    ClassDTO updateClassLocation(Long classId, String location);
    List<ClassDTO> getAllClassesWithoutTeacher();
    List<ClassDTO> getAllClassesWithTeacher();
}
