import request from '@/utils/request'

/**
 * 获取部门树
 */
export function getDeptTree() {
  return request({
    url: '/user/dept/tree',
    method: 'get'
  })
}

/**
 * 获取所有部门（平铺）
 */
export function getAllDepts() {
  return request({
    url: '/user/dept/all',
    method: 'get'
  })
}

/**
 * 创建部门
 */
export function createDept(data) {
  return request({
    url: '/user/dept/create',
    method: 'post',
    data
  })
}

/**
 * 更新部门
 */
export function updateDept(data) {
  return request({
    url: '/user/dept/update',
    method: 'post',
    data
  })
}

/**
 * 删除部门
 */
export function deleteDept(deptId) {
  return request({
    url: '/user/dept/delete',
    method: 'post',
    params: { deptId }
  })
}
