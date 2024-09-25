package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.ClassDTO;

import java.util.List;

public interface IClassService   {
    ClassDTO createClass(ClassDTO classDTO);
    ClassDTO updateClass(Long classId, ClassDTO classDTO);
    List<ClassDTO> getClassesByCourse(String courseCode);
    ClassDTO getClassById(Long classId);
}
