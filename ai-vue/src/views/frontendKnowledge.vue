<template>
    <div class="knowledge-container">
        <div class="knowledge-header">
            <div class="header-left">
                <img :src="bookIcon" class="header-icon" alt="书本">
                <h1>{{ isDetail ? '知识文章详情' : '知识库' }}</h1>
            </div>
        </div>

        <div v-if="!isDetail" class="knowledge-content">
            <div class="sidebar">
                <div class="sidebar-section">
                    <h3 class="sidebar-title">推荐阅读</h3>
                    <div class="article-list">
                        <div 
                            v-for="article in sidebarArticles" 
                            :key="article.id"
                            class="article-item"
                            :class="{ active: selectedArticle?.id === article.id }"
                            @click="selectArticle(article)"
                        >
                            <div class="article-title">{{ article.title }}</div>
                            <div class="article-meta">
                                <el-icon><Document /></el-icon>
                                <span class="read-count">阅读量 {{ article.readCount || 0 }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="main-content">
                <div class="article-grid">
                    <div 
                        v-for="article in articles" 
                        :key="article.id"
                        class="article-card"
                        :class="{ active: selectedArticle?.id === article.id }"
                        @click="selectArticle(article)"
                    >
                        <div class="article-image">
                            <el-image 
                                :src="getImageUrl(article)" 
                                alt="文章图片" 
                                fit="cover"
                                @error="(e) => handleImageError(e, article)"
                            />
                        </div>
                        <div class="article-info">
                            <div class="article-header">
                                <h4 class="article-title">{{ article.title }}</h4>
                                <span class="article-tag">{{ article.categoryName }}</span>
                            </div>
                            <div class="article-meta">
                                <span class="meta-item">
                                    <el-icon><User /></el-icon>
                                    {{ article.authorName }}
                                </span>
                                <span class="meta-item">
                                    <el-icon><Calendar /></el-icon>
                                    {{ formatDate(article.createdAt) }}
                                </span>
                                <span class="meta-item">
                                    <el-icon><Document /></el-icon>
                                    观看人数 {{ article.readCount || 0 }}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="pagination-wrapper">
                    <el-pagination
                        :page-size="pagination.size"
                        layout="prev, pager, next"
                        :total="pagination.total"
                        @change="handlePageChange"
                    />
                </div>
            </div>
        </div>

        <div v-else class="detail-content">
            <div class="back-btn" @click="goBack">
                <el-icon><ArrowLeft /></el-icon>
                返回列表
            </div>
            
            <div class="detail-card">
                <div class="article-info-header">
                    <span class="article-tag">{{ articleDetail.categoryName }}</span>
                    <span class="article-date">
                        <el-icon><Calendar /></el-icon>
                        {{ formatDate(articleDetail.createdAt) }}
                    </span>
                </div>
                <h2 class="article-title">{{ articleDetail.title }}</h2>
                <p class="article-summary">{{ articleDetail.summary }}</p>
                <div class="article-author">
                    <span class="author-name">
                        <el-icon><User /></el-icon>
                        {{ articleDetail.authorName }}
                    </span>
                    <span class="read-count">
                        <el-icon><Document /></el-icon>
                        {{ articleDetail.readCount || 0 }} 次阅读
                    </span>
                </div>
            </div>

            <div class="detail-card">
                <h3 class="content-title">正文内容</h3>
                <div class="article-content" v-html="articleDetail.content"></div>
                <div class="article-tags">
                    <span class="tags-label">相关标签</span>
                    <span 
                        v-for="(tag, index) in parsedTags" 
                        :key="index" 
                        class="tag-item"
                    >{{ tag }}</span>
                </div>
            </div>
        </div>

        <div class="knowledge-footer">
            <p>© 2026 心理健康AI助手. All rights reserved.</p>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ArrowLeft, User, Calendar, Document } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getKnowledgeList, getKnowledgeDetail } from '@/api/frontend'
import { fileBaseUrl } from '@/config/index.js'

const router = useRouter()
const route = useRoute()

const bookIcon = new URL('@/assets/images/book.png', import.meta.url).href

const articles = ref([])
const sidebarArticles = ref([])
const selectedArticle = ref(null)
const articleDetail = ref({})

const isDetail = computed(() => !!route.query.articleId)

