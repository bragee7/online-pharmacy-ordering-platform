package com.medicare.pharmacy.service;

import com.medicare.pharmacy.dto.CategoryDto;
import com.medicare.pharmacy.entity.Category;
import com.medicare.pharmacy.exception.BadRequestException;
import com.medicare.pharmacy.exception.ResourceNotFoundException;
import com.medicare.pharmacy.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<CategoryDto> list() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto get(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return CategoryDto.from(category);
    }

    @Transactional
    public CategoryDto create(CategoryDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Category name is required");
        }
        String name = dto.getName().trim();
        if (categoryRepository.existsByName(name)) {
            throw new BadRequestException("Category already exists");
        }
        Category saved = categoryRepository.save(Category.builder()
                .name(name)
                .description(dto.getDescription())
                .build());
        auditService.log(null, "CREATE_CATEGORY", "Category", saved.getId(),
                "Created category: " + saved.getName());
        return CategoryDto.from(saved);
    }

    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        if (dto.getName() != null && !dto.getName().isBlank()) {
            String newName = dto.getName().trim();
            if (!newName.equalsIgnoreCase(category.getName())
                    && categoryRepository.existsByName(newName)) {
                throw new BadRequestException("Category already exists");
            }
            category.setName(newName);
        }
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        Category saved = categoryRepository.save(category);
        auditService.log(null, "UPDATE_CATEGORY", "Category", saved.getId(),
                "Updated category: " + saved.getName());
        return CategoryDto.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Cannot delete: medicines exist in this category");
        }
        auditService.log(null, "DELETE_CATEGORY", "Category", id,
                "Deleted category: " + category.getName());
    }
}
