package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.CourseDTO;
import com.example.FPTLSPlatform.model.Category;
import com.example.FPTLSPlatform.model.Course;
import com.example.FPTLSPlatform.repository.CategoryRepository;
import com.example.FPTLSPlatform.repository.CourseRepository;
import com.example.FPTLSPlatform.service.ICourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class CourseService implements ICourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public CourseService(CourseRepository courseRepository, CategoryRepository categoryRepository, CloudinaryService cloudinaryService) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.cloudinaryService = cloudinaryService;
    }



    public CourseDTO createCourse(CourseDTO courseDTO, MultipartFile image) throws IOException {
        Course course = mapDTOToEntity(courseDTO, image);
        Course savedCourse = courseRepository.save(course);
        return mapEntityToDTO(savedCourse);
    }

    public CourseDTO updateCourse(String courseCode, CourseDTO courseDTO, MultipartFile image) throws IOException {
        Course course = courseRepository.findById(courseCode)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (courseDTO.getName() != null) {
            course.setName(courseDTO.getName());
        }
        if (courseDTO.getDescription() != null) {
            course.setDescription(courseDTO.getDescription());
        }
        if (courseDTO.getStatus() != null) {
            course.setStatus(courseDTO.getStatus());
        }
        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            course.setImage(imageUrl);
        }
        if (courseDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(courseDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            course.setCategories(category);
        }
        Course updatedCourse = courseRepository.save(course);
        return mapEntityToDTO(updatedCourse);
    }
    public void deleteCourse(String courseCode) {
        Course course = courseRepository.findById(courseCode)
                .orElseThrow(() -> new RuntimeException("Course not found with courseCode: " + courseCode));

        courseRepository.delete(course);
    }
    public List<CourseDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        if (courses.isEmpty()) {
            throw new RuntimeException("No courses found");
        }
        return courses.stream().map(this::mapEntityToDTO).toList();
    }

    private Course mapDTOToEntity(CourseDTO courseDTO, MultipartFile image) throws IOException {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(image);
        }
        Category category = categoryRepository.findById(courseDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return Course.builder()
                .courseCode(courseDTO.getCourseCode())
                .name(courseDTO.getName())
                .description(courseDTO.getDescription())
                .status(courseDTO.getStatus())
                .image(imageUrl)
                .categories(category)
                .build();
    }

    private CourseDTO mapEntityToDTO(Course course) {
        return CourseDTO.builder()
                .courseCode(course.getCourseCode())
                .name(course.getName())
                .description(course.getDescription())
                .status(course.getStatus())
                .image(course.getImage())
                .categoryId(course.getCategories().getCategoryId())
                .categoryName(course.getCategories().getName())
                .build();
    }
}
