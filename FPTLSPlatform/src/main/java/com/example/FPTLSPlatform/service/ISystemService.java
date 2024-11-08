package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.SystemDTO;

import java.util.List;


public interface ISystemService {

    List<SystemDTO> findAll();

    SystemDTO findByName(String name);

    SystemDTO createParam(SystemDTO systemDTO);

    SystemDTO updateParam(Long id, SystemDTO systemDTO);

    String deleteParam(Long id);
}
