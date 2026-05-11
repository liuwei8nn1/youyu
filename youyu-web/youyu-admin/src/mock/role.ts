const delay = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms))

let roleIdSeq = 10

const mockRoles = [
  {
    id: 1,
    roleCode: 'SUPER_ADMIN',
    roleName: '超级管理员',
    description: '系统超级管理员，拥有所有权限',
    sortOrder: 1,
    status: 1,
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    roleCode: 'ADMIN',
    roleName: '管理员',
    description: '系统管理员，拥有大部分权限',
    sortOrder: 2,
    status: 1,
    createTime: '2024-01-02 10:00:00',
    updateTime: '2024-01-02 10:00:00'
  },
  {
    id: 3,
    roleCode: 'USER',
    roleName: '普通用户',
    description: '普通用户，拥有基础查看权限',
    sortOrder: 3,
    status: 1,
    createTime: '2024-01-03 10:00:00',
    updateTime: '2024-01-03 10:00:00'
  }
]

const mockRoleMenus = {
  1: [1, 2, 3, 4, 5, 6, 7, 8, 9],
  2: [1, 2, 3, 5, 6],
  3: [5, 6]
}

export async function mockGetRoleList() {
  await delay()
  return {
    code: 200,
    message: 'success',
    data: [...mockRoles]
  }
}

export async function mockGetRole(id) {
  await delay()
  const role = mockRoles.find(r => r.id === id)
  if (role) {
    return {
      code: 200,
      message: 'success',
      data: { ...role }
    }
  }
  return {
    code: 404,
    message: '角色不存在'
  }
}

export async function mockCreateRole(data) {
  await delay()
  
  const existingRole = mockRoles.find(r => r.roleCode === data.roleCode)
  if (existingRole) {
    return {
      code: 500,
      message: '角色编码已存在'
    }
  }
  
  roleIdSeq++
  const now = new Date().toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
  const newRole = {
    id: roleIdSeq,
    roleCode: data.roleCode,
    roleName: data.roleName,
    description: data.description || '',
    sortOrder: data.sortOrder || mockRoles.length + 1,
    status: data.status !== undefined ? data.status : 1,
    createTime: now,
    updateTime: now
  }
  
  mockRoles.push(newRole)
  mockRoleMenus[roleIdSeq] = []
  
  return {
    code: 200,
    message: '创建成功',
    data: newRole
  }
}

export async function mockUpdateRole(id, data) {
  await delay()
  
  const role = mockRoles.find(r => r.id === id)
  if (!role) {
    return {
      code: 404,
      message: '角色不存在'
    }
  }
  
  if (data.roleCode && data.roleCode !== role.roleCode) {
    const existingRole = mockRoles.find(r => r.roleCode === data.roleCode && r.id !== id)
    if (existingRole) {
      return {
        code: 500,
        message: '角色编码已存在'
      }
    }
  }
  
  Object.assign(role, {
    ...data,
    updateTime: new Date().toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
  })
  
  return {
    code: 200,
    message: '更新成功',
    data: role
  }
}

export async function mockDeleteRole(id) {
  await delay()
  
  const index = mockRoles.findIndex(r => r.id === id)
  if (index === -1) {
    return {
      code: 404,
      message: '角色不存在'
    }
  }
  
  if (id === 1) {
    return {
      code: 500,
      message: '不能删除超级管理员角色'
    }
  }
  
  mockRoles.splice(index, 1)
  delete mockRoleMenus[id]
  
  return {
    code: 200,
    message: '删除成功'
  }
}

export async function mockGetRoleMenus(roleId) {
  await delay()
  
  const role = mockRoles.find(r => r.id === roleId)
  if (!role) {
    return {
      code: 404,
      message: '角色不存在'
    }
  }
  
  return {
    code: 200,
    message: 'success',
    data: mockRoleMenus[roleId] || []
  }
}

export async function mockAssignRoleMenus(roleId, menuIds) {
  await delay()
  
  const role = mockRoles.find(r => r.id === roleId)
  if (!role) {
    return {
      code: 404,
      message: '角色不存在'
    }
  }
  
  mockRoleMenus[roleId] = menuIds || []
  
  return {
    code: 200,
    message: '分配成功'
  }
}
