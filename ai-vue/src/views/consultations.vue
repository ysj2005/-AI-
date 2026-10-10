<template>
    <div class="page-content">
        <div class="consultation-container">
            <PageHead title="咨询记录"></PageHead>
            <table class="consultation-table">
                <thead>
                    <tr>
                        <th class="col-avatar">会话ID</th>
                        <th class="col-content">情绪日志</th>
                        <th class="col-count">消息数</th>
                        <th class="col-time">时间</th>
                        <th class="col-action">操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in tableData" :key="item.id">
                        <td class="col-avatar">
                            <div class="avatar" v-if="item.userNickname">
                                <span>{{ item.userNickname }}</span>
                            </div>
                            <div class="avatar empty-avatar" v-else></div>
                        </td>
                        <td class="col-content">
                            <div class="content-title">{{ item.sessionTitle }}</div>
                            <div class="content-body">{{ item.lastMessageContent || '-' }}</div>
                        </td>
                        <td class="col-count">{{ item.messageCount || 0 }}</td>
                        <td class="col-time">
                            <div class="time-row">{{ formatDate(item.lastMessageTime) }}</div>
                            <div class="time-row">{{ formatTime(item.lastMessageTime) }}</div>
                        </td>
                        <td class="col-action">
                            <el-button @click="handleDetail(item)" text type="primary">详情</el-button>
                        </td>
                    </tr>
                </tbody>
            </table>
            
            <el-pagination
                style="margin-top: 25px"
                v-model:current-page="currentPage"
                :page-size="pageSize"
                layout="prev, pager, next"
                :total="total"
                @current-change="handleSearch"
            />
        </div>
        
        <el-dialog
            title="咨询会话详情"
            v-model="dialogVisible"
            width="70%"
            :close-on-click-modal="false"
            @close="handleDialogClose"
        >
            <div class="detail-info">
                <div class="info-row">
                    <span class="info-label">用户：</span>
                    <span class="info-value">{{ currentItem?.userNickname || '-' }}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">开始时间：</span>
                    <span class="info-value">{{ formatDateTime(currentItem?.startedAt) }}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">消息数：</span>
                    <span class="info-value">{{ currentItem?.messageCount || 0 }}</span>
                </div>
            </div>
            
            <div class="dialog-title">对话记录</div>
            
            <div class="chat-container" ref="chatContainer">
                <div
                    v-for="(message, index) in messages"
                    :key="index"
                    :class="['chat-item', isUserMessage(message) ? 'user' : 'ai']"
                >
                    <div class="chat-label">{{ getSenderLabel(message) }}</div>
                    <div class="chat-text">{{ message.content }}</div>
                    <div class="chat-time">{{ formatDateTime(message.createdAt || message.timestamp || message.time) }}</div>
                </div>
            </div>
            
            <template #footer>
                <el-button @click="dialogVisible = false">关闭</el-button>
            </template>
        </el-dialog>
    </div>
</template>
<script setup>
import { onMounted, onUnmounted, ref, reactive, nextTick } from 'vue'
import PageHead from '@/components/PageHead.vue'
import { getConsultationPage, getSessionDetail } from '@/api/admin'
import { ElMessage } from 'element-plus'

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
let listRefreshTimer = null

const tableData = ref([])
const dialogVisible = ref(false)
const currentItem = ref(null)
const messages = ref([])
const chatContainer = ref(null)

const handleSearch = async () => {
    const params = {
        currentPage: currentPage.value,
        size: pageSize.value
    }

    console.log('请求分页参数:', params)

    try {
        const result = await getConsultationPage(params)
        console.log('咨询记录API返回:', JSON.stringify(result, null, 2))

        if (result && result.records) {
            tableData.value = result.records
            total.value = result.total || 0
        } else if (Array.isArray(result)) {
            tableData.value = result
            total.value = result.length
        } else if (result && result.data && Array.isArray(result.data)) {
            tableData.value = result.data
            total.value = result.total || result.totalCount || 0
        } else {
            tableData.value = []
            total.value = 0
        }

        console.log('当前页码:', currentPage.value, '数据条数:', tableData.value.length, '总条数:', total.value)
        if (tableData.value.length > 0) {
            console.log('第一条数据ID:', tableData.value[0].id)
        }
    } catch (error) {
        console.error('获取咨询记录失败:', error)
        ElMessage.error('获取咨询记录失败')
    }
}

const handleDetail = async (item) => {
    currentItem.value = item
    dialogVisible.value = true
    
    try {
        const result = await getSessionDetail(item.id)
        console.log('会话详情API返回:', JSON.stringify(result, null, 2))
        
        if (result && result.records) {
            messages.value = result.records
        } else if (Array.isArray(result)) {
            messages.value = result
        } else if (result && result.data && Array.isArray(result.data)) {
            messages.value = result.data
        } else {
            messages.value = []
        }
        
        if (messages.value.length > 0) {
            console.log('所有消息字段:', Object.keys(messages.value[0]))
            messages.value.forEach((msg, idx) => {
                console.log(`消息${idx+1}: role=${msg.role}, sender=${msg.sender}, from=${msg.from}, content前20字=${msg.content?.substring(0, 20)}`)
            })
        }
        
        nextTick(() => {
            if (chatContainer.value) {
                chatContainer.value.scrollTop = chatContainer.value.scrollHeight
            }
        })
    } catch (error) {
        console.error('获取会话详情失败:', error)
        ElMessage.error('获取会话详情失败')
    }
}

