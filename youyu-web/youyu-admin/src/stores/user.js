import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserMenus } from '@/api/auth'
import { saveTokens, clearTokens } from '@/utils/request'
import router, { resetRouter } from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('accessToken') || '')
  const refreshTokenValue = ref(localStorage.getItem('refreshToken') || '')
  
  // 从 localStorage 恢复用户信息
  const savedUserInfo = localStorage.getItem('userInfo')
  const userInfo = ref(savedUserInfo ? JSON.parse(savedUserInfo) : null)
  
  const menus = ref([])
  const roles = ref([])

  /**
   * 登录
   */
  async function login(loginForm) {
    try {
      const res = await loginApi(loginForm)

      // 兼容字符串和数字类型的 code
      if (res.code == 200 && res.data) {
        const { accessToken, refreshToken, userId, username, userType, roles: userRoles } = res.data

        // 保存 token
        saveTokens(accessToken, refreshToken)
        token.value = accessToken
        refreshTokenValue.value = refreshToken

        // 保存用户信息
        const userData = {
          userId,
          username,
          userType
        }
        userInfo.value = userData
        localStorage.setItem('userInfo', JSON.stringify(userData))
        
        roles.value = userRoles || []

        return { success: true }
      } else {
        return { success: false, message: res.message || '登录失败' }
      }
    } catch (error) {
      console.error('Login error:', error)
      return { success: false, message: error.message || '登录失败' }
    }
  }

  /**
   * 获取用户菜单
   */
  async function fetchMenus() {
    try {
      if (!userInfo.value || !userInfo.value.userId) {
        console.warn('User info not available')
        return []
      }
      
      const res = await getUserMenus(userInfo.value.userId)

      // 兼容字符串和数字类型的 code
      if (res.code == 200 && res.data) {
        menus.value = res.data
        return res.data
      }
      return []
    } catch (error) {
      console.error('Fetch menus error:', error)
      return []
    }
  }

  /**
   * 登出
   */
  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // 清除本地数据
      clearTokens()
      localStorage.removeItem('userInfo')
      token.value = ''
      refreshTokenValue.value = ''
      userInfo.value = null
      menus.value = []
      roles.value = []

      // 重置路由
      resetRouter()

      // 跳转到登录页
      router.push('/login')
    }
  }

  /**
   * 重置用户状态
   */
  function resetUserState() {
    clearTokens()
    localStorage.removeItem('userInfo')
    token.value = ''
    refreshTokenValue.value = ''
    userInfo.value = null
    menus.value = []
    roles.value = []
    
    // 重置路由
    resetRouter()
  }

  return {
    token,
    refreshToken: refreshTokenValue,
    userInfo,
    menus,
    roles,
    login,
    fetchMenus,
    logout,
    resetUserState
  }
})
