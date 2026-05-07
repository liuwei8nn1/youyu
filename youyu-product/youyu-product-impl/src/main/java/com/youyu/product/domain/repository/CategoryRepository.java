package com.youyu.product.domain.repository;

import com.youyu.product.domain.model.CategoryAggregate;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    void save(CategoryAggregate category);

    Optional<CategoryAggregate> findById(Long categoryId);

    void update(CategoryAggregate category);

    List<CategoryAggregate> findRootCategories();

    List<CategoryAggregate> findByParentId(Long parentId);

    List<CategoryAggregate> findCategoryTree();

    boolean removeById(Long categoryId);
}