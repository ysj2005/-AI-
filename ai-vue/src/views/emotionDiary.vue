<template>
    <div class="emotion-diary-container">
        <div class="diary-header">
            <div class="header-left">
                <img :src="heartIcon" class="header-icon" alt="爱心">
                <h1>情绪日志</h1>
            </div>
        </div>

        <div class="diary-content">
            <div class="section">
                <h2 class="section-title">今日情绪评分</h2>
                <p class="section-desc">您今天的整体情绪状态如何？(1-10分)</p>
                <div class="star-rating" @mouseleave="hoverScore = 0">
                    <span 
                        v-for="star in 10" 
                        :key="star" 
                        class="star"
                        :class="{ active: moodScore >= star || hoverScore >= star }"
                        @click="setMoodScore(star)"
                        @mouseenter="hoverScore = star"
                    >★</span>
                </div>
                <span class="score-value">{{ moodScore }}分</span>
            </div>

            <div class="section">
                <h2 class="section-title">主要情绪</h2>
                <div class="emotion-grid">
                    <div 
                        v-for="emotion in emotions" 
                        :key="emotion.name"
                        class="emotion-item"
                        :class="{ active: dominantEmotion === emotion.name }"
                        @click="selectEmotion(emotion.name)"
                    >
                        <div class="emotion-icon">
                            <el-image :src="emotion.icon" style="width: 40px; height: 40px" />
                        </div>
                        <span class="emotion-name">{{ emotion.name }}</span>
                    </div>
                </div>
            </div>

            <div class="section">
                <h2 class="section-title">详细记录</h2>
                
                <div class="form-group">
                    <label class="form-label">情绪触发因素</label>
                    <el-input 
                        v-model="emotionTriggers" 
                        placeholder="今天什么事情影响了您的情绪？"
                        class="form-input"
                    />
                </div>

                <div class="form-group">
                    <label class="form-label">今日感想</label>
                    <el-input 
                        v-model="diaryContent" 
                        type="textarea"
                        placeholder="写下您今天的想法、感受或发生的有趣事情..."
                        :rows="4"
                        class="form-textarea"
                    />
                </div>

                <div class="form-row">
                    <div class="form-group half">
                        <label class="form-label">睡眠质量</label>
                        <el-select v-model="sleepQuality" placeholder="请选择" class="form-select">
                            <el-option label="很差" :value="1" />
                            <el-option label="较差" :value="2" />
                            <el-option label="一般" :value="3" />
                            <el-option label="良好" :value="4" />
                            <el-option label="优秀" :value="5" />
                        </el-select>
                    </div>
                    <div class="form-group half">
                        <label class="form-label">压力水平</label>
                        <el-select v-model="stressLevel" placeholder="请选择" class="form-select">
                            <el-option label="很低" :value="1" />
                            <el-option label="较低" :value="2" />
                            <el-option label="中等" :value="3" />
                            <el-option label="较高" :value="4" />
                            <el-option label="很高" :value="5" />
                        </el-select>
                    </div>
                </div>

                <div class="form-actions">
                    <el-button @click="resetForm" class="btn-reset">重置</el-button>
                    <el-button type="primary" @click="submitDiary" class="btn-submit">提交记录</el-button>
                </div>
            </div>
        </div>

        <div class="diary-footer">
            <p>© 2026 心理健康AI助手. All rights reserved.</p>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addEmotionDiary } from '@/api/frontend'

const moodScore = ref(5)
const hoverScore = ref(0)
const dominantEmotion = ref('')
const heartIcon = new URL('@/assets/images/like.png', import.meta.url).href
const emotionTriggers = ref('')
const diaryContent = ref('')
const sleepQuality = ref(null)
const stressLevel = ref(null)

const emotions = [
    { name: '开心', icon: new URL('@/assets/images/开心.png', import.meta.url).href },
    { name: '平静', icon: new URL('@/assets/images/平静.png', import.meta.url).href },
    { name: '焦虑', icon: new URL('@/assets/images/焦虑.png', import.meta.url).href },
    { name: '悲伤', icon: new URL('@/assets/images/悲伤.png', import.meta.url).href },
    { name: '兴奋', icon: new URL('@/assets/images/兴奋.png', import.meta.url).href },
    { name: '疲惫', icon: new URL('@/assets/images/疲惫.png', import.meta.url).href },
    { name: '惊讶', icon: new URL('@/assets/images/惊讶.png', import.meta.url).href },
    { name: '困惑', icon: new URL('@/assets/images/困惑.png', import.meta.url).href }
]

