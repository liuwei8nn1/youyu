// Mock 拦截器已移至 src/utils/request.js
// 此文件保留但不做任何拦截，避免冲突

import { mockLogin, mockRefreshToken, mockGetUserMenus } from './auth'
import { mockGetMenuTree, mockGetAllMenus, mockGetMenu, mockCreateMenu, mockUpdateMenu, mockDeleteMenu } from './menu'
import { mockGetRoleList, mockGetRole, mockCreateRole, mockUpdateRole, mockDeleteRole, mockGetRoleMenus, mockAssignRoleMenus } from './role'

export {
  // Auth mocks
  mockLogin,
  mockRefreshToken,
  mockGetUserMenus,
  
  // Menu mocks
  mockGetMenuTree,
  mockGetAllMenus,
  mockGetMenu,
  mockCreateMenu,
  mockUpdateMenu,
  mockDeleteMenu,
  
  // Role mocks
  mockGetRoleList,
  mockGetRole,
  mockCreateRole,
  mockUpdateRole,
  mockDeleteRole,
  mockGetRoleMenus,
  mockAssignRoleMenus
}
