package com.youyu.auth.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youyu.auth.domain.model.Menu;
import com.youyu.auth.domain.repository.MenuRepository;
import com.youyu.auth.infrastructure.persistence.converter.MenuConverter;
import com.youyu.auth.infrastructure.persistence.entity.MenuDO;
import com.youyu.auth.infrastructure.persistence.mapper.MenuMapper;
import com.youyu.common.util.CollectionUtil;
import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单仓储实现
 */
@Slf4j
@Repository
public class MenuRepositoryImpl extends BaseRepositoryImpl<MenuDO, MenuMapper, Long> implements MenuRepository {

    @Override
    public Long save(Menu menu) {
        MenuDO menuDO = MenuConverter.INSTANCE.toDO(menu);
        this.save(menuDO);
        log.info("菜单保存成功，menuId: {}", menuDO.getId());
        return menuDO.getId();
    }

    @Override
    public void update(Menu menu) {
        MenuDO menuDO = MenuConverter.INSTANCE.toDO(menu);
        baseDao.updateById(menuDO);
        log.info("菜单更新成功，menuId: {}", menuDO.getId());
    }

    @Override
    public Optional<Menu> findById(Long id) {
        MenuDO menuDO = baseDao.selectById(id);
        return Optional.ofNullable(MenuConverter.INSTANCE.toDomain(menuDO));
    }

    @Override
    public List<Menu> findAllAndOrder() {
        List<MenuDO> menuDOList = baseDao.selectList(new SmartQueryWrapper<MenuDO>()
                        .orderByAsc(MenuDO.SORT_ORDER));
        return CollectionUtil.toList(menuDOList, MenuConverter.INSTANCE::toDomain);
    }

    @Override
    public List<Menu> listAll() {
        List<MenuDO> menuDOList = baseDao.selectList(null);
        return CollectionUtil.toList(menuDOList, MenuConverter.INSTANCE::toDomain);
    }

    @Override
    public List<Menu> findByParentId(Long parentId) {
        SmartQueryWrapper<MenuDO> wrapper = new SmartQueryWrapper<MenuDO>()
                .eq(MenuDO.PARENT_ID, parentId)
                .orderByAsc(MenuDO.SORT_ORDER);
        List<MenuDO> menuDOList = baseDao.selectList(wrapper);
        return CollectionUtil.toList(menuDOList, MenuConverter.INSTANCE::toDomain);
    }

    @Override
    public List<Menu> findByPermissionCode(String permissionCode) {
        SmartQueryWrapper<MenuDO> wrapper = new SmartQueryWrapper<MenuDO>()
                .eq(MenuDO.PERMISSION_CODE, permissionCode);
        List<MenuDO> menuDOList = baseDao.selectList(wrapper);
        return CollectionUtil.toList(menuDOList, MenuConverter.INSTANCE::toDomain);
    }

    @Override
    public List<Menu> findByPermissionCodes(Set<String> permissionCodes) {
        if (CollectionUtils.isEmpty(permissionCodes)) {
            return Collections.emptyList();
        }
        SmartQueryWrapper<MenuDO> wrapper = new SmartQueryWrapper<MenuDO>()
                .in(MenuDO.PERMISSION_CODE, permissionCodes)
                .eq(MenuDO.STATUS, 1)
                .orderByAsc(MenuDO.SORT_ORDER);
        List<MenuDO> menuDOList = this.selectList(wrapper);
        return CollectionUtil.toList(menuDOList, MenuConverter.INSTANCE::toDomain);
    }

    @Override
    public List<Menu> findVisibleMenus() {
        SmartQueryWrapper<MenuDO> wrapper = new SmartQueryWrapper<MenuDO>()
                .eq(MenuDO.VISIBLE, 1)
                .eq(MenuDO.STATUS, 1)
                .orderByAsc(MenuDO.SORT_ORDER);
        List<MenuDO> menuDOList = this.selectList(wrapper);
        return CollectionUtil.toList(menuDOList, MenuConverter.INSTANCE::toDomain);
    }

    @Override
    public List<Menu> findByTargetUserType(Integer targetUserType) {
        SmartQueryWrapper<MenuDO> wrapper = new SmartQueryWrapper<MenuDO>()
                .eq(MenuDO.TARGET_USER_TYPE, targetUserType)
                .eq(MenuDO.VISIBLE, 1)
                .eq(MenuDO.STATUS, 1)
                .orderByAsc(MenuDO.SORT_ORDER);
        List<MenuDO> menuDOList = this.selectList(wrapper);
        return CollectionUtil.toList(menuDOList, MenuConverter.INSTANCE::toDomain);
    }


    @Override
    public boolean hasChildren(Long parentId) {
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<MenuDO>()
                .eq(MenuDO::getParentId, parentId);
        return baseDao.selectCount(wrapper) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        int result = baseDao.deleteById(id);
        if (result > 0) {
            log.info("菜单删除成功，menuId: {}", id);
        }
        return result > 0;
    }
}
