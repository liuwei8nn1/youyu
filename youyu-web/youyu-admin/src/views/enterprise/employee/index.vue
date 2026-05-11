<template>
  <div class='employee-container'>
    <el-card>
      <template #header>
        <div class='card-header'>
          <span>员工管理</span>
          <el-button type='primary' :icon='Plus' @click='handleAdd'>新增员工</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline='true' :model='queryParams' class='search-form'>
        <el-form-item label='关键词'>
          <el-input v-model='queryParams.keyword' placeholder='用户名/手机/邮箱' clearable @keyup.enter='handleQuery' />
        </el-form-item>
        <el-form-item label='部门'>
          <el-tree-select
            v-model="queryParams.deptId"
            :data="deptOptions"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择部门"
            clearable
            check-strictly
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label='状态'>
          <el-select v-model='queryParams.status' placeholder='请选择' clearable style='width: 120px'>
            <el-option label='启用' :value='1' />
            <el-option label='禁用' :value='0' />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type='primary' @click='handleQuery'>查询</el-button>
          <el-button @click='handleResetQuery'>重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="employeeList" border stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="150" show-overflow-tooltip />
        <el-table-column label="部门" width="150">
          <template #default="{ row }">
            {{ getDeptName(row.deptId) }}
          </template>
        </el-table-column>
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hireDate" label="入职时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page='queryParams.page'
        v-model:page-size='queryParams.pageSize'
        :page-sizes='[10, 20, 50, 100]'
        :total='total'
        layout='total, sizes, prev, pager, next, jumper'
        @size-change='loadEmployees'
        @current-change='loadEmployees'
        style='margin-top: 16px; justify-content: flex-end'
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      destroy-on-close
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter='20'>
          <el-col :span='12'>
            <el-form-item label='用户名' prop='username'>
              <el-input v-model='formData.username' placeholder='请输入用户名' :disabled='isEdit' />
            </el-form-item>
          </el-col>
          <el-col :span='12' v-if='!isEdit'>
            <el-form-item label='密码' prop='password'>
              <el-input v-model='formData.password' type='password' placeholder='请输入密码' show-password />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter='20'>
          <el-col :span='12'>
            <el-form-item label='手机号' prop='phone'>
              <el-input v-model='formData.phone' placeholder='请输入手机号' />
            </el-form-item>
          </el-col>
          <el-col :span='12'>
            <el-form-item label='邮箱' prop='email'>
              <el-input v-model='formData.email' placeholder='请输入邮箱' />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter='20'>
          <el-col :span='12'>
            <el-form-item label="部门" prop="deptId">
              <el-tree-select
                v-model="formData.deptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'id', children: 'children' }"
                placeholder="请选择部门"
                check-strictly
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span='12'>
            <el-form-item label='职位' prop='position'>
              <el-input v-model='formData.position' placeholder='请输入职位' />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label='角色' prop='roleIds'>
          <el-select
            v-model='formData.roleIds'
            multiple
            placeholder='请选择角色'
            style='width: 100%'
          >
            <el-option
              v-for='role in roleList'
              :key='role.id'
              :label='role.roleName'
              :value='role.id'
            />
          </el-select>
        </el-form-item>

        <el-form-item label='状态' prop='status'>
          <el-radio-group v-model='formData.status'>
            <el-radio :value='1'>启用</el-radio>
            <el-radio :value='0'>禁用</el-radio>
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

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getEmployeeList, createEmployee, updateEmployee, deleteEmployee } from '@/api/employee'
import { getUserRoles, assignRolesToUser } from '@/api/userRole'  // 新增
import { getDeptTree } from '@/api/dept'
import { getRoleList } from '@/api/role'

const employeeList = ref([])
const deptTree = ref([])
const deptOptions = ref([])
const roleList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentEditId = ref(null)
const total = ref(0)

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  deptId: null,
  status: null
})

const formData = reactive({
  username: '',
  password: '',
  phone: '',
  email: '',
  deptId: null,
  position: '',
  status: 1,
  roleIds: []
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const deptNameMap = ref({})

const getDeptName = (deptId) => {
  return deptNameMap.value[deptId] || '-'
}

const loadEmployees = async () => {
  loading.value = true
  try {
    const res = await getEmployeeList(queryParams)
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      employeeList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('加载员工列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadDeptTree = async () => {
  try {
    const res = await getDeptTree()
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      deptTree.value = res.data
      deptOptions.value = [{ id: 0, deptName: '全部部门', children: res.data }]
      // 构建部门名称映射
      const buildMap = (depts) => {
        depts.forEach(d => {
          deptNameMap.value[d.id] = d.deptName
          if (d.children) buildMap(d.children)
        })
      }
      buildMap(res.data)
    }
  } catch (error) {
    console.error('加载部门树失败:', error)
  }
}

const loadRoles = async () => {
  try {
    const res = await getRoleList()
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      roleList.value = res.data
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

const handleQuery = () => {
  queryParams.page = 1
  loadEmployees()
}

const handleResetQuery = () => {
  queryParams.keyword = ''
  queryParams.deptId = null
  queryParams.status = null
  handleQuery()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增员工'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑员工'
  currentEditId.value = row.id

  // 获取员工角色（从 Auth 服务）
  try {
    const roleRes = await getUserRoles(row.identityId, 2)  // userType: 2 = enterprise
    // 兼容字符串和数字类型的 code
    if (roleRes.code == 200) {
      Object.assign(formData, {
        username: row.username,
        password: '',
        phone: row.phone || '',
        email: row.email || '',
        deptId: row.deptId,
        position: row.position || '',
        status: row.status,
        roleIds: roleRes.data || []
      })
      dialogVisible.value = true
    } else {
      ElMessage.error('加载员工角色失败')
    }
  } catch (error) {
    console.error('加载员工详情失败:', error)
    ElMessage.error('加载员工信息失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除员工"${row.username}"吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await deleteEmployee(row.id)
    // 兼容字符串和数字类型的 code
    if (res.code == 200) {
      ElMessage.success('删除成功')
      loadEmployees()
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
          // 1. 更新员工基本信息
          res = await updateEmployee({ ...formData, id: currentEditId.value })
          
          // 2. 分配角色（如果选择了角色）
          // 兼容字符串和数字类型的 code
          if (res.code == 200 && formData.roleIds && formData.roleIds.length > 0) {
            await assignRolesToUser({
              userId: row.identityId,  // 使用 identityId（user_identity.id）
              userType: 2,  // enterprise
              roleIds: formData.roleIds
            })
          }
        } else {
          // 1. 创建员工
          res = await createEmployee(formData)
          
          // 2. 分配角色（如果选择了角色）
          // 兼容字符串和数字类型的 code
          if (res.code == 200 && formData.roleIds && formData.roleIds.length > 0) {
            await assignRolesToUser({
              userId: res.data.identityId,  // 新创建的用户身份ID（user_identity.id）
              userType: 2,  // enterprise
              roleIds: formData.roleIds
            })
          }
        }

        // 兼容字符串和数字类型的 code
        if (res.code == 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
          dialogVisible.value = false
          loadEmployees()
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
    username: '',
    password: '',
    phone: '',
    email: '',
    deptId: null,
    position: '',
    status: 1,
    roleIds: []
  })
}

onMounted(() => {
  loadEmployees()
  loadDeptTree()
  loadRoles()
})
</script>

<style scoped lang='scss'>
.employee-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 16px;
  }
}
</style>
