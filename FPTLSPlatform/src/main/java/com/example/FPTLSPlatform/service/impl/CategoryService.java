package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.CategoryDTO;
import com.example.FPTLSPlatform.model.Category;
import com.example.FPTLSPlatform.repository.CategoryRepository;
import com.example.FPTLSPlatform.service.ICategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    public CategoryDTO getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return mapToDto(category);
    }

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = mapToEntity(categoryDTO);
        category = categoryRepository.save(category);
        return mapToDto(category);
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        existingCategory.setName(categoryDTO.getName());

        Category updatedCategory = categoryRepository.save(existingCategory);
        return mapToDto(updatedCategory);
    }
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    public long getTotalCategories() {
        return categoryRepository.count();
    }
    private CategoryDTO mapToDto(Category category) {
        return new CategoryDTO(
                category.getCategoryId(),
                category.getName()
        );
    }

    private Category mapToEntity(CategoryDTO categoryDTO) {
        return Category.builder()
                .categoryId(categoryDTO.getCategoryId())
                .name(categoryDTO.getName())
                .build();
    }
}
