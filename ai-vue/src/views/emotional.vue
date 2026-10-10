<template>
    <div class="page-content">
        <div class="content-card">
            <PageHead title="情绪日志"></PageHead>
            <div class="search-row">
                <el-input 
                    v-model="searchForm.userId" 
                    placeholder="请输入用户ID" 
                    class="search-input"
                />
                <el-select 
                    v-model="searchForm.scoreRange" 
                    placeholder="请选择评分范围" 
                    class="search-select"
                >
                    <el-option label="低分 (1-3)" value="low" />
                    <el-option label="中分 (4-7)" value="medium" />
                    <el-option label="高分 (8-10)" value="high" />
                </el-select>
            </div>
            <div class="button-row">
                <el-button type="primary" @click="handleSearch">查询</el-button>
                <el-button @click="handleReset">重置</el-button>
            </div>

            <div class="scroll-container">
                <table class="emotional-list">
                <thead>
                    <tr class="list-header">
                        <th class="col-user-id">用户ID</th>
                        <th class="col-session">会话ID</th>
                        <th class="col-date">记录日期</th>
                        <th class="col-score">情绪评分</th>
                        <th class="col-life">生活指标</th>
                        <th class="col-trigger">情绪触发因素</th>
                        <th class="col-content">日记内容</th>
                        <th class="col-action">操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr 
                        v-for="item in tableData" 
                        :key="item.id" 
                        class="list-item"
                    >
                        <td class="col-user-id">{{ item.userId || '-' }}</td>
                        <td class="col-session">
                            <div class="avatar">
                                <span>{{ item.nickname || '?' }}</span>
                            </div>
                        </td>
                        <td class="col-date">{{ formatDate(item.diaryDate || item.createdAt) }}</td>
                        <td class="col-score">
                            <span 
                                v-for="i in (isWindowSmall && minScore > 0 ? minScore : (item.moodScore || 0))" 
                                :key="i" 
                                class="star active"
                            >⭐</span>
                        </td>
                        <td class="col-life">
                            <div>睡眠: {{ item.sleepQuality || 0 }}/5</div>
                            <div>压力: {{ item.stressLevel || 0 }}/5</div>
                        </td>
                        <td class="col-trigger">{{ item.emotionTriggers || '-' }}</td>
                        <td class="col-content">{{ item.diaryContent || '-' }}</td>
                        <td class="col-action">
                            <el-button @click="handleDetail(item)" text type="primary">详情</el-button>
                            <el-button @click="handleDelete(item)" text type="danger">删除</el-button>
                        </td>
                    </tr>
                </tbody>
            </table>
            </div>
            <el-pagination
                style="margin-top: 25px"
                :page-size="pagination.size"
                layout="prev, pager, next"
                :total="pagination.total"
                @change="handleChange"
            />
        </div>
        
        <el-dialog
            title="删除确认"
            v-model="deleteDialogVisible"
            width="30%"
            :close-on-click-modal="false"
            :show-close="false"
            top="50vh"
            class="center-dialog"
        >
            <p>确定删除该条记录吗？</p>
            <template #footer>
                <el-button @click="deleteDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="confirmDelete">确定</el-button>
            </template>
        </el-dialog>
        
        <el-dialog
            title="情绪日志详情"
            v-model="dialogVisible"
            width="70%"
            :close-on-click-modal="false"
            @close="handleDialogClose"
        >
            <div class="detail-section">
                <h4>用户信息</h4>
                <div class="detail-table">
                    <div class="table-row">
                        <div class="table-cell label-cell">用户名</div>
                        <div class="table-cell">{{ currentItem?.username || '-' }}</div>
                        <div class="table-cell label-cell">昵称</div>
                        <div class="table-cell">{{ currentItem?.nickname || '-' }}</div>
                    </div>
                    <div class="table-row">
                        <div class="table-cell label-cell">用户ID</div>
                        <div class="table-cell">{{ currentItem?.userId || '-' }}</div>
                        <div class="table-cell label-cell">记录日期</div>
                        <div class="table-cell">{{ formatDate(currentItem?.diaryDate || currentItem?.createdAt) }}</div>
                    </div>
                </div>
            </div>
            
            <div class="detail-section">
                <h4>情绪状态</h4>
                <div class="detail-table">
                    <div class="table-row">
                        <div class="table-cell label-cell">情绪评分</div>
                        <div class="table-cell star-cell">
                            <span 
                                v-for="i in 10" 
                                :key="i" 
                                :class="['star', i <= (currentItem?.moodScore || 0) ? 'active' : '']"
                            >★</span>
                        </div>
                        <div class="table-cell label-cell">主要情绪</div>
                        <div class="table-cell">
                            <span class="emotion-tag">{{ currentItem?.dominantEmotion || '-' }}</span>
                        </div>
                    </div>
                    <div class="table-row">
                        <div class="table-cell label-cell">睡眠质量</div>
                        <div class="table-cell">{{ currentItem?.sleepQuality || 0 }}/5</div>
                        <div class="table-cell label-cell">压力水平</div>
                        <div class="table-cell">{{ currentItem?.stressLevel || 0 }}/5</div>
                    </div>
                </div>
            </div>
            
            <div class="detail-section">
                <h4>日记内容</h4>
                <div class="detail-table">
                    <div class="table-row">
                        <div class="table-cell label-cell">情绪触发因素</div>
                        <div class="table-cell" colspan="3">{{ currentItem?.emotionTriggers || '-' }}</div>
                    </div>
                    <div class="table-row">
                        <div class="table-cell label-cell">日记内容</div>
                        <div class="table-cell" colspan="3">{{ currentItem?.diaryContent || '-' }}</div>
                    </div>
                </div>
            </div>
            
            <div class="detail-section">
                <h4>AI情绪分析结果</h4>
                <div class="detail-table">
                    <div class="table-row">
                        <div class="table-cell label-cell">主要情绪</div>
                        <div class="table-cell">
                            <span class="emotion-tag">{{ analysisData?.primaryEmotion || currentItem?.dominantEmotion || '无' }}</span>
                        </div>
                        <div class="table-cell label-cell">情绪强度</div>
                        <div class="table-cell">
                            <div class="intensity-bar">
                                <div 
                                    class="intensity-fill" 
                                    :style="{ width: (analysisData?.emotionScore || 0) + '%', background: getIntensityColor(analysisData?.emotionScore || 0) }"
                                ></div>
                            </div>
                            <span class="intensity-percent">{{ analysisData?.emotionScore || 0 }}%</span>
                        </div>
                    </div>
                    <div class="table-row">
                        <div class="table-cell label-cell">风险等级</div>
                        <div class="table-cell">
                            <span class="risk-tag">{{ analysisData?.riskLevel !== undefined && analysisData.riskLevel !== 0 ? analysisData.riskLevel : '无' }}</span>
                        </div>
                        <div class="table-cell label-cell">情绪性质</div>
                        <div class="table-cell">
                            <span class="nature-tag" :class="getEmotionClass(getEmotionNature())">
                                {{ getEmotionNature() }}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="detail-section advice-section">
                <h4>专业建议</h4>
                <div class="advice-box">
                    <span class="advice-content">{{ analysisData?.suggestion || '无' }}</span>
                </div>
            </div>
            
            <div class="detail-section advice-section">
                <h4>风险描述</h4>
                <div class="advice-box">
                    <span class="advice-content">{{ analysisData?.riskDescription || '无' }}</span>
                </div>
            </div>
            
            <div class="detail-section advice-section">
                <h4>改善建议</h4>
                <div class="advice-box">
                    <div class="suggestion-list" v-if="(analysisData?.improvementSuggestions || []).length > 0">
                        <div 
                            v-for="(suggestion, index) in (analysisData?.improvementSuggestions || [])" 
                            :key="index"
                            class="suggestion-item"
                        >
                            {{ suggestion }}
                        </div>
                    </div>
                    <span v-else class="advice-content">无</span>
                </div>
            </div>
            
            <div class="detail-section">
                <h4>时间信息</h4>
                <div class="detail-table">
                    <div class="table-row">
                        <div class="table-cell label-cell">创建时间</div>
                        <div class="table-cell">{{ formatDateTime(currentItem?.createdAt) }}</div>
                        <div class="table-cell label-cell">更新时间</div>
                        <div class="table-cell">{{ formatDateTime(currentItem?.updatedAt) }}</div>
                    </div>
                </div>
            </div>
            
            <template #footer>
                <el-button @click="dialogVisible = false">关闭</el-button>
            </template>
        </el-dialog>
    </div>
