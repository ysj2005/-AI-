<template>
    <div class="page-content">
        <div class="stats-cards">
            <div class="stat-card">
                <div class="stat-icon user-icon">
                    <span class="icon-text">👤</span>
                </div>
                <div class="stat-info">
                    <div class="stat-value">{{ overviewData?.systemOverview?.totalUsers || 0 }}</div>
                    <div class="stat-label">总用户数</div>
                    <div class="stat-change">活跃用户: {{ overviewData?.systemOverview?.activeUsers || 0 }}</div>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon diary-icon">
                    <span class="icon-text">📔</span>
                </div>
                <div class="stat-info">
                    <div class="stat-value">{{ overviewData?.systemOverview?.totalDiaries || 0 }}</div>
                    <div class="stat-label">情绪日志</div>
                    <div class="stat-change">今日新增: {{ overviewData?.systemOverview?.todayNewDiaries || 0 }}</div>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon chat-icon">
                    <span class="icon-text">💬</span>
                </div>
                <div class="stat-info">
                    <div class="stat-value">{{ overviewData?.systemOverview?.totalSessions || 0 }}</div>
                    <div class="stat-label">咨询会话</div>
                    <div class="stat-change">今日新增: {{ overviewData?.systemOverview?.todayNewSessions || 0 }}</div>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon emotion-icon">
                    <span class="icon-text">😊</span>
                </div>
                <div class="stat-info">
                    <div class="stat-value">{{ (overviewData?.systemOverview?.avgMoodScore || 0).toFixed(1) }}/10</div>
                    <div class="stat-label">平均情绪</div>
                    <div class="stat-change">情绪健康指数</div>
                </div>
            </div>
        </div>

        <div class="charts-grid">
            <div class="chart-card">
                <div class="chart-header">
                    <h3>情绪趋势分析</h3>
                </div>
                <div ref="emotionChartRef" class="chart-container"></div>
            </div>

            <div class="chart-card">
                <div class="chart-header">
                    <h3>咨询会话统计</h3>
                </div>
                <div class="stats-row">
                    <div class="mini-stat">
                        <div class="mini-value">{{ overviewData?.consultationStats?.totalSessions || 0 }}</div>
                        <div class="mini-label">总会话数</div>
                    </div>
                    <div class="mini-stat">
                        <div class="mini-value">{{ (overviewData?.consultationStats?.avgDurationMinutes || 0).toFixed(1) }}</div>
                        <div class="mini-label">平均时长</div>
                    </div>
                    <div class="mini-stat">
                        <div class="mini-value">{{ overviewData?.systemOverview?.activeUsers || 0 }}</div>
                        <div class="mini-label">活跃用户</div>
                    </div>
                </div>
                <div ref="consultationChartRef" class="chart-container"></div>
            </div>
        </div>

        <div class="chart-card full-width">
            <div class="chart-header">
                <h3>用户活跃度趋势</h3>
            </div>
            <div ref="activityChartRef" class="chart-container"></div>
        </div>
    </div>
</template>
<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getAnalyticsOverview } from '@/api/admin'

const overviewData = ref(null)
const emotionChartRef = ref(null)
const consultationChartRef = ref(null)
const activityChartRef = ref(null)

let emotionChart = null
let consultationChart = null
let activityChart = null
let refreshTimer = null

const initEmotionChart = () => {
    if (!emotionChartRef.value) return
    emotionChart = echarts.init(emotionChartRef.value)
    
    const emotionTrend = overviewData.value?.emotionTrend || []
    const dates = emotionTrend.map(item => item.date)
    const avgScores = emotionTrend.map(item => item.avgMoodScore || 0)
    const recordCounts = emotionTrend.map(item => item.recordCount || 0)

    emotionChart.setOption({
        title: { text: '情绪趋势分析', left: 'center', textStyle: { fontSize: 14, fontWeight: 'bold', color: '#303133' } },
        tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
        legend: { data: ['平均情绪评分', '记录数量'], bottom: 10 },
        grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
        xAxis: { type: 'category', data: dates, axisLabel: { rotate: 45, fontSize: 10 } },
        yAxis: [
            { type: 'value', name: '情绪评分', min: 0, max: 10 },
            { type: 'value', name: '记录数量', min: 0 }
        ],
        series: [
            {
                name: '平均情绪评分',
                type: 'line',
                smooth: true,
                data: avgScores,
                lineStyle: { color: '#e6a23c', width: 2 },
                itemStyle: { color: '#e6a23c' },
                areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(230, 162, 60, 0.3)' }, { offset: 1, color: 'rgba(230, 162, 60, 0.05)' }]) }
            },
            {
                name: '记录数量',
                type: 'line',
                yAxisIndex: 1,
                smooth: true,
                data: recordCounts,
                lineStyle: { color: '#f56c6c', width: 2 },
                itemStyle: { color: '#f56c6c' },
                areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(245, 108, 108, 0.3)' }, { offset: 1, color: 'rgba(245, 108, 108, 0.05)' }]) }
            }
        ]
    })
}

