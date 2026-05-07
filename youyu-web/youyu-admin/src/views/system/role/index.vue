<template>
  <div class="role-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
        </div>
      </template>

      <el-table :data="roleList" border stripe v-loading="loading">
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link :icon="Key" @click="handleAssignMenus(row)">分配菜单</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="menuDialogVisible"
      title="分配菜单"
      width="500px"
      destroy-on-close
      @close="handleMenuDialogClose"
    >
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        :check-strictly="false"
        show-checkbox
        default-expand-all
        empty-text="加载中..."
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="menuSubmitLoading" @click="handleMenuSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Key } from '@element-plus/icons-vue'
import { getRoleList, getRole, createRole, updateRole, deleteRole, getRoleMenus, assignRoleMenus } from '@/api/role'
import { getMenuTree } from '@/api/menu'

const roleList = ref([])
const menuTree = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const menuSubmitLoading = ref(false)
const formRef = ref(null)
const menuTreeRef = ref(null)
const isEdit = ref(false)
const currentEditId = ref(null)
const currentRoleId = ref(null)

const formData = reactive({
  roleCode: '',
  roleName: '',
  description: '',
  sortOrder: 0,
  status: 1
})

const formRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const loadRoles = async () => {
  loading.value = true
  try {
    console.log('=== Loading Roles ===')
    const res = await getRoleList()
    console.log('Role API Response:', res)
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      roleList.value = res.data
      console.log('Role List:', roleList.value)
    } else {
      console.error('Failed to load roles:', res.message)
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMenuTree = async () => {
  try {
    const res = await getMenuTree()
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      menuTree.value = res.data
    }
  } catch (error) {
    console.error('加载菜单树失败:', error)
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增角色'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  currentEditId.value = row.id
  
  try {
    const res = await getRole(row.id)
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      Object.assign(formData, res.data)
    }
  } catch (error) {
    console.error('加载角色详情失败:', error)
  }
  
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色"${row.roleName}"吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await deleteRole(row.id)
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      ElMessage.success('删除成功')
      loadRoles()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleAssignMenus = async (row) => {
  currentRoleId.value = row.id
  menuDialogVisible.value = true
  
  try {
    const [menuRes, roleMenuRes] = await Promise.all([
      getMenuTree(),
      getRoleMenus(row.id)
    ])
    
    // 兼容字符串和数字类型的 code
    if (menuRes.code == 200) {
      menuTree.value = menuRes.data
    }
    
    if (roleMenuRes.code == 200) {
      await nextTick()
      menuTreeRef.value?.setCheckedKeys(roleMenuRes.data || [])
    }
  } catch (error) {
    console.error('加载菜单数据失败:', error)
  }
}

const handleMenuSubmit = async () => {
  if (!currentRoleId.value) return
  
  menuSubmitLoading.value = true
  try {
    const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
    const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
    const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys]
    
    const res = await assignRoleMenus(currentRoleId.value, allCheckedKeys)
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      ElMessage.success('分配成功')
      menuDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '分配失败')
    }
  } catch (error) {
    console.error('分配菜单失败:', error)
    ElMessage.error('分配失败')
  } finally {
    menuSubmitLoading.value = false
  }
}

const handleMenuDialogClose = () => {
  menuTreeRef.value?.setCheckedKeys([])
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (isEdit.value) {
          res = await updateRole(currentEditId.value, formData)
        } else {
          res = await createRole(formData)
        }

        // 兼容字符串和数字类型的 code
        if (res.code == 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
          dialogVisible.value = false
          loadRoles()
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  resetForm()
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(formData, {
    roleCode: '',
    roleName: '',
    description: '',
    sortOrder: 0,
    status: 1
  })
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped lang="scss">
.role-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