</template>
<script setup>
import { onMounted, onUnmounted, ref, reactive, computed } from 'vue'
import PageHead from '@/components/PageHead.vue'
import { getEmotionalPage, deleteEmotional } from '@/api/admin'
import { ElMessage } from 'element-plus'

const pagination = reactive({
    currentPage: 1,
    size: 10,
    total: 0
})

const searchForm = reactive({
    userId: '',
    scoreRange: ''
})

const tableData = ref([])
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const deleteId = ref(null)
const currentItem = ref(null)
const isWindowSmall = ref(false)
let listRefreshTimer = null
let detailRefreshTimer = null

const analysisData = computed(() => {
    if (!currentItem.value || !currentItem.value.aiEmotionAnalysis) {
        console.log('no aiEmotionAnalysis:', currentItem.value?.aiEmotionAnalysis)
        return {}
    }
    try {
        const analysis = typeof currentItem.value.aiEmotionAnalysis === 'string' 
            ? JSON.parse(currentItem.value.aiEmotionAnalysis) 
            : currentItem.value.aiEmotionAnalysis
        console.log('analysisData:', analysis)
        return analysis
    } catch (e) {
        console.log('parse error:', e)
        return {}
    }
})

const minScore = computed(() => {
    if (!tableData.value || tableData.value.length === 0) return 0
    const scores = tableData.value.map(item => item.moodScore || 0).filter(s => s > 0)
    if (scores.length === 0) return 0
    return Math.min(...scores)
})

