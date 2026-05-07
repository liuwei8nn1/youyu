import request from '@/utils/request'

/**
 * 获取所有菜单（树形结构）
 */
export function getMenuTree() {
  return request({
    url: '/auth/role-permission/menus',
    method: 'get'
  })
}

/**
 * 获取所有菜单（平铺结构，用于分配）
 */
export function getAllMenus() {
  return request({
    url: '/auth/role-permission/menus/all',
    method: 'get'
  })
}

/**
 * 获取菜单详情
 */
export function getMenu(id) {
  return request({
    url: `/auth/role-permission/menus/${id}`,
    method: 'get'
  })
}

/**
 * 创建菜单
 */
export function createMenu(data) {
  return request({
    url: '/auth/role-permission/menus',
    method: 'post',
    data
  })
}

/**
 * 更新菜单
 */
export function updateMenu(id, data) {
  return request({
    url: `/auth/role-permission/updateMenu?menuId=${id}`,
    method: 'post',
    data
  })
}

/**
 * 删除菜单
 */
export function deleteMenu(id) {
  return request({
    url: `/auth/role-permission/deleteMenu?menuId=${id}`,
    method: 'post'
  })
}
