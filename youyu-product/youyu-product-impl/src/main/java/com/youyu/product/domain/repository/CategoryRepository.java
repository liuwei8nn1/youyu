package com.youyu.product.domain.repository;

import java.util.List;
import java.util.Optional;

import com.youyu.product.domain.aggregate.Category;

public interface CategoryRepository {

    void save(Category category);

    Optional<Category> findById(Long categoryId);

    void update(Category category);

    List<Category> findRootCategories();

    List<Category> findByParentId(Long parentId);

    List<Category> findCategoryTree();

    boolean removeById(Long categoryId);
}