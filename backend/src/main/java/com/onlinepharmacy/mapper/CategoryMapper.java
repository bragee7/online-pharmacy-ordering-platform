package com.onlinepharmacy.mapper;

import com.onlinepharmacy.dto.category.CategoryResponse;
import com.onlinepharmacy.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.from(category);
    }
}