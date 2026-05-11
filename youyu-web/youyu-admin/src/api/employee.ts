import request from '@/utils/request'
import type { Employee, PageRequest, PageResponse } from '@/types'

/**
 * 分页查询员工列表
 */
export function getEmployeeList(params: PageRequest & Partial<Employee>) {
  return request<PageResponse<Employee>>({
    url: '/user/employee/list',
    method: 'get',
    params
  })
}

/**
 * 获取员工详情
 */
export function getEmployee(id: number) {
  return request<Employee>({
    url: `/user/employee/${id}`,
    method: 'get'
  })
}

/**
 * 创建员工
 */
export function createEmployee(data: Partial<Employee>) {
  return request<void>({
    url: '/user/employee/create',
    method: 'post',
    data
  })
}

/**
 * 更新员工
 */
export function updateEmployee(data: Partial<Employee>) {
  return request<void>({
    url: '/user/employee/update',
    method: 'post',
    data
  })
}

/**
 * 删除员工
 */
export function deleteEmployee(id: number) {
  return request<void>({
    url: '/user/employee/delete',
    method: 'post',
    params: { id }
  })
}

/**
 * 获取员工角色（从 Auth 服务查询）
 */
export function getEmployeeRoles(id: number) {
  return request<number[]>({
    url: `/auth/user-role/roles/byUser`,
    method: 'get',
    params: { userId: id, userType: 2 }  // 员工属于 enterprise 类型
  })
}
