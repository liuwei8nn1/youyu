import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { ApiResponse } from '@/types'

const ENABLE_MOCK = false

// 导入mock函数
import {
  mockLogin,
  mockRefreshToken,
  mockGetUserMenus,
  mockGetMenuTree,
  mockGetAllMenus,
  mockGetMenu,
  mockCreateMenu,
  mockUpdateMenu,
  mockDeleteMenu,
  mockGetRoleList,
  mockGetRole,
  mockCreateRole,
  mockUpdateRole,
  mockDeleteRole,
  mockGetRoleMenus,
  mockAssignRoleMenus
} from '@/mock'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// Mock 适配器
if (ENABLE_MOCK) {
  const originalAdapter = service.defaults.adapter

  service.defaults.adapter = async function(config: InternalAxiosRequestConfig) {
    const url = config.url || ''
    const method = (config.method || 'get').toLowerCase()

    // Auth 接口
    if (url.includes('/auth/sso/login') && method === 'post') {
      return { data: await mockLogin(typeof config.data === 'string' ? JSON.parse(config.data) : config.data), status: 200, statusText: 'OK', headers: {}, config }
    }
    if (url.includes('/auth/sso/refresh') && method === 'post') {
      return { data: await mockRefreshToken(config.params?.refreshToken), status: 200, statusText: 'OK', headers: {}, config }
    }
    if (url.includes('/auth/sso/logout') && method === 'post') {
      return { data: { code: 200, message: 'success' }, status: 200, statusText: 'OK', headers: {}, config }
    }
    if (url.includes('/auth/role-permission/getUserMenus') && method === 'get') {
      console.log('Mock: getUserMenus called')
      const result = await mockGetUserMenus()
      console.log('Mock: getUserMenus result:', result)
      return { data: result, status: 200, statusText: 'OK', headers: {}, config }
    }

    // 菜单接口
    if (url.includes('/auth/role-permission/menus')) {
      if (url.includes('/all')) return { data: await mockGetAllMenus(), status: 200, statusText: 'OK', headers: {}, config }
      const idMatch = url.match(/\/auth\/role-permission\/menus\/(\d+)/)
      if (idMatch && method === 'get') return { data: await mockGetMenu(parseInt(idMatch[1])), status: 200, statusText: 'OK', headers: {}, config }
      if (!idMatch && method === 'get') return { data: await mockGetMenuTree(), status: 200, statusText: 'OK', headers: {}, config }
      if (method === 'post') return { data: await mockCreateMenu(typeof config.data === 'string' ? JSON.parse(config.data) : config.data), status: 200, statusText: 'OK', headers: {}, config }
      if (idMatch && method === 'put') return { data: await mockUpdateMenu(parseInt(idMatch[1]), typeof config.data === 'string' ? JSON.parse(config.data) : config.data), status: 200, statusText: 'OK', headers: {}, config }
      if (idMatch && method === 'delete') return { data: await mockDeleteMenu(parseInt(idMatch[1])), status: 200, statusText: 'OK', headers: {}, config }
    }

    // 角色权限相关接口
    if (url.includes('/auth/role-permission/roles')) {
      const idMatch = url.match(/\/auth\/role-permission\/roles\/(\d+)/)
      if (idMatch && method === 'get' && !url.includes('/menus')) return { data: await mockGetRole(parseInt(idMatch[1])), status: 200, statusText: 'OK', headers: {}, config }
      if (!idMatch && method === 'get') return { data: await mockGetRoleList(), status: 200, statusText: 'OK', headers: {}, config }
      if (!idMatch && method === 'post' && !url.includes('/menus')) return { data: await mockCreateRole(typeof config.data === 'string' ? JSON.parse(config.data) : config.data), status: 200, statusText: 'OK', headers: {}, config }
      if (idMatch && method === 'put' && !url.includes('/menus')) return { data: await mockUpdateRole(parseInt(idMatch[1]), typeof config.data === 'string' ? JSON.parse(config.data) : config.data), status: 200, statusText: 'OK', headers: {}, config }
      if (idMatch && method === 'delete') return { data: await mockDeleteRole(parseInt(idMatch[1])), status: 200, statusText: 'OK', headers: {}, config }
      if (url.includes('/menus') && method === 'get') return { data: await mockGetRoleMenus(parseInt(url.match(/\/auth\/role-permission\/roles\/(\d+)/)![1])), status: 200, statusText: 'OK', headers: {}, config }
      if (url.includes('/menus') && method === 'post') {
        const roleId = parseInt(url.match(/\/auth\/role-permission\/roles\/(\d+)/)![1])
        const menuIds = typeof config.data === 'string' ? JSON.parse(config.data) : config.data
        return { data: await mockAssignRoleMenus(roleId, menuIds.menuIds || []), status: 200, statusText: 'OK', headers: {}, config }
      }
    }

    // 其他角色权限相关接口
    if (url.includes('/auth/role-permission/createRole') && method === 'post') {
      return { data: await mockCreateRole(typeof config.data === 'string' ? JSON.parse(config.data) : config.data), status: 200, statusText: 'OK', headers: {}, config }
    }
    if (url.includes('/auth/role-permission/updateRole') && method === 'post') {
      const data = typeof config.data === 'string' ? JSON.parse(config.data) : config.data
      return { data: await mockUpdateRole(data.roleId, data), status: 200, statusText: 'OK', headers: {}, config }
    }
    if (url.includes('/auth/role-permission/deleteRole') && method === 'post') {
      const roleId = parseInt(new URLSearchParams(url.split('?')[1]).get('roleId')!)
      return { data: await mockDeleteRole(roleId), status: 200, statusText: 'OK', headers: {}, config }
    }
    if (url.includes('/auth/role-permission/assignMenus') && method === 'post') {
      const data = typeof config.data === 'string' ? JSON.parse(config.data) : config.data
      return { data: await mockAssignRoleMenus(data.roleId, data.menuIds || []), status: 200, statusText: 'OK', headers: {}, config }
    }
    if (url.includes('/auth/role-permission/getRoleMenus') && method === 'get') {
      const roleId = parseInt(new URLSearchParams(url.split('?')[1]).get('roleId')!)
      return { data: await mockGetRoleMenus(roleId), status: 200, statusText: 'OK', headers: {}, config }
    }
    if (url.includes('/auth/role-permission/updateMenu') && method === 'post') {
      const menuId = parseInt(new URLSearchParams(url.split('?')[1]).get('menuId')!)
      const data = typeof config.data === 'string' ? JSON.parse(config.data) : config.data
      return { data: await mockUpdateMenu(menuId, data), status: 200, statusText: 'OK', headers: {}, config }
    }
    if (url.includes('/auth/role-permission/deleteMenu') && method === 'post') {
      const menuId = parseInt(new URLSearchParams(url.split('?')[1]).get('menuId')!)
      return { data: await mockDeleteMenu(menuId), status: 200, statusText: 'OK', headers: {}, config }
    }

    return originalAdapter && typeof originalAdapter === 'function' 
      ? (originalAdapter as any)(config) 
      : Promise.reject(new Error('No adapter'))
  }
}

