import request from '@/utils/request'
import type { Role } from '@/types'

/**
 * 获取所有角色
 */
export function getRoleList() {
  return request<Role[]>({
    url: '/auth/role-permission/roles',
    method: 'get'
  })
}

/**
 * 获取角色详情
 */
export function getRole(id: number) {
  return request<Role>({
    url: `/auth/role-permission/roles/${id}`,
    method: 'get'
  })
}

/**
 * 创建角色
 */
export function createRole(data: Partial<Role>) {
  return request<void>({
    url: '/auth/role-permission/createRole',
    method: 'post',
    data
  })
}

/**
 * 更新角色
 */
export function updateRole(id: number, data: Partial<Role>) {
  return request<void>({
    url: `/auth/role-permission/updateRole`,
    method: 'post',
    data: { ...data, roleId: id }
  })
}

/**
 * 删除角色
 */
export function deleteRole(id: number) {
  return request<void>({
    url: `/auth/role-permission/deleteRole?roleId=${id}`,
    method: 'post'
  })
}

/**
 * 获取角色的菜单ID列表
 */
export function getRoleMenus(roleId: number) {
  return request<number[]>({
    url: `/auth/role-permission/getRoleMenus?roleId=${roleId}`,
    method: 'get'
  })
}

/**
 * 给角色分配菜单
 */
export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return request<void>({
    url: '/auth/role-permission/assignMenus',
    method: 'post',
    data: { roleId, menuIds }
  })
}
