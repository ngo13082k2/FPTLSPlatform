package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.SystemDTO;
import com.example.FPTLSPlatform.service.ISystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final ISystemService systemService;

    @Autowired
    public SystemController(ISystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/params")
    public ResponseEntity<List<SystemDTO>> getAllParams() {
        List<SystemDTO> params = systemService.findAll();
        return ResponseEntity.ok(params);
    }

    @GetMapping("/param/{name}")
    public ResponseEntity<SystemDTO> getParamByName(@PathVariable String name) {
        SystemDTO param = systemService.findByName(name);
        return ResponseEntity.ok(param);
    }

    @PostMapping("/param")
    public ResponseEntity<SystemDTO> createParam(@RequestBody SystemDTO systemDTO) {
        SystemDTO param = systemService.createParam(systemDTO);
        return ResponseEntity.ok(param);
    }

    @PutMapping("/param")
    public ResponseEntity<List<SystemDTO>> updateParam(@RequestBody List<SystemDTO> systemDTOS) {
        List<SystemDTO> param = systemService.updateParam(systemDTOS);
        return ResponseEntity.ok(param);
    }

    @DeleteMapping("/param/{id}")
    public ResponseEntity<String> deleteParam(@PathVariable Long id) {
        String param = systemService.deleteParam(id);
        return ResponseEntity.ok(param);
    }

    @PostMapping("param/default")
    public ResponseEntity<?> createDefaultParam() {
        List<SystemDTO> params = systemService.createDefaultParam();
        return ResponseEntity.ok(params);
    }
}
