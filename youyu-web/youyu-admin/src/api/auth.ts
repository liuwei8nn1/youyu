import request from '@/utils/request'
import type { LoginRequest, LoginResponse, MenuItem } from '@/types'

/**
 * 用户登录
 */
export function login(data: LoginRequest) {
  return request<LoginResponse>({
    url: '/auth/sso/login',
    method: 'post',
    data: new URLSearchParams(data as any),
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
}

/**
 * 刷新 token
 */
export function refreshToken(refreshToken: string) {
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
export function getUserMenus(userId: number) {
  return request<MenuItem[]>({
    url: '/auth/role-permission/getUserMenus',
    method: 'get',
    params: { userId }
  })
}
