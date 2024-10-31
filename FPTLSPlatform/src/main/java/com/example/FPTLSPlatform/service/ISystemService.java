package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.SystemDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ISystemService {

    List<SystemDTO> findAll();

    SystemDTO findByName(String name);

    SystemDTO createParam(SystemDTO systemDTO);

    SystemDTO updateParam(Long id, SystemDTO systemDTO);

    String deleteParam(Long id);
}