const pagination = reactive({
    currentPage: 1,
    size: 5,
    total: 0
})

const formatDate = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const getImageUrl = (article) => {
    const image = article.coverImage || article.image || article.cover || article.imgUrl || article.avatar || article.picture
    if (!image) return ''
    if (typeof image !== 'string') return ''
    if (image.startsWith('data:')) return image
    if (image.startsWith('http')) return image
    if (image === 'string') return ''
    if (image.startsWith('/')) {
        return fileBaseUrl + image
    }
    return fileBaseUrl + '/' + image
}

const handleImageError = async (event, article) => {
    event.target.style.display = 'none'
    
    const image = article?.coverImage
    if (!image) return
    
    const token = localStorage.getItem('token')
    if (!token) {
        localStorage.removeItem('userInfo')
        window.location.href = '/auth/login'
        return
    }
    
    if (!image.startsWith('http') && !image.startsWith('data:')) {
        const url = fileBaseUrl + image
        try {
            const response = await fetch(url, {
                headers: { 'token': token }
            })
            if (response.status === 403) {
                ElMessage.error('登录已过期，请重新登录')
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                setTimeout(() => {
                    window.location.href = '/auth/login'
                }, 1500)
            }
        } catch (error) {
            console.error('检查图片token状态失败:', error)
        }
    }
}

const parsedTags = computed(() => {
    const tags = articleDetail.value.tags
    if (!tags) return []
    if (Array.isArray(tags)) return tags
    if (typeof tags === 'string') {
        return tags.split(/[,，、]/).map(tag => tag.trim()).filter(tag => tag)
    }
    return []
})

const fetchArticles = async () => {
    const params = {
        sortField: 'readCount',
        sortDirection: 'desc',
        currentPage: pagination.currentPage,
        size: pagination.size
    }
    
    const res = await getKnowledgeList(params)
    if (res.records) {
        articles.value = res.records
        pagination.total = res.total
        
        for (const article of articles.value) {
            try {
                const detail = await getKnowledgeDetail(article.id)
                if (detail.coverImage) {
                    article.coverImage = detail.coverImage
                }
            } catch (error) {
                console.error('获取文章详情失败:', error)
            }
        }
    }
}

const fetchSidebarArticles = async () => {
    const params = {
        sortField: 'readCount',
        sortDirection: 'desc',
        currentPage: 1,
        size: 10
    }
    
    const res = await getKnowledgeList(params)
    if (res.records) {
        sidebarArticles.value = res.records
    }
}

const selectArticle = async (article) => {
    selectedArticle.value = article
    router.push({ query: { articleId: article.id } })
}

const handlePageChange = (page) => {
    pagination.currentPage = page
    fetchArticles()
}

const goBack = () => {
    router.push({ query: {} })
}

const loadArticleDetail = async (articleId) => {
    if (!articleId) return
    
    const res = await getKnowledgeDetail(articleId)
    articleDetail.value = res
    
    const article = articles.value.find(a => a.id === parseInt(articleId))
    if (article && res.coverImage) {
        article.coverImage = res.coverImage
    }
    const sidebarArticle = sidebarArticles.value.find(a => a.id === parseInt(articleId))
    if (sidebarArticle && res.coverImage) {
        sidebarArticle.coverImage = res.coverImage
    }
    
    selectedArticle.value = { id: parseInt(articleId), title: res.title }
}

watch(() => route.query.articleId, async (newId) => {
    await loadArticleDetail(newId)
})

onMounted(() => {
    fetchArticles()
    fetchSidebarArticles()
    loadArticleDetail(route.query.articleId)
})
</script>

<style scoped lang="scss">
.knowledge-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
    padding: 0 0 60px;
}

