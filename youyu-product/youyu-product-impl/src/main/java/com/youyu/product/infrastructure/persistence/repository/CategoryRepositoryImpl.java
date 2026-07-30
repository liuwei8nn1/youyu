package com.youyu.product.infrastructure.persistence.repository;

import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import com.youyu.product.domain.model.Category;
import com.youyu.product.domain.repository.CategoryRepository;
import com.youyu.product.infrastructure.persistence.converter.CategoryConverter;
import com.youyu.product.infrastructure.persistence.entity.CategoryDO;
import com.youyu.product.infrastructure.persistence.mapper.CategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class CategoryRepositoryImpl extends BaseRepositoryImpl<CategoryDO, CategoryMapper, Long> implements CategoryRepository {

    @Override
    public void save(Category category) {
        CategoryDO categoryDO = CategoryConverter.INSTANCE.toDO(category);
        if (categoryDO.getId() == null) {
            baseDao.insert(categoryDO);
            category.setId(categoryDO.getId());
            log.info("分类保存成功，categoryId: {}", categoryDO.getId());
        } else {
            baseDao.updateById(categoryDO);
            log.info("分类更新成功，categoryId: {}", categoryDO.getId());
        }
    }

    @Override
    public Optional<Category> findById(Long categoryId) {
        CategoryDO categoryDO = baseDao.selectById(categoryId);
        return Optional.ofNullable(CategoryConverter.INSTANCE.toAggregate(categoryDO));
    }

    @Override
    public void update(Category category) {
        CategoryDO categoryDO = CategoryConverter.INSTANCE.toDO(category);
        baseDao.updateById(categoryDO);
        log.info("分类更新成功，categoryId: {}", categoryDO.getId());
    }

    @Override
    public List<Category> findRootCategories() {
        SmartQueryWrapper<CategoryDO> wrapper = new SmartQueryWrapper<CategoryDO>()
                .eq(CategoryDO.PARENT_ID, 0L)
                .orderByAsc(CategoryDO.SORT_ORDER);
        List<CategoryDO> doList = baseDao.selectList(wrapper);
        List<Category> result = new ArrayList<>();
        for (CategoryDO categoryDO : doList) {
            result.add(CategoryConverter.INSTANCE.toAggregate(categoryDO));
        }
        return result;
    }

    @Override
    public List<Category> findByParentId(Long parentId) {
        SmartQueryWrapper<CategoryDO> wrapper = new SmartQueryWrapper<CategoryDO>()
                .eq(CategoryDO.PARENT_ID, parentId)
                .orderByAsc(CategoryDO.SORT_ORDER);
        List<CategoryDO> doList = baseDao.selectList(wrapper);
        List<Category> result = new ArrayList<>();
        for (CategoryDO categoryDO : doList) {
            result.add(CategoryConverter.INSTANCE.toAggregate(categoryDO));
        }
        return result;
    }

    @Override
    public List<Category> findCategoryTree() {
        List<Category> roots = findRootCategories();
        buildTree(roots);
        return roots;
    }

    private void buildTree(List<Category> parents) {
        for (Category parent : parents) {
            List<Category> children = findByParentId(parent.getId());
            if (!children.isEmpty()) {
                parent.getChildren().addAll(children);
                buildTree(children);
            }
        }
    }

    @Override
    public boolean removeById(Long categoryId) {
        int result = baseDao.deleteById(categoryId);
        if (result > 0) {
            log.info("分类删除成功，categoryId: {}", categoryId);
        }
        return result > 0;
    }
}
