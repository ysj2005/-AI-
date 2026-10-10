<template>
    <div class="container">
        <div class="title-section">
            <h2>创建您的账户</h2>
            <p>创建您的注册信息</p>
        </div>
        <div class="form-container">
            <el-form 
            ref="ruleFormRef"
            :model="formData"
            :rules="rules"
            label-position="top"
            >
                <el-form-item label="用户名或邮箱" prop="username">
                    <el-input v-model="formData.username" size="large" placeholder="请输入用户名或邮箱"></el-input>
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="formData.email" size="large" placeholder="请输入邮箱"></el-input>
                </el-form-item>
                <el-form-item label="昵称">
                    <el-input v-model="formData.nickname" size="large" placeholder="请输入昵称（可选）"></el-input>
                </el-form-item>
                <el-form-item label="手机号">
                    <el-input v-model="formData.phone" size="large" placeholder="请输入手机号（可选）"></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="formData.password" size="large" placeholder="请输入密码" type="password" show-password></el-input>
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input v-model="formData.confirmPassword" size="large" placeholder="请再次输入密码" type="password" show-password></el-input>
                </el-form-item>
                <el-button class="btn" size="large" type="primary" @click="submitForm(ruleFormRef)">创建用户</el-button>
            </el-form>
            <div class="footer">
                <p>已有账户？<router-link to="/auth/login">立即登录</router-link></p>
            </div>
        </div>
    </div>
</template>
<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/frontend'
import { ElMessage } from 'element-plus'

const router = useRouter()
const ruleFormRef = ref()
const formData = reactive({
    username: '',
    email: '',
    nickname: '',
    phone: '',
    password: '',
    confirmPassword: '',
    gender:0,
    userType:1
})

const validateConfirmPassword = (rule, value, callback) => {
    if (value !== formData.password) {
        callback(new Error('两次输入的密码不一致'))
    } else {
        callback()
    }
}

const rules = reactive({
    username: [
        { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
    ]
})

const submitForm = async (formEl) => {
    if (!formEl) return
    await formEl.validate((valid) => {
        if (valid) {
            register(formData).then(() => {
                ElMessage.success('注册成功，请登录')
                router.push('/auth/login')
            }).catch(err => {
                console.error('register error:', err)
            })
        }
    })
}
</script>
<style scoped lang="scss">
.container{
    width: 384px;
    
    .title-section{
        text-align: center;
        margin-bottom: 40px;
        
        h2{
            font-size: 32px;
            font-weight: bold;
            color: #1f2937;
            margin-bottom: 8px;
        }
        
        p{
            font-size: 16px;
            color: #6b7280;
        }
    }
    
    .form-container{
        .btn{
            margin-top: 30px;
            width: 100%;
        }
        
        .footer{
            padding: 20px 0;
            text-align: center;
            
            p{
                font-size: 14px;
                color: #6b7280;
                
                a{
                    color: #4A90E2;
                    text-decoration: none;
                    
                    &:hover{
                        text-decoration: underline;
                    }
                }
            }
        }
    }
}
</style>