const handleSearch = async () => {
    const params = {
        current: pagination.currentPage,
        size: pagination.size
    }
    if (searchForm.userId) {
        params.userId = searchForm.userId
    }
    if (searchForm.scoreRange) {
        if (searchForm.scoreRange === 'low') {
            params.minMoodScore = 1
            params.maxMoodScore = 3
        } else if (searchForm.scoreRange === 'medium') {
            params.minMoodScore = 4
            params.maxMoodScore = 7
        } else if (searchForm.scoreRange === 'high') {
            params.minMoodScore = 8
            params.maxMoodScore = 10
        }
    }
    
    try {
        const result = await getEmotionalPage(params)
        console.log('情绪日志API返回:', result)
        let records = []
        let total = 0
        
        if (result && result.records) {
            records = result.records
            total = result.total || 0
        } else if (Array.isArray(result)) {
            records = result
            total = result.length
        } else if (result && result.data && Array.isArray(result.data)) {
            records = result.data
            total = result.total || result.totalCount || 0
        } else if (result && typeof result === 'object') {
            const keys = Object.keys(result)
            for (const key of keys) {
                if (Array.isArray(result[key])) {
                    records = result[key]
                    total = result.total || result.totalCount || records.length
                    break
                }
            }
        }
        
        tableData.value = records
        pagination.total = total
    } catch (error) {
        console.error('获取情绪日志失败:', error)
        ElMessage.error('获取情绪日志失败')
    }
}

const handleChange = (page) => {
    pagination.currentPage = page
    handleSearch()
}

const handleReset = () => {
    searchForm.userId = ''
    searchForm.scoreRange = ''
    pagination.currentPage = 1
    handleSearch()
}

const handleDetail = (item) => {
    currentItem.value = item
    dialogVisible.value = true
}

const handleDialogClose = () => {
    currentItem.value = null
    if (detailRefreshTimer) {
        clearInterval(detailRefreshTimer)
        detailRefreshTimer = null
    }
}

const handleDelete = (item) => {
    deleteId.value = item.id
    deleteDialogVisible.value = true
}

const confirmDelete = async () => {
    if (!deleteId.value) return
    try {
        await deleteEmotional(deleteId.value)
        ElMessage.success('删除成功')
        handleSearch()
    } catch (error) {
        ElMessage.error('删除失败')
    }
    deleteDialogVisible.value = false
}

