<template>
  <div class='dept-container'>
    <el-card>
      <template #header>
        <div class='card-header'>
          <span>部门管理</span>
          <el-button type='primary' :icon='Plus' @click='handleAdd'>新增部门</el-button>
        </div>
      </template>

      <el-table
        :data="deptTree"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
        border
        v-loading="loading"
      >
        <el-table-column prop="deptName" label="部门名称" width="200" />
        <el-table-column prop="deptCode" label="部门编码" width="120" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
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
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="deptOptions"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            check-strictly
            placeholder="请选择上级部门"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="formData.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="formData.deptCode" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="formData.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态" prop="status">
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getDeptTree, createDept, updateDept, deleteDept } from '@/api/dept'

const deptTree = ref([])
const deptOptions = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentEditId = ref(null)

const formData = reactive({
  parentId: 0,
  deptName: '',
  deptCode: '',
  leader: '',
  phone: '',
  email: '',
  sortOrder: 0,
  status: 1
})

const formRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }]
}

const loadDepts = async () => {
  loading.value = true
  try {
    const res = await getDeptTree()
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      deptTree.value = res.data
      // 构建树形选项
      deptOptions.value = [{ id: 0, deptName: '顶级部门', children: res.data }]
    }
  } catch (error) {
    console.error('加载部门失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = (parent) => {
  isEdit.value = false
  dialogTitle.value = '新增部门'
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
  dialogTitle.value = '编辑部门'
  currentEditId.value = row.id
  Object.assign(formData, {
    parentId: row.parentId,
    deptName: row.deptName,
    deptCode: row.deptCode,
    leader: row.leader || '',
    phone: row.phone || '',
    email: row.email || '',
    sortOrder: row.sortOrder || 0,
    status: row.status
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门"${row.deptName}"吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await deleteDept(row.id)
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      ElMessage.success('删除成功')
      loadDepts()
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
          res = await updateDept({ ...formData, deptId: currentEditId.value })
        } else {
          res = await createDept(formData)
        }

        // 兼容字符串和数字类型的 code
        if (res.code == 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
          dialogVisible.value = false
          loadDepts()
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
    deptName: '',
    deptCode: '',
    leader: '',
    phone: '',
    email: '',
    sortOrder: 0,
    status: 1
  })
}

onMounted(() => {
  loadDepts()
})
</script>

<style scoped lang='scss'>
.dept-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
