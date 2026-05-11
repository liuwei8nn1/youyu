import request from '@/utils/request'
import type { Department } from '@/types'

/**
 * 获取部门树
 */
export function getDeptTree() {
  return request<Department[]>({
    url: '/user/dept/tree',
    method: 'get'
  })
}

/**
 * 获取所有部门（平铺）
 */
export function getAllDepts() {
  return request<Department[]>({
    url: '/user/dept/all',
    method: 'get'
  })
}

/**
 * 创建部门
 */
export function createDept(data: Partial<Department>) {
  return request<void>({
    url: '/user/dept/create',
    method: 'post',
    data
  })
}

/**
 * 更新部门
 */
export function updateDept(data: Partial<Department>) {
  return request<void>({
    url: '/user/dept/update',
    method: 'post',
    data
  })
}

/**
 * 删除部门
 */
export function deleteDept(deptId: number) {
  return request<void>({
    url: '/user/dept/delete',
    method: 'post',
    params: { deptId }
  })
}
