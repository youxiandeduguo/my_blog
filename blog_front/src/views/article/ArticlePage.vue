<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { articleListService } from '@/api/article.js'
import { ElMessage } from 'element-plus'
import useUserInfoStore from '@/stores/userInfo.js'



const userInfoStore = useUserInfoStore()

// 获取完整用户信息对象
const userInfo = computed(() => userInfoStore.info)

// 获取单个属性
const nickname = computed(() => userInfoStore.info.nickname)


console.log('用户昵称:', nickname.value)

// 路由和状态
const route = useRoute()
const articles = ref([])
const loading = ref(true)
const error = ref(null)
const commentText = ref('')
const commentName = ref(nickname.value)
const comments = ref([])

// 计算属性
const articleTitle = computed(() => decodeURIComponent(route.params.title))
const articleCategory = computed(() => route.params.category)

const targetArticle = computed(() => {
  return articles.value.find(item => item.title === articleTitle.value) || {
    content: "内容加载中...",
    coverImg: ""
  }
})

// 本地存储键生成（使用文章ID+标题作为唯一标识）
const storageKey = computed(() => 
  `article_comments_${targetArticle.value?.id}_${articleTitle.value}`
)

// 加载文章和评论
const loadData = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: 1,
      pageSize: 10, // 增加获取数量以提高匹配成功率
      categoryId: articleCategory.value || null,
      state: null
    }
    const result = await articleListService(params)
    
    if (result?.data?.items) {
      articles.value = result.data.items
      loadComments() // 文章加载完成后加载评论
    } else {
      throw new Error('无效的数据结构')
    }
  } catch (err) {
    error.value = `数据加载失败: ${err.message}`
    console.error('请求错误:', err)
  } finally {
    loading.value = false
  }
}

// 加载评论
const loadComments = () => {
  const saved = localStorage.getItem(storageKey.value)
  comments.value = saved ? JSON.parse(saved) : []
}

// 提交评论
const submitComment = () => {
  if (!validateComment()) return
  
  const newComment = {
    id: Date.now(),
    name: commentName.value.trim(),
    content: commentText.value.trim(),
    date: new Date().toLocaleString()
  }
  
  comments.value.unshift(newComment)
  saveComments()
  clearForm()
  ElMessage.success('评论提交成功')
}

// 保存到本地存储
const saveComments = () => {
  localStorage.setItem(storageKey.value, JSON.stringify(comments.value))
}

// 表单验证
const validateComment = () => {
  if (!commentName.value.trim()) {
    ElMessage.warning('请输入昵称')
    return false
  }
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return false
  }
  return true
}

// 清空表单
const clearForm = () => {
  commentText.value = ''
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div v-if="loading" class="loading-container">
    <el-icon class="is-loading">
      <Loading />
    </el-icon>
    正在加载文章...
  </div>
  <div v-else-if="error" class="error-container">
    <el-alert type="error" :title="error" show-icon />
  </div>

  <div v-else class="article-container">
    <h1 class="title">{{ targetArticle.title || '未知标题' }}</h1>
    <div v-if="targetArticle.coverImg" class="cover-container">
      <el-image 
        :src="targetArticle.coverImg" 
        fit="cover" 
        class="cover-img"
      >
        <template #error>
          <div class="cover-error">图片加载失败</div>
        </template>
      </el-image>
    </div>
    <div class="content-container">
      <div class="content" v-html="targetArticle.content"></div>
    </div>
  </div>

  <!-- 评论区 -->
  <div class="comment-section">
    <h3 class="comment-title">评论（{{ comments.length }}）</h3>
    
    <!-- 评论表单 -->
    <div class="comment-form">
      <el-input
        v-if="nickname.value"
        v-model="commentName"
        placeholder="请输入昵称（必填）"
        class="name-input"
      />
      <el-input
        v-model="commentText"
        placeholder="写下你的评论..."
        type="textarea"
        :rows="3"
        class="content-input"
      />
      <div class="action-bar">
        <el-button 
          type="primary" 
          size="small"
          @click="submitComment"
        >
          提交评论
        </el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <div 
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <div class="comment-header">
          <span class="user-name">{{ comment.name }}</span>
          <span class="comment-date">{{ comment.date }}</span>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
      </div>
      
      <div 
        v-if="comments.length === 0"
        class="empty-comments"
      >
        暂无评论，快来抢沙发~
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>

.article-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px;

  .title {
    text-align: center;
    font-size: 24px;
    color: #333;
    margin: 20px 0 30px;
    font-weight: 600;
  }

  .cover-container {
    width: 70%;
    margin: 0 auto 30px;
    
    .cover-img {
      width: 100%;
      height: 300px;
      object-fit: cover;
      border-radius: 4px;
      border: 1px solid #eee;
    }
    
    .cover-error {
      height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f7fa;
      color: #909399;
    }
  }

  .content-container {
    font-size: 16px;
    line-height: 1.8;
    color: #444;

    ::v-deep(img) {
      max-width: 80%;
      margin: 20px auto;
      display: block;
    }
  }
}

.comment-section {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px;
}


.comment-section {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;

  .comment-title {
    font-size: 1.4em;
    color: #333;
    margin-bottom: 1.5em;
    padding-bottom: 0.5em;
    border-bottom: 1px solid #eee;
  }

  .comment-form {
    margin-bottom: 30px;

    .name-input {
      margin-bottom: 10px;
    }

    .content-input {
      margin-bottom: 10px;

      :deep(.el-textarea__inner) {
        resize: vertical;
      }
    }

    .action-bar {
      display: flex;
      justify-content: flex-end;
    }
  }

  .comment-list {
    .comment-item {
      padding: 15px;
      margin-bottom: 15px;
      background: white;
      border-radius: 4px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      .comment-header{
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        font-size: 0.9em;

        .user-name {
          color: #409eff;
          font-weight: 500;
        }

        .comment-date {
          color: #999;
        }
      }

      .comment-content {
        color: #555;
        line-height: 1.6;
        white-space: pre-wrap;
      }
    }

    .empty-comments {
      text-align: center;
      color: #999;
      padding: 20px;
    }
  }
}
</style>