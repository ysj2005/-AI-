<template>
  <div class="backend-layout">
    <el-container class="main-container">
      <Sidebar @menu-change="handleMenuChange" />
      <el-container direction="vertical">
        <Navbar :title="currentTitle" />
        <el-main class="main-content">
          <router-view class="content-container"></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'

const route = useRoute()
const currentTitle = ref('数据分析')

const handleMenuChange = (title) => {
  currentTitle.value = title
}

onMounted(() => {
  const matched = route.matched.find(r => r.meta && r.meta.title)
  if (matched) {
    currentTitle.value = matched.meta.title
  }
})
</script>
<style lang="scss" scoped>
.backend-layout {
  height: 100vh;
  .el-header{
    height: 74px !important;
  }
  .main-container{
    height: 100%;
  }
  .main-content {
    padding: 0;
    overflow-y: auto;
  }
  .content-container{
    padding: 20px;
    background-color: #f0f2f5;
  }
}
</style>