.knowledge-header {
    background: linear-gradient(135deg, #d97706 0%, #7c3aed 100%);
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

.knowledge-content {
    max-width: 1200px;
    margin: 30px auto;
    padding: 0 20px;
    display: flex;
    gap: 24px;
}

.sidebar {
    width: 280px;
    flex-shrink: 0;
    
    .sidebar-section {
        background: #fff;
        border-radius: 12px;
        padding: 20px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    }
    
    .sidebar-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin: 0 0 16px;
        padding-bottom: 10px;
        border-bottom: 2px solid #f59e0b;
        position: relative;
        
        &::after {
            content: '';
            position: absolute;
            bottom: -2px;
            left: 0;
            width: 40px;
            height: 2px;
            background: #f59e0b;
        }
    }
    
    .article-list {
        .article-item {
            padding: 12px 0;
            border-bottom: 1px solid #f5f5f5;
            cursor: pointer;
            transition: all 0.2s;
            
            &:last-child {
                border-bottom: none;
            }
            
            &:hover, &.active {
                background: #fffbeb;
                padding-left: 8px;
            }
            
            .article-title {
                font-size: 14px;
                color: #333;
                margin-bottom: 4px;
            }
            
            .article-meta {
                display: flex;
                align-items: center;
                gap: 4px;
                font-size: 12px;
                color: #999;
            }
        }
    }
}

.main-content {
    flex: 1;
    
    .article-grid {
        display: flex;
        flex-direction: column;
        gap: 16px;
    }
    
    .article-card {
        background: #fff;
        border-radius: 12px;
        padding: 16px;
        display: flex;
        gap: 20px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
        cursor: pointer;
        transition: all 0.2s;
        
        &:hover {
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
        }
        
        &.active {
            border: 2px solid #f59e0b;
        }
        
        .article-image {
            width: 200px;
            height: 140px;
            border-radius: 8px;
            overflow: hidden;
            flex-shrink: 0;
            background: #f5f5f5;
            
            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
                display: block;
            }
        }
        
        .article-info {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            
            .article-header {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                
                .article-title {
                    font-size: 18px;
                    font-weight: 600;
                    color: #333;
                    margin: 0;
                    line-height: 1.4;
                }
                
                .article-tag {
                    font-size: 12px;
                    color: #fff;
                    background: #6366f1;
                    padding: 4px 10px;
                    border-radius: 4px;
                    flex-shrink: 0;
                }
            }
            
            .article-meta {
                display: flex;
                flex-direction: column;
                gap: 8px;
                font-size: 14px;
                color: #555;
                font-weight: 600;
                
                .meta-item {
                    display: flex;
                    align-items: center;
                    gap: 6px;
                }
            }
        }
    }
    
    .pagination-wrapper {
        display: flex;
        justify-content: center;
        margin-top: 30px;
    }
}

.detail-content {
    max-width: 800px;
    margin: 30px auto;
    padding: 0 20px;
    
    .back-btn {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #6366f1;
        cursor: pointer;
        font-size: 14px;
        margin-bottom: 20px;
        transition: color 0.2s;
        
        &:hover {
            color: #4f46e5;
        }
    }
    
    .detail-card {
        background: #fff;
        border-radius: 12px;
        padding: 24px;
        margin-bottom: 20px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    }
    
    .article-info-header {
        display: flex;
        gap: 12px;
        margin-bottom: 16px;
        
        .article-tag {
            font-size: 12px;
            color: #fff;
            background: #6366f1;
            padding: 4px 10px;
            border-radius: 4px;
        }
        
        .article-date {
            font-size: 14px;
            color: #888;
            display: flex;
            align-items: center;
            gap: 4px;
        }
    }
    
    .article-title {
        font-size: 24px;
        font-weight: 600;
        color: #333;
        margin: 0 0 16px;
    }
    
    .article-summary {
        font-size: 15px;
        color: #666;
        line-height: 1.6;
        padding-left: 12px;
        border-left: 4px solid #86efac;
        background: #f0fdf4;
        padding: 12px;
        margin: 0 0 16px;
    }
    
    .article-author {
        display: flex;
        gap: 16px;
        font-size: 14px;
        
        span {
            display: flex;
            align-items: center;
            gap: 4px;
        }
        
        .author-name {
            color: #333;
            font-weight: 500;
        }
        
        .read-count {
            color: #999;
        }
    }
    
    .content-title {
        font-size: 18px;
        font-weight: 600;
        color: #333;
        margin: 0 0 16px;
    }
    
    .article-content {
        font-size: 15px;
        color: #555;
        line-height: 1.8;
        margin-bottom: 24px;
    }
    
    .article-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        
        .tags-label {
            font-size: 14px;
            color: #888;
            margin-right: 4px;
        }
        
        .tag-item {
            font-size: 12px;
            color: #6366f1;
            background: #e0e7ff;
            padding: 4px 10px;
            border-radius: 4px;
        }
    }
}

.knowledge-footer {
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