const handleDialogClose = () => {
    currentItem.value = null
    messages.value = []
}

const isUserMessage = (message) => {
    if (!message) return false
    // 优先检查 senderType 字段：1 = 用户，2 = AI助手
    if (message.senderType !== undefined && message.senderType !== null && message.senderType !== '') {
        return Number(message.senderType) === 1
    }
    // 兼容 senderTypeDesc 字段
    if (message.senderTypeDesc) {
        const desc = String(message.senderTypeDesc)
        return desc === '用户' || desc.toLowerCase() === 'user'
    }
    // 兼容其他字段命名
    const sender = message.sender || message.role || message.from || ''
    const lowerSender = String(sender).toLowerCase()
    return lowerSender === 'user' || lowerSender === 'client' || lowerSender === 'u' || lowerSender === 'c'
}

const getSenderLabel = (message) => {
    if (!message) return '未知'
    if (isUserMessage(message)) {
        return '用户'
    }
    return message.senderTypeDesc || 'AI助手'
}

const formatDate = (dateStr) => {
    if (!dateStr) return '-'
    try {
        const date = new Date(dateStr)
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        return `${year}-${month}-${day}`
    } catch {
        return String(dateStr).split(' ')[0] || '-'
    }
}

const formatTime = (dateStr) => {
    if (!dateStr) return '-'
    try {
        const date = new Date(dateStr)
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        const seconds = String(date.getSeconds()).padStart(2, '0')
        return `${hours}:${minutes}:${seconds}`
    } catch {
        const str = String(dateStr)
        const timePart = str.split(' ')[1]
        return timePart || '-'
    }
}

const formatDateTime = (dateStr) => {
    if (!dateStr) return '-'
    try {
        const date = new Date(dateStr)
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        const seconds = String(date.getSeconds()).padStart(2, '0')
        return `${year}/${month}/${day} ${hours}:${minutes}:${seconds}`
    } catch {
        return String(dateStr)
    }
}

onMounted(() => {
    handleSearch()
    listRefreshTimer = setInterval(() => {
        handleSearch()
    }, 10000)
})

onUnmounted(() => {
    if (listRefreshTimer) {
        clearInterval(listRefreshTimer)
        listRefreshTimer = null
    }
})
</script>
<style lang="scss" scoped>
.page-content {
    min-height: calc(100vh - 80px);
}
.consultation-container {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    padding: 20px;
}
.consultation-table {
    width: 100%;
    border-collapse: collapse;
    background: #fafafa;
    border-radius: 8px;
    overflow: hidden;
    thead {
        background: #fff;
        th {
            padding: 12px 15px;
            font-size: 14px;
            font-weight: 500;
            color: #606266;
            text-align: left;
            border-bottom: 1px solid #e4e7ed;
        }
    }
    tbody {
        tr {
            border-bottom: 1px solid #e4e7ed;
            &:hover {
                background: #f5f7fa;
            }
            &:last-child {
                border-bottom: none;
            }
            td {
                padding: 15px;
                vertical-align: top;
            }
        }
    }
}
.col-avatar {
    width: 80px;
    text-align: center;
    .avatar {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background: #c0c4cc;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 14px;
        font-weight: 500;
        margin: 0 auto;
        
        &.empty-avatar {
            background: #c0c4cc;
        }
    }
}
.col-content {
    flex: 1;
    min-width: 300px;
    .content-title {
        font-size: 14px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 8px;
    }
    .content-body {
        font-size: 13px;
        color: #606266;
        line-height: 1.5;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 500px;
    }
}
.col-count {
    width: 80px;
    text-align: center;
    font-size: 14px;
    color: #606266;
}
.col-time {
    width: 120px;
    text-align: center;
    .time-row {
        font-size: 13px;
        color: #909399;
        line-height: 1.4;
    }
}
.col-action {
    width: 80px;
    text-align: center;
}
.detail-info {
    background: #fafafa;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 20px;
    .info-row {
        display: flex;
        margin-bottom: 8px;
        &:last-child {
            margin-bottom: 0;
        }
        .info-label {
            width: 80px;
            font-size: 13px;
            color: #909399;
            flex-shrink: 0;
        }
        .info-value {
            font-size: 13px;
            color: #303133;
        }
    }
}
.dialog-title {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
    margin-bottom: 12px;
}
.chat-container {
    max-height: 400px;
    overflow-y: auto;
    background: #fafafa;
    border-radius: 8px;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 12px;
}
.chat-item {
    border-radius: 8px;
    padding: 12px 16px;
    position: relative;
    &.user {
        background: #ecf5ff;
        .chat-label {
            color: #409eff;
        }
    }
    &.ai {
        background: #f0f9eb;
        .chat-label {
            color: #67c23a;
        }
    }
    .chat-label {
        font-size: 12px;
        font-weight: 500;
        margin-bottom: 6px;
        display: block;
    }
    .chat-text {
        font-size: 13px;
        color: #303133;
        line-height: 1.6;
        word-break: break-word;
        margin-bottom: 8px;
    }
    .chat-time {
        font-size: 11px;
        color: #909399;
        text-align: right;
        display: block;
    }
}
</style>