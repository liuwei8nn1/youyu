package com.youyu.product.infrastructure.persistence.converter;

import com.youyu.product.domain.model.Category;
import com.youyu.product.infrastructure.persistence.entity.CategoryDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CategoryConverter {

    CategoryConverter INSTANCE = Mappers.getMapper(CategoryConverter.class);

    default Category toAggregate(CategoryDO categoryDO) {
        if (categoryDO == null) {
            return null;
        }
        return Category.restore(
            categoryDO.getId(),
            categoryDO.getCategoryName(),
            categoryDO.getParentId(),
            categoryDO.getLevel(),
            categoryDO.getSortOrder(),
            categoryDO.getStatus()
        );
    }

    CategoryDO toDO(Category aggregate);
}