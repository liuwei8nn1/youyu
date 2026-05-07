<template>
  <!-- 如果是按钮类型，不渲染任何内容 -->
  <template v-if="!isButton">
    <!-- 有非按钮类型的子菜单 -->
    <el-sub-menu v-if="hasNonButtonChildren" :index="item.path">
      <template #title>
        <el-icon v-if="item.icon && typeof item.icon === 'string'">
          <component :is="item.icon" />
        </el-icon>
        <span>{{ item.name }}</span>
      </template>

      <sidebar-item
        v-for="child in item.children"
        :key="child.id"
        :item="child"
        :base-path="fullPath"
      />
    </el-sub-menu>

    <!-- 没有非按钮类型的子菜单（包括只有按钮子菜单或无子菜单） -->
    <el-menu-item v-else :index="fullPath" @click="handleMenuClick">
      <el-icon v-if="item.icon && typeof item.icon === 'string'">
        <component :is="item.icon" />
      </el-icon>
      <span>{{ item.name }}</span>
    </el-menu-item>
  </template>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  basePath: {
    type: String,
    default: ''
  }
})

const router = useRouter()

// 判断是否是按钮类型（type=3 或 button=true）
const isButton = computed(() => {
  return props.item.type === 3 || props.item.button === true
})

// 判断是否有非按钮类型的子菜单
const hasNonButtonChildren = computed(() => {
  if (!props.item.children || props.item.children.length === 0) {
    return false
  }
  // 检查是否有任何非按钮类型的子项
  return props.item.children.some(child => child.type !== 3 && child.button !== true)
})

// 判断是否有子菜单（用于显示展开箭头）
const hasChildren = computed(() => {
  return props.item.children && props.item.children.length > 0
})

// 解析基础路径
const resolveBasePath = (path) => {
  if (path.startsWith('/')) {
    return path
  }
  
  const parentPath = props.basePath || ''
  if (!parentPath) return path
  
  if (parentPath.endsWith('/')) {
    return parentPath + path
  }
  return parentPath + '/' + path
}

// 解析完整路径（用于菜单项的 index）
const fullPath = computed(() => {
  if (props.item.path.startsWith('/')) {
    return props.item.path
  }

  const parentPath = props.basePath || ''
  if (!parentPath) return props.item.path
  
  if (parentPath.endsWith('/')) {
    return parentPath + props.item.path
  }
  return parentPath + '/' + props.item.path
})

// 处理菜单点击
const handleMenuClick = () => {
  console.log('=== Menu Clicked ===')
  console.log('Item:', props.item)
  console.log('Item Path:', props.item.path)
  console.log('Base Path:', props.basePath)
  console.log('Full Path:', fullPath.value)
  console.log('Is Button:', isButton.value)
  console.log('Has Non-Button Children:', hasNonButtonChildren.value)
  
  // 如果是目录类型（type=1），不执行跳转，只展开/收起
  if (props.item.type === 1) {
    console.log('This is a directory, skip navigation')
    return
  }
  
  console.log('Navigating to:', fullPath.value)
  router.push(fullPath.value)
}
</script>