const getEmotionClass = (nature) => {
    if (!nature) return ''
    if (nature.includes('正面') || nature.includes('积极')) return 'positive'
    if (nature.includes('负面') || nature.includes('消极')) return 'negative'
    return ''
}

const getIntensityColor = (score) => {
    if (score >= 70) return '#67c23a'
    if (score >= 30) return '#e6a23c'
    return '#f56c6c'
}

const positiveEmotions = ['开心', '快乐', '高兴', '兴奋', '愉悦', '平静', '满足', '幸福', '期待', '惊喜']
const negativeEmotions = ['悲伤', '难过', '焦虑', '恐惧', '愤怒', '失落', '沮丧', '忧郁', '伤心', '绝望']

const getEmotionNature = () => {
    if (analysisData.value?.emotionNature) {
        return analysisData.value.emotionNature
    }
    if (analysisData.value?.isNegative !== undefined) {
        const isNeg = String(analysisData.value.isNegative).toLowerCase()
        return isNeg === 'false' ? '正面情绪' : '负面情绪'
    }
    const emotion = currentItem.value?.dominantEmotion || ''
    if (positiveEmotions.some(e => emotion.includes(e))) {
        return '正面情绪'
    }
    if (negativeEmotions.some(e => emotion.includes(e))) {
        return '负面情绪'
    }
    return '无'
}

const getAdviceList = (suggestions) => {
    if (!suggestions) return []
    if (Array.isArray(suggestions)) return suggestions
    if (typeof suggestions === 'string') {
        return suggestions.split(/[\n;；]/).filter(s => s.trim())
    }
    return []
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
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    } catch {
        return String(dateStr)
    }
}

onMounted(() => {
    handleSearch()
    listRefreshTimer = setInterval(() => {
        handleSearch()
    }, 10000)
    
    checkWindowSize()
    window.addEventListener('resize', checkWindowSize)
})

const checkWindowSize = () => {
    isWindowSmall.value = window.innerWidth < 1200
}

