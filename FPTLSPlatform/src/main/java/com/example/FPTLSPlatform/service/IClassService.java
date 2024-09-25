package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.ClassDTO;

public interface IClassService   {
    ClassDTO createClass(ClassDTO classDTO);
    ClassDTO updateClass(Long classId, ClassDTO classDTO);
}
