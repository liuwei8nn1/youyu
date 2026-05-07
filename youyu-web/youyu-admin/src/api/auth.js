import request from '@/utils/request'

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: '/auth/sso/login',
    method: 'post',
    data: new URLSearchParams(data),
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 刷新 token
 */
export function refreshToken(refreshToken) {
  return request({
    url: '/auth/sso/refresh',
    method: 'post',
    params: { refreshToken }
  })
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: '/auth/sso/logout',
    method: 'post'
  })
}

/**
 * 获取用户菜单
 */
export function getUserMenus(userId) {
  return request({
    url: '/auth/role-permission/getUserMenus',
    method: 'get',
    params: { userId }
  })
}
