<template>
  <div class="dashboard-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>欢迎使用 YouYu Admin</span>
        </div>
      </template>

      <div class="dashboard-content">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="用户数" :value="12345">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="商品数" :value="6789">
              <template #prefix>
                <el-icon><Goods /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="订单数" :value="23456">
              <template #prefix>
                <el-icon><ShoppingCart /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="销售额" :value="999999" :precision="2">
              <template #prefix>¥</template>
            </el-statistic>
          </el-col>
        </el-row>

        <el-divider />

        <div class="welcome-text">
          <h3>系统信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">
              {{ userStore.userInfo?.username || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="用户类型">
              {{ getUserTypeText(userStore.userInfo?.userType) }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              {{ userStore.roles.join(', ') || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="登录时间">
              {{ new Date().toLocaleString() }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const getUserTypeText = (type) => {
  const typeMap = {
    1: '管理员',
    2: '普通用户',
    3: '商户'
  }
  return typeMap[type] || '未知'
}
</script>

<style scoped lang="scss">
.dashboard-container {
  .card-header {
    font-size: 16px;
    font-weight: bold;
  }

  .dashboard-content {
    padding: 20px 0;

    .el-statistic {
      text-align: center;
    }

    .welcome-text {
      margin-top: 20px;

      h3 {
        margin-bottom: 16px;
        color: #303133;
      }
    }
  }
}
</style>
