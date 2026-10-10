<template>
    <el-header class="navbar">
        <div class="flex-box">
        <el-button @click="handleCollapse">
            <el-icon><Expand /></el-icon>
        </el-button>
        <p class="page-title">{{ title }}</p>
    </div>
    <div class="flex-box">
        <el-dropdown @command="handleCommand">
            <div class="flex-box">
                <el-avatar src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png

" size="medium"></el-avatar>
                <p class="user-name">admin</p>
                <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
                <el-dropdown-menu>
                    <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
                
            </template>
        </el-dropdown>
       
    </div>
    </el-header>
</template>
<script setup>
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { logout } from '@/api/admin'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()

defineProps({
    title: {
        type: String,
        default: '数据分析'
    }
})

const handleCommand = (command) => {
    if(command === 'logout'){
        handleLogout()
    }
}

const handleLogout = async () => {
    try {
        await ElMessageBox.confirm(
            '确定退出登录吗？',
            '提示',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )
        
        await logout()
        
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        
        useAdminStore().toggleCollapsed()
        
        ElMessage.success('退出登录成功')
        
        router.push('/auth/login')
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('退出登录失败')
        }
    }
}

const handleCollapse = () => {
    useAdminStore().toggleCollapsed()
}
</script>
<style lang="scss" scoped>
.navbar{
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 15px;
    background: white;
    box-shadow: 0 1px 4px rgba(0,21,41,0.08);
    border-bottom: 1px solid #e5e7eb;
    .flex-box{
    display: flex;
    align-items: center;
    justify-content: center;
}
.page-title{
    margin-left: 20px;
    font-size: 26px;
    font-weight: bold;
    color: #1f2937;
}
}

</style>