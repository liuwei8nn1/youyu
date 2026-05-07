import request from '@/utils/request'

/**
 * 获取所有角色
 */
export function getRoleList() {
  return request({
    url: '/auth/role-permission/roles',
    method: 'get'
  })
}

/**
 * 获取角色详情
 */
export function getRole(id) {
  return request({
    url: `/auth/role-permission/roles/${id}`,
    method: 'get'
  })
}

/**
 * 创建角色
 */
export function createRole(data) {
  return request({
    url: '/auth/role-permission/createRole',
    method: 'post',
    data
  })
}

/**
 * 更新角色
 */
export function updateRole(id, data) {
  return request({
    url: `/auth/role-permission/updateRole`,
    method: 'post',
    data: { ...data, roleId: id }
  })
}

/**
 * 删除角色
 */
export function deleteRole(id) {
  return request({
    url: `/auth/role-permission/deleteRole?roleId=${id}`,
    method: 'post'
  })
}

/**
 * 获取角色的菜单ID列表
 */
export function getRoleMenus(roleId) {
  return request({
    url: `/auth/role-permission/getRoleMenus?roleId=${roleId}`,
    method: 'get'
  })
}

/**
 * 给角色分配菜单
 */
export function assignRoleMenus(roleId, menuIds) {
  return request({
    url: '/auth/role-permission/assignMenus',
    method: 'post',
    data: { roleId, menuIds }
  })
}
