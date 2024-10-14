package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.ClassDTO;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface IClassService {
    ClassDTO createClass(ClassDTO classDTO) throws GeneralSecurityException, IOException;

    ClassDTO updateClass(Long classId, ClassDTO classDTO);

    ClassDTO confirmClassCompletion(Long classId, String teacherUsername) throws Exception;

    List<ClassDTO> getClassesByCourse(String courseCode);

    ClassDTO getClassById(Long classId);
}
