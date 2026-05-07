package com.youyu.auth.infrastructure.persistence.repository;

import com.youyu.auth.domain.repository.RoleMenuRepository;
import com.youyu.auth.infrastructure.persistence.entity.RoleMenuDO;
import com.youyu.auth.infrastructure.persistence.mapper.RoleMenuMapper;
import com.youyu.common.util.CollectionUtil;
import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色-菜单关联仓储实现
 */
@Slf4j
@Repository
public class RoleMenuRepositoryImpl extends BaseRepositoryImpl<RoleMenuDO, RoleMenuMapper, Long> implements RoleMenuRepository {

    @Override
    public List<Long> findMenuIdsByRoleId(Long roleId) {
        SmartQueryWrapper<RoleMenuDO> wrapper = new SmartQueryWrapper<RoleMenuDO>()
                .eq("role_id", roleId);
        List<RoleMenuDO> roleMenus = baseDao.selectList(wrapper);
        return CollectionUtil.toList(roleMenus, RoleMenuDO::getMenuId);
    }

    @Override
    public List<Long> findMenuIdsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        SmartQueryWrapper<RoleMenuDO> wrapper = new SmartQueryWrapper<RoleMenuDO>()
                .in("role_id", roleIds);
        List<RoleMenuDO> roleMenus = baseDao.selectList(wrapper);
        // 去重
        Set<Long> menuIdSet = new HashSet<>();
        for (RoleMenuDO roleMenu : roleMenus) {
            menuIdSet.add(roleMenu.getMenuId());
        }
        return new ArrayList<>(menuIdSet);
    }

    @Override
    @Transactional
    public void deleteByRoleId(Long roleId) {
        SmartQueryWrapper<RoleMenuDO> wrapper = new SmartQueryWrapper<RoleMenuDO>()
                .eq("role_id", roleId);
        baseDao.delete(wrapper);
        log.info("删除角色菜单关联成功，roleId: {}", roleId);
    }

    @Override
    @Transactional
    public void batchInsert(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }

        List<RoleMenuDO> roleMenus = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Long menuId : menuIds) {
            RoleMenuDO roleMenu = new RoleMenuDO();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenu.initTime(now);
            roleMenus.add(roleMenu);
        }

        for (RoleMenuDO roleMenu : roleMenus) {
            baseDao.insert(roleMenu);
        }
        log.info("批量插入角色菜单关联成功，roleId: {}, count: {}", roleId, menuIds.size());
    }
}
