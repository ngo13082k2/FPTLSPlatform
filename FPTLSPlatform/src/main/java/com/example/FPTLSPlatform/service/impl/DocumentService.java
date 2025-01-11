package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.DocumentDTO;
import com.example.FPTLSPlatform.model.Course;
import com.example.FPTLSPlatform.model.Document;
import com.example.FPTLSPlatform.repository.CourseRepository;
import com.example.FPTLSPlatform.repository.DocumentRepository;
import com.example.FPTLSPlatform.service.IDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService implements IDocumentService {
    private final DocumentRepository documentRepository;
    private final CloudinaryService cloudinaryService;
    private final CourseRepository courseRepository;


    public DocumentService(DocumentRepository documentRepository, CloudinaryService cloudinaryService, CourseRepository courseRepository) {
        this.documentRepository = documentRepository;
        this.cloudinaryService = cloudinaryService;
        this.courseRepository = courseRepository;
    }
    @Override
    public DocumentDTO createDocument(DocumentDTO documentDTO, MultipartFile file, HttpServletRequest request) throws IOException {
        String courseCode = (String) request.getSession().getAttribute("course_code");

        if (courseCode == null) {
            throw new IllegalArgumentException("Course code not found in session.");
        }
        documentDTO.setCourseCode(courseCode);
        Course course = courseRepository.findByCourseCode(documentDTO.getCourseCode())
                .orElseThrow(() -> new IllegalArgumentException("Course with the given course_code not found"));
        String filePath = cloudinaryService.uploadFile(file);
        documentDTO.setFilePath(filePath);

        Document document = mapDTOToEntity(documentDTO);
        document.setCourse(course);
        Document savedDocument = documentRepository.save(document);

        return mapEntityToDTO(savedDocument);
    }

    @Override
    public DocumentDTO updateDocument(Long id, DocumentDTO documentDTO, MultipartFile file) throws IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        if (file != null && !file.isEmpty()) {
            String filePath = cloudinaryService.uploadFile(file);
            document.setFilePath(filePath);
        }

        document.setTitle(documentDTO.getTitle());
        document.setContent(documentDTO.getContent());
        document.setCompletedSlots(documentDTO.getCompletedSlots());

        Document updatedDocument = documentRepository.save(document);
        return mapEntityToDTO(updatedDocument);
    }

    @Override
    public List<DocumentDTO> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentDTO getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        return mapEntityToDTO(document);
    }

    @Override
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new IllegalArgumentException("Document not found");
        }
        documentRepository.deleteById(id);
    }
    @Override
    public DocumentDTO createDocumentByCourseCode(String courseCode, DocumentDTO documentDTO, MultipartFile file) throws IOException {
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new IllegalArgumentException("Course with the given course_code not found"));

        String filePath = cloudinaryService.uploadFile(file);
        documentDTO.setFilePath(filePath);

        Document document = mapDTOToEntity(documentDTO);
        document.setCourse(course);

        Document savedDocument = documentRepository.save(document);

        return mapEntityToDTO(savedDocument);
    }
    public List<DocumentDTO> getDocumentsByCourseCode(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new IllegalArgumentException("Course with the given course_code not found"));

        List<Document> documents = documentRepository.findByCourse(course);

        return documents.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }



    private Document mapDTOToEntity(DocumentDTO documentDTO) {
        Document document = new Document();
        document.setId(documentDTO.getId());
        document.setTitle(documentDTO.getTitle());
        document.setContent(documentDTO.getContent());
        document.setFilePath(documentDTO.getFilePath());
        document.setCompletedSlots(documentDTO.getCompletedSlots());
        return document;
    }

    private DocumentDTO mapEntityToDTO(Document document) {
        return DocumentDTO.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .courseCode(document.getCourse().getCourseCode())
                .filePath(document.getFilePath())
                .completedSlots(document.getCompletedSlots())

                .build();
    }
}
