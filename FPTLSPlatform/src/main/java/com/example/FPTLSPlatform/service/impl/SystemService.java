package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.SystemDTO;
import com.example.FPTLSPlatform.model.System;
import com.example.FPTLSPlatform.repository.SystemRepository;
import com.example.FPTLSPlatform.service.ISystemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SystemService implements ISystemService {

    private final SystemRepository systemRepository;

    public SystemService(SystemRepository systemRepository) {
        this.systemRepository = systemRepository;
    }

    @Override
    public List<SystemDTO> findAll() {
        List<System> systems = systemRepository.findAll();
        return systems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public SystemDTO findByName(String name) {
        System system = systemRepository.findByName(name);
        return SystemDTO.builder().name(system.getName()).id(system.getId()).build();
    }

    @Override
    public SystemDTO createParam(SystemDTO systemDTO) {
        System system = convertToSystem(systemDTO);
        systemRepository.save(system);
        return convertToDTO(system);
    }

    @Override
    public SystemDTO updateParam(Long id, SystemDTO systemDTO) {
        System system = systemRepository.getReferenceById(id);
        system.setName(systemDTO.getName());
        system.setValue(systemDTO.getValue());
        systemRepository.save(system);
        return convertToDTO(system);
    }

    @Override
    public String deleteParam(Long id) {
        System system = systemRepository.getReferenceById(id);
        systemRepository.delete(system);
        return "Delete success param with id: " + system.getId();
    }

    private SystemDTO convertToDTO(System system) {
        return SystemDTO.builder()
                .name(system.getName())
                .value(system.getValue())
                .build();
    }

    private System convertToSystem(SystemDTO systemDTO) {
        return System.builder()
                .id(systemDTO.getId())
                .name(systemDTO.getName())
                .value(systemDTO.getValue())
                .build();
    }

}
