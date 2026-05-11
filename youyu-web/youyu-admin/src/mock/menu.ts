const delay = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms))

let menuIdSeq = 100

const mockMenuTree = [
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
      },
      {
        id: 7,
        parentId: 5,
        name: '商品分类',
        path: 'category',
        component: 'product/category/index',
        icon: 'Grid',
        permissionCode: 'product:category',
        type: 2, // MENU
        visible: 1,
        status: 1,
        sortOrder: 2
      }
    ]
  },
  {
    id: 8,
    parentId: 0,
    name: '订单管理',
    path: '/order',
    component: 'Layout',
    icon: 'ShoppingCart',
    permissionCode: 'order',
    type: 1, // DIRECTORY
    visible: 1,
    status: 1,
    sortOrder: 3,
    redirect: '/order/list',
    children: [
      {
        id: 9,
        parentId: 8,
        name: '订单列表',
        path: 'list',
        component: 'order/list/index',
        icon: 'Document',
        permissionCode: 'order:list',
        type: 2, // MENU
        visible: 1,
        status: 1,
        sortOrder: 1
      }
    ]
  }
]

function flattenMenus(menus, result = []) {
  for (const menu of menus) {
    const { children, ...menuWithoutChildren } = menu
    result.push(menuWithoutChildren)
    if (children && children.length > 0) {
      flattenMenus(children, result)
    }
  }
  return result
}

function findMenuById(menus, id) {
  for (const menu of menus) {
    if (menu.id === id) return menu
    if (menu.children && menu.children.length > 0) {
      const found = findMenuById(menu.children, id)
      if (found) return found
    }
  }
  return null
}

function buildMenuTree(menus, parentId = 0) {
  return menus
    .filter(m => m.parentId === parentId)
    .map(m => ({
      ...m,
      children: buildMenuTree(menus, m.id)
    }))
    .sort((a, b) => a.sortOrder - b.sortOrder)
}

export async function mockGetMenuTree() {
  await delay()
  return {
    code: 200,
    message: 'success',
    data: JSON.parse(JSON.stringify(mockMenuTree))
  }
}

export async function mockGetAllMenus() {
  await delay()
  const flatMenus = flattenMenus(mockMenuTree)
  return {
    code: 200,
    message: 'success',
    data: flatMenus
  }
}

export async function mockGetMenu(id) {
  await delay()
  const flatMenus = flattenMenus(mockMenuTree)
  const menu = flatMenus.find(m => m.id === id)
  if (menu) {
    return {
      code: 200,
      message: 'success',
      data: menu
    }
  }
  return {
    code: 404,
    message: '菜单不存在'
  }
}

export async function mockCreateMenu(data) {
  await delay()
  menuIdSeq++
  const newMenu = {
    ...data,
    id: menuIdSeq,
    children: []
  }
  
  if (data.parentId === 0 || data.parentId === null || data.parentId === undefined) {
    mockMenuTree.push(newMenu)
  } else {
    const parent = findMenuById(mockMenuTree, data.parentId)
    if (parent) {
      if (!parent.children) parent.children = []
      parent.children.push(newMenu)
    }
  }
  
  return {
    code: 200,
    message: '创建成功',
    data: newMenu
  }
}

export async function mockUpdateMenu(id, data) {
  await delay()
  const flatMenus = flattenMenus(mockMenuTree)
  const menu = flatMenus.find(m => m.id === id)
  
  if (!menu) {
    return {
      code: 404,
      message: '菜单不存在'
    }
  }
  
  Object.assign(menu, data)
  
  return {
    code: 200,
    message: '更新成功',
    data: menu
  }
}

export async function mockDeleteMenu(id) {
  await delay()
  
  function removeMenu(menus, menuId) {
    const index = menus.findIndex(m => m.id === menuId)
    if (index !== -1) {
      menus.splice(index, 1)
      return true
    }
    for (const menu of menus) {
      if (menu.children && menu.children.length > 0) {
        if (removeMenu(menu.children, menuId)) return true
      }
    }
    return false
  }
  
  const removed = removeMenu(mockMenuTree, id)
  
  if (removed) {
    return {
      code: 200,
      message: '删除成功'
    }
  }
  
  return {
    code: 404,
    message: '菜单不存在'
  }
}
