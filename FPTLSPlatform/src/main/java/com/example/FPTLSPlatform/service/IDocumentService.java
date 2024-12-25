package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.DocumentDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IDocumentService {
    DocumentDTO createDocument(DocumentDTO documentDTO, MultipartFile file, HttpServletRequest request) throws IOException;

    DocumentDTO updateDocument(Long id, DocumentDTO documentDTO, MultipartFile file) throws IOException;
    List<DocumentDTO> getAllDocuments();
    DocumentDTO getDocumentById(Long id);
    void deleteDocument(Long id);
}