// 是否正在刷新 token
let isRefreshing = false
let requests: Array<(token: string) => void> = []

const getAccessToken = (): string | null => localStorage.getItem('accessToken')
const getRefreshToken = (): string | null => localStorage.getItem('refreshToken')

const saveTokens = (accessToken: string, refreshToken?: string): void => {
  localStorage.setItem('accessToken', accessToken)
  if (refreshToken) localStorage.setItem('refreshToken', refreshToken)
}

const clearTokens = (): void => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
}

const refreshTokenFn = async (): Promise<string> => {
  const refreshTokenValue = getRefreshToken()
  if (!refreshTokenValue) throw new Error('No refresh token')
  try {
    const response = await axios.post<ApiResponse<{ accessToken: string; refreshToken?: string }>>(
      '/api/auth/sso/refresh',
      null,
      { params: { refreshToken: refreshTokenValue } }
    )
    if (response.data.code === "200" && response.data.data) {
      const { accessToken, refreshToken: newRefreshToken } = response.data.data
      saveTokens(accessToken, newRefreshToken || refreshTokenValue)
      return accessToken
    } else {
      throw new Error(response.data.message || 'Token refresh failed')
    }
  } catch (error) {
    clearTokens()
    router.push('/login')
    throw error
  }
}

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken()
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response: AxiosResponse): any => {
    const res = response.data as ApiResponse
    // 兼容字符串和数字类型的 code
    if (res.code !== "200") {
      ElMessage.error(res.message || 'Error')
      if (res.code === "401") {
        clearTokens()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  async (error) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }
    if (error.response?.status === "401" && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve) => {
          requests.push((token: string) => {
            originalRequest.headers!['Authorization'] = `Bearer ${token}`
            resolve(service(originalRequest))
          })
        })
      }
      originalRequest._retry = true
      isRefreshing = true
      try {
        const newToken = await refreshTokenFn()
        isRefreshing = false
        requests.forEach((cb) => cb(newToken))
        requests = []
        originalRequest.headers!['Authorization'] = `Bearer ${newToken}`
        return service(originalRequest)
      } catch (refreshError) {
        isRefreshing = false
        requests = []
        ElMessage.error('登录已过期，请重新登录')
        return Promise.reject(refreshError)
      }
    }
    ElMessage.error(error.message || 'Network Error')
    return Promise.reject(error)
  }
)

// 包装 request 函数，提供类型支持
function request<T = any>(config: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service(config) as Promise<ApiResponse<T>>
}

export default request
export { saveTokens, clearTokens, getAccessToken }
