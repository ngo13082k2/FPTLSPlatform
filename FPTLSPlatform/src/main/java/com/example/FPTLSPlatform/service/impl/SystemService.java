package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.SystemDTO;
import com.example.FPTLSPlatform.model.System;
import com.example.FPTLSPlatform.repository.SystemRepository;
import com.example.FPTLSPlatform.service.ISystemService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    public List<SystemDTO> updateParam(List<SystemDTO> systemDTOS) {
        for (SystemDTO systemDTO : systemDTOS) {
            System system = systemRepository.findByName(systemDTO.getName());
            system.setValue(systemDTO.getValue());
            systemRepository.save(system);
        }
        return systemDTOS;
    }

    @Override
    public String deleteParam(Long id) {
        System system = systemRepository.getReferenceById(id);
        systemRepository.delete(system);
        return "Delete success param with id: " + system.getId();
    }

    @Override
    public List<SystemDTO> createDefaultParam() {
        List<System> defaultSystems = Arrays.asList(
                new System(1L, "check_time_before_start", "2"),
                new System(2L, "minimum_required_percentage", "0.8"),
                new System(3L, "discount_percentage", "0.2"),
                new System(4L, "feedback_deadline", "7"),
                new System(5L, "demo_mode", "false"),
                new System(6L, "demo_adjust_active_time", "0"),
                new System(7L, "demo_adjust_start_time", "0"),
                new System(8L, "demo_adjust_end_time", "0")
        );

        List<System> existingSystems = systemRepository.findAll();
        Map<Long, System> existingSystemsMap = existingSystems.stream()
                .collect(Collectors.toMap(System::getId, system -> system));

        for (System defaultSystem : defaultSystems) {
            System existingSystem = existingSystemsMap.get(defaultSystem.getId());
            if (existingSystem != null) {
                existingSystem.setValue(defaultSystem.getValue());
            } else {
                existingSystems.add(defaultSystem);
            }
        }

        systemRepository.saveAll(existingSystems);

        return existingSystems.stream().map(this::convertToDTO).collect(Collectors.toList());
    }


    private SystemDTO convertToDTO(System system) {
        return SystemDTO.builder()
                .id(system.getId())
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
