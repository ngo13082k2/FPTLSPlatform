package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.DocumentDTO;
import com.example.FPTLSPlatform.service.impl.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }


    @PostMapping
    public ResponseEntity<DocumentDTO> createDocument(
            @RequestPart("document") String documentJson,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {
        String courseCode = (String) request.getSession().getAttribute("course_code");

        if (courseCode == null) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            DocumentDTO documentDTO = objectMapper.readValue(documentJson, DocumentDTO.class);

            documentDTO.setCourseCode(courseCode);

            DocumentDTO createdDocument = documentService.createDocument(documentDTO, file, request);

            return ResponseEntity.ok(createdDocument);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> updateDocument(
            @PathVariable Long id,
            @RequestPart("document") String documentJson,
            @RequestPart("file") MultipartFile file) throws IOException {
        try {
            DocumentDTO documentDTO = objectMapper.readValue(documentJson, DocumentDTO.class);

            DocumentDTO updatedDocument = documentService.updateDocument(id, documentDTO, file);

            return ResponseEntity.ok(updatedDocument);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }


    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{courseCode}/documents")
    public ResponseEntity<DocumentDTO> createDocumentByCourseCode(
            @PathVariable("courseCode") String courseCode,
            @RequestPart("document") String documentJson,
            @RequestPart("file") MultipartFile file) {
        try {
            DocumentDTO documentDTO = objectMapper.readValue(documentJson, DocumentDTO.class);

            DocumentDTO createdDocument = documentService.createDocumentByCourseCode(courseCode, documentDTO, file);

            return ResponseEntity.status(201).body(createdDocument);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(null);
        }
    }
    @GetMapping("/{courseCode}/documents")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByCourseCode(@PathVariable("courseCode") String courseCode) {
        try {
            List<DocumentDTO> documents = documentService.getDocumentsByCourseCode(courseCode);
            return ResponseEntity.ok(documents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(null);
        }
    }



}
