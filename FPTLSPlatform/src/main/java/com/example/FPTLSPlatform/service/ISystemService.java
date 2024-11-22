package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.SystemDTO;

import java.util.List;


public interface ISystemService {

    List<SystemDTO> findAll();

    SystemDTO findByName(String name);

    SystemDTO createParam(SystemDTO systemDTO);

    List<SystemDTO> updateParam(List<SystemDTO> systemDTOS);

    String deleteParam(Long id);

    List<SystemDTO> createDefaultParam();
}
