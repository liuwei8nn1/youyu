/**
 * Mock 数据 - 模拟后端接口响应
 */

// 模拟延迟
const delay = (ms = 500) => new Promise(resolve => setTimeout(resolve, ms))

/**
 * Mock 登录接口
 */
export async function mockLogin(data) {
  await delay()

  const { username, password } = data

  // 简单的验证逻辑
  if (!username || !password) {
    return {
      code: 500,
      message: '用户名和密码不能为空'
    }
  }

  // 模拟成功登录
  return {
    code: 200,
    message: 'success',
    data: {
      accessToken: 'mock_access_token_' + Date.now(),
      refreshToken: 'mock_refresh_token_' + Date.now(),
      userId: 1,
      username: username,
      userType: 1,
      roles: ['admin']
    }
  }
}

/**
 * Mock 刷新 token 接口
 */
export async function mockRefreshToken(refreshToken) {
  await delay()

  if (!refreshToken) {
    return {
      code: 500,
      message: 'Refresh token 不能为空'
    }
  }

  return {
    code: 200,
    message: 'success',
    data: {
      accessToken: 'mock_access_token_' + Date.now(),
      refreshToken: refreshToken // 滚动刷新时返回新的 refresh token
    }
  }
}

/**
 * Mock 获取用户菜单接口
 */
export async function mockGetUserMenus() {
  await delay()

  return {
    code: 200,
    message: 'success',
    data: [
      {
        id: 1,
        parentId: 0,
        name: '系统管理',
        path: '/system',
        component: 'Layout',
        icon: 'Setting',
        permissionCode: 'system',
        type: 1, // DIRECTORY
        visible: 1,
        status: 1,
        sortOrder: 1,
        redirect: '/system/user',
        children: [
          {
            id: 2,
            parentId: 1,
            name: '用户管理',
            path: 'user',
            component: 'system/user/index',
            icon: 'User',
            permissionCode: 'system:user',
            type: 2, // MENU
            visible: 1,
            status: 1,
            sortOrder: 1
          },
          {
            id: 3,
            parentId: 1,
            name: '角色管理',
            path: 'role',
            component: 'system/role/index',
            icon: 'UserFilled',
            permissionCode: 'system:role',
            type: 2, // MENU
            visible: 1,
            status: 1,
            sortOrder: 2
          },
          {
            id: 4,
            parentId: 1,
            name: '菜单管理',
            path: 'menu',
            component: 'system/menu/index',
            icon: 'Menu',
            permissionCode: 'system:menu',
            type: 2, // MENU
            visible: 1,
            status: 1,
            sortOrder: 3
          }
        ]
      },
      {
        id: 5,
        parentId: 0,
        name: '商品管理',
        path: '/product',
        component: 'Layout',
        icon: 'Goods',
        permissionCode: 'product',
        type: 1, // DIRECTORY
        visible: 1,
        status: 1,
        sortOrder: 2,
        redirect: '/product/list',
        children: [
          {
            id: 6,
            parentId: 5,
            name: '商品列表',
            path: 'list',
            component: 'product/list/index',
            icon: 'List',
            permissionCode: 'product:list',
            type: 2, // MENU
            visible: 1,
            status: 1,
            sortOrder: 1
          }
        ]
      }
    ]
  }
}