const initConsultationChart = () => {
    if (!consultationChartRef.value) return
    consultationChart = echarts.init(consultationChartRef.value)
    
    const dailyTrend = overviewData.value?.consultationStats?.dailyTrend || []
    const dates = dailyTrend.map(item => item.date)
    const sessionCounts = dailyTrend.map(item => item.sessionCount || 0)
    const userCounts = dailyTrend.map(item => item.userCount || 0)

    consultationChart.setOption({
        title: { text: '咨询活动统计', left: 'center', textStyle: { fontSize: 14, fontWeight: 'bold', color: '#303133' } },
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { data: ['会话数量', '参与用户数'], bottom: 10 },
        grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
        xAxis: { type: 'category', data: dates, axisLabel: { rotate: 45, fontSize: 10 } },
        yAxis: { type: 'value', min: 0 },
        series: [
            {
                name: '会话数量',
                type: 'bar',
                data: sessionCounts,
                itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] }
            },
            {
                name: '参与用户数',
                type: 'bar',
                data: userCounts,
                itemStyle: { color: '#e6a23c', borderRadius: [4, 4, 0, 0] }
            }
        ]
    })
}

const initActivityChart = () => {
    if (!activityChartRef.value) return
    activityChart = echarts.init(activityChartRef.value)
    
    const userActivity = overviewData.value?.userActivity || []
    const dates = userActivity.map(item => item.date)
    const activeUsers = userActivity.map(item => item.activeUsers || 0)
    const newUsers = userActivity.map(item => item.newUsers || 0)
    const diaryUsers = userActivity.map(item => item.diaryUsers || 0)
    const consultationUsers = userActivity.map(item => item.consultationUsers || 0)

    activityChart.setOption({
        title: { text: '用户活跃度趋势', left: 'center', textStyle: { fontSize: 14, fontWeight: 'bold', color: '#303133' } },
        tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
        legend: { data: ['活跃用户', '新增用户', '日记用户', '咨询用户'], bottom: 10 },
        grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
        xAxis: { type: 'category', data: dates, axisLabel: { rotate: 45, fontSize: 10 } },
        yAxis: { type: 'value', min: 0 },
        series: [
            {
                name: '活跃用户',
                type: 'line',
                smooth: true,
                data: activeUsers,
                lineStyle: { color: '#409eff', width: 2 },
                itemStyle: { color: '#409eff' },
                areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(64, 158, 255, 0.3)' }, { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }]) }
            },
            {
                name: '新增用户',
                type: 'line',
                smooth: true,
                data: newUsers,
                lineStyle: { color: '#e6a23c', width: 2 },
                itemStyle: { color: '#e6a23c' },
                areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(230, 162, 60, 0.3)' }, { offset: 1, color: 'rgba(230, 162, 60, 0.05)' }]) }
            },
            {
                name: '日记用户',
                type: 'line',
                smooth: true,
                data: diaryUsers,
                lineStyle: { color: '#67c23a', width: 2 },
                itemStyle: { color: '#67c23a' },
                areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(103, 194, 58, 0.3)' }, { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }]) }
            },
            {
                name: '咨询用户',
                type: 'line',
                smooth: true,
                data: consultationUsers,
                lineStyle: { color: '#f56c6c', width: 2 },
                itemStyle: { color: '#f56c6c' },
                areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(245, 108, 108, 0.3)' }, { offset: 1, color: 'rgba(245, 108, 108, 0.05)' }]) }
            }
        ]
    })
}

const handleResize = () => {
    emotionChart?.resize()
    consultationChart?.resize()
    activityChart?.resize()
}

const fetchData = async () => {
    try {
        const result = await getAnalyticsOverview()
        overviewData.value = result
        
        initEmotionChart()
        initConsultationChart()
        initActivityChart()
    } catch (error) {
        console.error('获取数据分析失败:', error)
    }
}

onMounted(() => {
    fetchData()
    window.addEventListener('resize', handleResize)
    refreshTimer = setInterval(() => {
        fetchData()
    }, 15000)
})

onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    emotionChart?.dispose()
    consultationChart?.dispose()
    activityChart?.dispose()
    if (refreshTimer) {
        clearInterval(refreshTimer)
        refreshTimer = null
    }
})
</script>
<style lang="scss" scoped>
.page-content {
    padding: 24px;
}

.stats-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 24px;
}

.stat-card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    
    .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        
        &.user-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
        &.diary-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
        &.chat-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
        &.emotion-icon { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
        
        .icon-text { font-size: 24px; }
    }
    
    .stat-info {
        .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #303133;
        }
        
        .stat-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
        }
        
        .stat-change {
            font-size: 12px;
            color: #67c23a;
            margin-top: 4px;
        }
    }
}

.charts-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-auto-rows: 1fr;
    gap: 20px;
    margin-bottom: 24px;
}

.chart-card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    padding: 24px;
    
    &.full-width { grid-column: span 2; }
    
    .chart-header {
        h3 {
            font-size: 16px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 16px;
        }
    }
    
    .stats-row {
        display: flex;
        justify-content: space-around;
        margin-bottom: 20px;
        padding-bottom: 20px;
        border-bottom: 1px solid #f0f0f0;
        
        .mini-stat {
            text-align: center;
            
            .mini-value {
                font-size: 24px;
                font-weight: bold;
                color: #303133;
            }
            
            .mini-label {
                font-size: 12px;
                color: #909399;
                margin-top: 4px;
            }
        }
    }
    
    .chart-container {
        height: 300px;
    }
    
    &.full-width .chart-container {
        height: 350px;
    }
}

@media (max-width: 1200px) {
    .stats-cards {
        grid-template-columns: repeat(2, 1fr);
    }
    
    .charts-grid {
        grid-template-columns: 1fr;
    }
    
    .chart-card.full-width {
        grid-column: span 1;
    }
}

@media (max-width: 768px) {
    .stats-cards {
        grid-template-columns: 1fr;
    }
}
</style>