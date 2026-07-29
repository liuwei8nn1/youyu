package com.youyu.product.domain.aggregate;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Category implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    private String categoryName;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private Integer status;
    private List<Category> children = new ArrayList<>();

    public Category() {
    }

    public static Category createRoot(String categoryName, Integer sortOrder) {
        Category category = new Category();
        category.categoryName = categoryName;
        category.parentId = 0L;
        category.level = 1;
        category.sortOrder = sortOrder != null ? sortOrder : 0;
        category.status = 1;
        return category;
    }

    public static Category createChild(String categoryName, Long parentId,
                                                                      Integer level, Integer sortOrder) {
        if (parentId == null || parentId <= 0) {
            throw new IllegalArgumentException("父分类ID必须大于0");
        }
        if (level == null || level <= 1) {
            throw new IllegalArgumentException("子分类层级必须大于1");
        }

        Category category = new Category();
        category.categoryName = categoryName;
        category.parentId = parentId;
        category.level = level;
        category.sortOrder = sortOrder != null ? sortOrder : 0;
        category.status = 1;
        return category;
    }

    public static Category restore(Long id, String categoryName, Long parentId,
                                                                  Integer level, Integer sortOrder, Integer status) {
        Category category = new Category();
        category.id = id;
        category.categoryName = categoryName;
        category.parentId = parentId != null ? parentId : 0L;
        category.level = level != null ? level : 1;
        category.sortOrder = sortOrder != null ? sortOrder : 0;
        category.status = status != null ? status : 1;
        return category;
    }

    public void validate() {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        if (level == null || level < 1) {
            throw new IllegalArgumentException("分类层级必须大于等于1");
        }
        if (sortOrder == null || sortOrder < 0) {
            throw new IllegalArgumentException("排序号不能为负数");
        }
    }

    public void enable() {
        if (this.status != null && this.status == 1) {
            throw new IllegalStateException("分类已处于启用状态");
        }
        this.status = 1;
    }

    public void disable() {
        if (this.status != null && this.status == 0) {
            throw new IllegalStateException("分类已处于禁用状态");
        }
        this.status = 0;
    }

    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    public boolean isRoot() {
        return this.parentId != null && this.parentId == 0;
    }

    public void addChild(Category child) {
        if (child == null) {
            throw new IllegalArgumentException("子分类不能为空");
        }
        if (!Long.valueOf(this.id).equals(child.getParentId())) {
            throw new IllegalArgumentException("子分类的父ID与当前分类ID不匹配");
        }
        this.children.add(child);
    }

    public void removeChild(Long childId) {
        if (childId == null) {
            throw new IllegalArgumentException("子分类ID不能为空");
        }
        this.children.removeIf(child -> child.getId() != null && child.getId().equals(childId));
    }

    public void updateInfo(String categoryName, Integer sortOrder) {
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            this.categoryName = categoryName;
        }
        if (sortOrder != null && sortOrder >= 0) {
            this.sortOrder = sortOrder;
        }
        validate();
    }
}