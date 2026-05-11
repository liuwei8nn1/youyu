/**
 * 通用 API 响应类型
 */
export interface ApiResponse<T = any> {
  code: string
  message: string
  data: T
}

/**
 * 用户信息类型
 */
export interface UserInfo {
  userId: number
  username: string
  userType: number
}

/**
 * 登录请求参数
 */
export interface LoginRequest {
  credential: string
  password: string
  loginType: string
  userType: number
}

/**
 * 登录响应数据
 */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  userId: number
  username: string
  userType: number
  roles?: string[]
}

/**
 * 菜单项类型
 */
export interface MenuItem {
  id: number
  parentId: number
  name: string
  path?: string
  component?: string
  icon?: string
  sort: number
  type: number // 1:目录 2:菜单 3:按钮
  permission?: string
  visible: number // 0:隐藏 1:显示
  children?: MenuItem[]
}

/**
 * 角色类型
 */
export interface Role {
  roleId: number
  roleName: string
  roleCode: string
  description?: string
  status: number
  createTime?: string
  updateTime?: string
}

/**
 * 分页请求参数
 */
export interface PageRequest {
  pageNum: number
  pageSize: number
}

/**
 * 分页响应数据
 */
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 部门类型
 */
export interface Department {
  deptId: number
  parentId: number
  deptName: string
  sort: number
  status: number
  createTime?: string
  children?: Department[]
}

/**
 * 员工类型
 */
export interface Employee {
  employeeId: number
  username: string
  realName: string
  phone?: string
  email?: string
  deptId: number
  status: number
  createTime?: string
}
