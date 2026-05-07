import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 静态路由（不需要权限）
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

// 是否已经添加过动态路由
let hasAddedRoutes = false

/**
 * 根据菜单数据生成路由
 */
function generateRoutes(menus, parentPath = '') {
  const routes = []

  // 组件映射表 - 手动导入所有可能的组件
  const componentMap = {
    'Layout': () => import('@/layout/index.vue'),
    'dashboard/index': () => import('@/views/dashboard/index.vue'),
    'system/user/index': () => import('@/views/system/user/index.vue'),
    'system/role/index': () => import('@/views/system/role/index.vue'),
    'system/menu/index': () => import('@/views/system/menu/index.vue'),
    'product/list/index': () => import('@/views/product/list/index.vue'),
    'product/category/index': () => import('@/views/product/category/index.vue'),
    'order/list/index': () => import('@/views/order/list/index.vue'),
    'enterprise/dept/index': () => import('@/views/enterprise/dept/index.vue'),
    'enterprise/employee/index': () => import('@/views/enterprise/employee/index.vue'),
  }

  menus.forEach(menu => {
    // 跳过按钮类型（type: 3-BUTTON 或 button=true）
    if (menu.type === 3 || menu.button === true) {
      return
    }
    
    // 处理菜单类型和目录类型且可见的 (type: 1-DIRECTORY, 2-MENU)
    if ((menu.type === 2 || menu.type === 1) && menu.visible === 1 && menu.status === 1) {
      // 构建完整路径用于调试
      let fullPath = menu.path
      if (parentPath && !menu.path.startsWith('/')) {
        fullPath = parentPath.endsWith('/') ? parentPath + menu.path : parentPath + '/' + menu.path
      }
      
      const route = {
        path: menu.path,
        name: menu.name.replace(/[\s-]/g, ''), // 移除空格和横线，作为路由名称
        meta: {
          title: menu.name,
          icon: menu.icon,
          permissionCode: menu.permissionCode
        }
      }

      // 处理组件路径
      if (menu.component === 'Layout') {
        route.component = componentMap['Layout']
        if (menu.redirect) {
          route.redirect = menu.redirect
        }
      } else {
        // 从映射表中获取组件
        const componentLoader = componentMap[menu.component]
        if (componentLoader) {
          route.component = componentLoader
        } else {
          console.warn(`Component not found in map: ${menu.component}`)
          // 默认组件
          route.component = () => import('@/views/dashboard/index.vue')
        }
      }

      // 处理子菜单（只添加非按钮类型的子路由）
      if (menu.children && menu.children.length > 0) {
        const childRoutes = generateRoutes(menu.children, menu.path)
        // 只有当有非按钮类型的子路由时才添加
        if (childRoutes.length > 0) {
          route.children = childRoutes
        }
      }

      console.log(`Route generated: ${menu.name} -> ${fullPath}`)
      routes.push(route)
    }
  })

  return routes
}

/**
 * 重置路由
 */
export function resetRouter() {
  hasAddedRoutes = false
  console.log('Router reset, hasAddedRoutes:', hasAddedRoutes)
}

// 路由守卫
router.beforeEach(async (to, from) => {
  const userStore = useUserStore()
  const hasToken = userStore.token

  if (hasToken) {
    if (to.path === '/login') {
      // 已登录，跳转到首页
      return '/'
    } else {
      // 检查是否已经添加过动态路由
      if (!hasAddedRoutes) {
        try {
          // 获取用户菜单
          const menus = await userStore.fetchMenus()

          // 生成动态路由（即使菜单为空也继续）
          const dynamicRoutes = menus && menus.length > 0 ? generateRoutes(menus) : []

          console.log('Generated routes:', JSON.stringify(dynamicRoutes, null, 2))

          // 将动态路由添加到 '/' 父路由下
          dynamicRoutes.forEach(route => {
            router.addRoute('/', route)
            console.log(`Added route to /: ${route.path}`)
          })

          hasAddedRoutes = true
          console.log('Routes added successfully')

          // 如果没有菜单，提示用户
          if (!menus || menus.length === 0) {
            console.warn('No menus found, only default routes available')
          }

          // 返回目标路径，继续导航
          return { ...to, replace: true }
        } catch (error) {
          console.error('Failed to add routes:', error)
          // 出错则清除 token 并跳转到登录页
          userStore.resetUserState()
          return `/login?redirect=${to.fullPath}`
        }
      }
      // 已经添加过路由，继续导航
      return true
    }
  } else {
    // 没有 token
    if (to.path === '/login') {
      return true
    } else {
      // 重定向到登录页
      return `/login?redirect=${to.fullPath}`
    }
  }
})

export default router
