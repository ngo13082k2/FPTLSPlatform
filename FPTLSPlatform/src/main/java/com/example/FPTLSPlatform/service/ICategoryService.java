package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.CategoryDTO;

import java.util.List;

public interface ICategoryService {
    CategoryDTO getCategory(Long id);
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);
    List<CategoryDTO> getAllCategories();
}
