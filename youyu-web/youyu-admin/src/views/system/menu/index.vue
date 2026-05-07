<template>
  <div class="menu-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增菜单</el-button>
        </div>
      </template>

      <el-table
        :data="menuTree"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
        border
      >
        <el-table-column prop="name" label="菜单名称" width="180" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{ row }">
            <el-icon v-if="row.icon && typeof row.icon === 'string'">
              <component :is="row.icon" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" width="180" />
        <el-table-column prop="permissionCode" label="权限码" width="150" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.type === 2" type="success">菜单</el-tag>
            <el-tag v-else-if="row.type === 1" type="warning">目录</el-tag>
            <el-tag v-else type="info">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="visible" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'danger'">
              {{ row.visible === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Plus" @click="handleAdd(row)">新增</el-button>
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="menuOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            placeholder="请选择上级菜单"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="formData.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="formData.component" placeholder="请输入组件路径，如：system/user/index" />
        </el-form-item>
        <el-form-item label="权限码" prop="permissionCode">
          <el-input v-model="formData.permissionCode" placeholder="请输入权限码，如：system:user" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="formData.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="是否显示" prop="visible">
          <el-radio-group v-model="formData.visible">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="重定向" prop="redirect">
          <el-input v-model="formData.redirect" placeholder="请输入重定向路径" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getMenuTree, getAllMenus, createMenu, updateMenu, deleteMenu } from '@/api/menu'

const menuTree = ref([])
const menuOptions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentEditId = ref(null)

const formData = reactive({
  parentId: 0,
  type: 2, // MENU
  name: '',
  path: '',
  component: '',
  permissionCode: '',
  icon: '',
  sortOrder: 0,
  visible: 1,
  redirect: ''
})

const formRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  path: [{ required: true, message: '请输入路由路径', trigger: 'blur' }]
}

const loadMenus = async () => {
  try {
    console.log('=== Loading Menus ===')
    const res = await getMenuTree()
    console.log('Menu API Response:', res)
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      console.log('Loaded menu tree:', JSON.stringify(res.data, null, 2))
      menuTree.value = res.data
      console.log('Menu Tree:', menuTree.value)
    } else {
      console.error('Failed to load menus:', res.message)
    }
  } catch (error) {
    console.error('加载菜单失败:', error)
  }
}

const loadAllMenus = async () => {
  try {
    const res = await getAllMenus()
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      menuOptions.value = [
        { id: 0, name: '顶级菜单' },
        ...res.data.filter(m => m.type === 2 || m.type === 1) // MENU or DIRECTORY
      ]
    }
  } catch (error) {
    console.error('加载菜单选项失败:', error)
  }
}

const handleAdd = (parent) => {
  isEdit.value = false
  dialogTitle.value = '新增菜单'
  resetForm()
  if (parent) {
    formData.parentId = parent.id
  } else {
    formData.parentId = 0
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑菜单'
  currentEditId.value = row.id
  Object.assign(formData, {
    parentId: row.parentId,
    type: row.type,
    name: row.name,
    path: row.path,
    component: row.component || '',
    permissionCode: row.permissionCode || '',
    icon: row.icon || '',
    sortOrder: row.sortOrder || 0,
    visible: row.visible,
    redirect: row.redirect || ''
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除菜单"${row.name}"吗？${row.children?.length ? '注意：该菜单包含子菜单，也会一并删除。' : ''}`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await deleteMenu(row.id)
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      ElMessage.success('删除成功')
      loadMenus()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (isEdit.value) {
          res = await updateMenu(currentEditId.value, formData)
        } else {
          res = await createMenu(formData)
        }

        // 兼容字符串和数字类型的 code
        if (res.code == 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
          dialogVisible.value = false
          loadMenus()
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
    parentId: 0,
    type: 2, // MENU
    name: '',
    path: '',
    component: '',
    permissionCode: '',
    icon: '',
    sortOrder: 0,
    visible: 1,
    redirect: ''
  })
}

onMounted(() => {
  loadMenus()
  loadAllMenus()
})
</script>

<style scoped lang="scss">
.menu-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
