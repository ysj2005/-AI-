<template>
  <el-aside :width="isCollapse ? '64px' : '264px'">
    <el-menu
    :collapse="isCollapse"
    :collapse-transition="false"
    default-active="2"  
    class="menu-style"
    @select="handleSelect"
    >
      <div class="brand">
        <el-image style="width: 50px; height: 50px; margin-right: 10px;"  :src="iconURL" alt="logo" fit="contain" />
        <div class="info-card" v-show="!isCollapse">
          <h1 class="brand-title">心理健康AI助手</h1>
          <p class="brand-subtitle">管理后台</p>
        </div>
      </div>
      <el-menu-item v-for="item in router.options.routes[0].children" :key="item.path" :index="item.path">
        <el-icon><component :is="getIcon(item.meta.icon)" /></el-icon>
        <span>{{item.meta.title}}</span>
      </el-menu-item>
      
    </el-menu>
  </el-aside>
</template>
<script setup>
import { computed } from 'vue'
import { useAdminStore } from '@/stores/admin'
import { useRouter } from 'vue-router'
import { PieChart, ChatLineSquare, Message, User } from '@element-plus/icons-vue'

const router = useRouter()
const emit = defineEmits(['menu-change'])
const iconURL = new URL('../assets/images/机器人.png', import.meta.url).href
const isCollapse = computed(() => useAdminStore().isCollapsed)

const iconMap = {
  PieChart,
  ChatLineSquare,
  Message,
  User
}

const getIcon = (iconName) => {
  return iconMap[iconName] || PieChart
}

const handleSelect = (index) => {
  const currentRoute = router.options.routes[0]
  const item = currentRoute.children.find(r => r.path === index)
  if (item) {
    emit('menu-change', item.meta.title)
  }
  router.push(`${currentRoute.path}/${index}`)
}
</script>
<style lang="scss" scoped>
.menu-style{
  height: 100%;
  .brand{
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  background-color: #fff;
  border-bottom: 1px solid #e5e7eb;
  .info-card{
    .brand-title{
      font-size: 20px;
      font-weight: bold;
      margin-bottom: 5px;
      color: #1f2937;
    }
    .brand-subtitle{
      font-size: 14px;
      color: #6b7280;
      
    }
  }
}
}

</style>