onUnmounted(() => {
    if (listRefreshTimer) {
        clearInterval(listRefreshTimer)
        listRefreshTimer = null
    }
    if (detailRefreshTimer) {
        clearInterval(detailRefreshTimer)
        detailRefreshTimer = null
    }
    window.removeEventListener('resize', checkWindowSize)
})
</script>
<style lang="scss" scoped>
.page-content {
    min-height: calc(100vh - 80px);
}
.content-card {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    padding: 24px;
    .search-row {
        display: flex;
        align-items: center;
        gap: 20px;
        margin-bottom: 16px;
        .search-input {
            width: 220px;
        }
        .search-select {
            width: 160px;
        }
    }
    .button-row {
        display: flex;
        gap: 12px;
        margin-bottom: 24px;
    }
}
.scroll-container {
    overflow-x: auto;
    overflow-y: hidden;
    border-radius: 8px;
    width: 100%;
    position: relative;
    &::-webkit-scrollbar {
        height: 8px;
    }
    &::-webkit-scrollbar-track {
        background: #f1f1f1;
        border-radius: 4px;
    }
    &::-webkit-scrollbar-thumb {
        background: #c0c4cc;
        border-radius: 4px;
        &:hover {
            background: #909399;
        }
    }
}
.emotional-list {
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    overflow: hidden;
    width: 100%;
    table-layout: fixed;
    border-collapse: collapse;
    thead {
        background: #f5f7fa;
    }
    th, td {
        padding: 15px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        border-bottom: 1px solid #e4e7ed;
        vertical-align: middle;
    }
    th {
        font-weight: bold;
        color: #606266;
        font-size: 14px;
        text-align: left;
    }
    .col-user-id { width: 80px; }
    .col-session { width: 80px; }
    .col-date { width: 120px; text-align: center; }
    .col-score { width: 140px; text-align: center; }
    .col-life { width: 120px; text-align: center; }
    .col-trigger { width: 120px; text-align: center; }
    .col-content { width: auto; }
    .col-action { width: 120px; text-align: center; }
    tbody {
        .list-item {
            font-size: 14px;
            &:hover {
                background: #fafafa;
            }
            &:last-child td {
                border-bottom: none;
            }
            .col-user-id {
                color: #606266;
            }
            .col-session {
                .avatar {
                    width: 40px;
                    height: 40px;
                    border-radius: 50%;
                    background: #c0c4cc;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: #fff;
                    font-size: 12px;
                    text-align: center;
                }
            }
            .col-date {
                color: #909399;
            }
            .col-score {
                overflow: visible;
                .star {
                    font-size: 14px;
                    color: #f5f5f5;
                    &.active {
                        color: #f0c78a;
                        filter: drop-shadow(0 0 2px rgba(240, 199, 138, 0.3));
                    }
                }
            }
            .col-life {
                color: #606266;
                overflow: visible;
                div {
                    font-size: 13px;
                    white-space: nowrap;
                }
            }
            .col-trigger {
                color: #606266;
            }
            .col-content {
                color: #909399;
            }
            .col-action {
                display: flex;
                justify-content: center;
                gap: 10px;
                overflow: visible;
            }
        }
    }
}
.center-dialog {
    display: flex;
    align-items: center;
    justify-content: center;
    .el-dialog {
        margin-top: -50% !important;
        transform: translateY(50%);
    }
}
.detail-section {
    margin-bottom: 20px;
    h4 {
        margin-bottom: 12px;
        font-size: 14px;
        font-weight: bold;
        color: #303133;
        border-left: 4px solid #409eff;
        padding-left: 8px;
    }
}
.detail-table {
    width: 100%;
    .table-row {
        display: flex;
        border-bottom: 1px solid #f0f0f0;
        &:last-child {
            border-bottom: none;
        }
    }
    .table-cell {
        flex: 1;
        padding: 12px 0;
        font-size: 14px;
        color: #606266;
        &.label-cell {
            font-weight: 500;
            color: #909399;
            width: 120px;
            flex-shrink: 0;
        }
        &.star-cell {
            .star {
                font-size: 18px;
                color: #f5f5f5;
                margin-right: 2px;
                &.active {
                    color: #f0c78a;
                    filter: drop-shadow(0 0 2px rgba(240, 199, 138, 0.3));
                }
            }
        }
        &[colspan="3"] {
            flex: 3;
            color: #303133;
        }
    }
}
.emotion-tag {
    display: inline-block;
    padding: 4px 12px;
    background: #ecf5ff;
    color: #409eff;
    border-radius: 4px;
    font-size: 12px;
}
.risk-tag {
    display: inline-block;
    padding: 4px 12px;
    background: #fef0f0;
    color: #f56c6c;
    border-radius: 4px;
    font-size: 12px;
}
.nature-tag {
    display: inline-block;
    padding: 4px 12px;
    border-radius: 4px;
    font-size: 12px;
    &.positive {
        background: #f0f9eb;
        color: #67c23a;
    }
    &.negative {
        background: #fef0f0;
        color: #f56c6c;
    }
}
.intensity-bar {
    width: 150px;
    height: 8px;
    background: #f0f0f0;
    border-radius: 4px;
    overflow: hidden;
    margin-bottom: 4px;
    .intensity-fill {
        height: 100%;
        background: linear-gradient(90deg, #409eff, #67c23a);
        border-radius: 4px;
        transition: width 0.3s ease;
    }
}
.intensity-percent {
    font-size: 12px;
    color: #606266;
}
.advice-section {
    .advice-box {
        background: #fafafa;
        border-radius: 8px;
        padding: 16px;
        display: flex;
        flex-direction: column;
        gap: 8px;
        .advice-label {
            font-size: 14px;
            font-weight: 500;
            color: #909399;
        }
        .advice-content {
            font-size: 14px;
            color: #303133;
            line-height: 1.8;
        }
        .suggestion-list {
            display: flex;
            flex-direction: column;
            gap: 8px;
            .suggestion-item {
                font-size: 14px;
                color: #303133;
                line-height: 1.8;
                padding-left: 16px;
                position: relative;
                &::before {
                    content: '•';
                    position: absolute;
                    left: 0;
                    color: #409eff;
                }
            }
        }
    }
}
</style>