const setMoodScore = (score) => {
    moodScore.value = score
}

const selectEmotion = (emotion) => {
    dominantEmotion.value = emotion
}

const resetForm = () => {
    moodScore.value = 0
    hoverScore.value = 0
    dominantEmotion.value = ''
    emotionTriggers.value = ''
    diaryContent.value = ''
    sleepQuality.value = null
    stressLevel.value = null
    ElMessage.info('表单已重置')
}

const submitDiary = () => {
    if (!dominantEmotion.value) {
        ElMessage.error('请选择主要情绪')
        return
    }
    if (!emotionTriggers.value.trim()) {
        ElMessage.error('请填写情绪触发因素')
        return
    }
    if (!diaryContent.value.trim()) {
        ElMessage.error('请填写今日感想')
        return
    }
    if (!sleepQuality.value) {
        ElMessage.error('请选择睡眠质量')
        return
    }
    if (!stressLevel.value) {
        ElMessage.error('请选择压力水平')
        return
    }

    const today = new Date()
    const diaryDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

    addEmotionDiary({
        diaryDate,
        moodScore: moodScore.value,
        dominantEmotion: dominantEmotion.value,
        emotionTriggers: emotionTriggers.value,
        diaryContent: diaryContent.value,
        sleepQuality: sleepQuality.value,
        stressLevel: stressLevel.value
    }).then(res => {
        ElMessage.success('记录提交成功')
        resetForm()
    }).catch(err => {
        ElMessage.error('提交失败，请重试')
    })
}
</script>

<style scoped lang="scss">
.emotion-diary-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
    padding: 0 0 60px;
}

.diary-header {
    background: linear-gradient(135deg, #90c535 0%, #f59e0b 100%);
    padding: 20px 40px;
    color: #fff;
    
    .header-left {
        display: flex;
        align-items: center;
        gap: 12px;
    }
    
    .header-icon {
        width: 36px;
        height: 36px;
        filter: brightness(0) invert(1);
    }
    
    h1 {
        font-size: 24px;
        font-weight: 600;
        margin: 0;
    }
}

.diary-content {
    max-width: 900px;
    margin: 30px auto;
    padding: 0 20px;
}

.section {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0 0 12px;
}

.section-desc {
    font-size: 14px;
    color: #888;
    margin: 0 0 16px;
}

.star-rating {
    display: flex;
    gap: 8px;
    
    .star {
        font-size: 32px;
        color: #ddd;
        cursor: pointer;
        transition: all 0.2s;
        
        &:hover {
            transform: scale(1.1);
        }
        
        &.active {
            color: #fbbf24;
        }
    }
}

.score-value {
    display: inline-block;
    margin-left: 16px;
    font-size: 16px;
    font-weight: 600;
    color: #f59e0b;
}

.emotion-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
}

.emotion-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px 8px;
    border-radius: 10px;
    border: 2px solid #eee;
    cursor: pointer;
    transition: all 0.2s;
    background: #fafafa;
    
    &:hover {
        border-color: #f59e0b;
        background: #fffbeb;
    }
    
    &.active {
        border-color: #f59e0b;
        background: #fffbeb;
        box-shadow: 0 2px 8px rgba(245, 158, 11, 0.2);
    }
    
    .emotion-icon {
        margin-bottom: 8px;
    }
    
    .emotion-name {
        font-size: 13px;
        color: #666;
    }
}

.form-group {
    margin-bottom: 20px;
    
    &.half {
        flex: 1;
        min-width: 0;
    }
}

.form-label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #333;
    margin-bottom: 8px;
}

.form-input,
.form-textarea,
.form-select {
    width: 100%;
}

.form-row {
    display: flex;
    gap: 20px;
}

.form-actions {
    display: flex;
    gap: 16px;
    justify-content: flex-end;
    margin-top: 24px;
    
    .btn-reset {
        padding: 10px 24px;
    }
    
    .btn-submit {
        padding: 10px 32px;
    }
}

.diary-footer {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #1a1a2e;
    color: #888;
    text-align: center;
    padding: 16px;
    font-size: 13px;
}
</style>