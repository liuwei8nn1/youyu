package com.youyu.auth.infrastructure.persistence.converter;

import com.youyu.auth.domain.model.Menu;
import com.youyu.auth.infrastructure.persistence.entity.MenuDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 菜单转换器
 */
@Mapper
public interface MenuConverter {

    MenuConverter INSTANCE = Mappers.getMapper(MenuConverter.class);

    /**
     * DO转领域模型
     */
    default Menu toDomain(MenuDO menuDO) {
        if (menuDO == null) {
            return null;
        }
        Menu menu = new Menu();
        menu.setId(menuDO.getId());
        menu.setParentId(menuDO.getParentId());
        menu.setName(menuDO.getName());
        menu.setPath(menuDO.getPath());
        menu.setComponent(menuDO.getComponent());
        menu.setIcon(menuDO.getIcon());
        menu.setPermissionCode(menuDO.getPermissionCode());
        menu.setType(menuDO.getType());
        menu.setTargetUserType(menuDO.getTargetUserType());
        menu.setVisible(menuDO.getVisible());
        menu.setStatus(menuDO.getStatus());
        menu.setSortOrder(menuDO.getSortOrder());
        menu.setRedirect(menuDO.getRedirect());
        menu.setCreatedAt(menuDO.getCreatedAt());
        menu.setUpdatedAt(menuDO.getUpdatedAt());
        return menu;
    }

    /**
     * 领域模型转DO
     */
    default MenuDO toDO(Menu menu) {
        if (menu == null) {
            return null;
        }
        MenuDO menuDO = new MenuDO();
        menuDO.setId(menu.getId());
        menuDO.setParentId(menu.getParentId());
        menuDO.setName(menu.getName());
        menuDO.setPath(menu.getPath());
        menuDO.setComponent(menu.getComponent());
        menuDO.setIcon(menu.getIcon());
        menuDO.setPermissionCode(menu.getPermissionCode());
        menuDO.setType(menu.getType());
        menuDO.setTargetUserType(menu.getTargetUserType());
        menuDO.setVisible(menu.getVisible());
        menuDO.setStatus(menu.getStatus());
        menuDO.setSortOrder(menu.getSortOrder());
        menuDO.setRedirect(menu.getRedirect());
        menuDO.setCreatedAt(menu.getCreatedAt());
        menuDO.setUpdatedAt(menu.getUpdatedAt());
        menuDO.setCreatedBy(menu.getCreatedBy());
        menuDO.setUpdatedBy(menu.getUpdatedBy());
        return menuDO;
    }
}
