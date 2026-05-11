import request from '@/utils/request'

/**
 * 为用户分配角色
 */
export function assignRoleToUser(data: { userIdentityId: number; roleId: number }) {
  return request<void>({
    url: '/auth/user-role/assign',
    method: 'post',
    data
  })
}

/**
 * 批量为用户分配角色
 */
export function assignRolesToUser(data: { userIdentityId: number; roleIds: number[] }) {
  return request<void>({
    url: '/auth/user-role/assignBatch',
    method: 'post',
    data
  })
}

/**
 * 撤销用户的指定角色
 */
export function revokeRoleFromUser(data: { userIdentityId: number; roleId: number }) {
  return request<void>({
    url: '/auth/user-role/revoke',
    method: 'post',
    data
  })
}

/**
 * 撤销用户的所有角色
 */
export function revokeAllRoles(userIdentityId: number) {
  return request<void>({
    url: '/auth/user-role/revokeAll',
    method: 'post',
    params: { userIdentityId }
  })
}

/**
 * 查询用户的角色ID列表（通过 userId + userType）
 */
export function getUserRoles(userId: number, userType: number) {
  return request<number[]>({
    url: '/auth/user-role/roles/byUser',
    method: 'get',
    params: { userId, userType }
  })
}

/**
 * 检查用户是否拥有指定角色
 */
export function hasRole(userIdentityId: number, roleId: number) {
  return request<boolean>({
    url: '/auth/user-role/hasRole',
    method: 'get',
    params: { userIdentityId, roleId }
  })
}
