<template>
  <div class="inspiration-container">
    <div class="header">
      <el-button @click="router.push('/dashboard')" :icon="Back" circle />
      <h2>灵感生成器</h2>
    </div>

    <div class="content">
      <el-card class="input-card">
        <template #header>
          <div class="card-header">
            <span>输入关键词</span>
          </div>
        </template>
        <el-input
          v-model="keyword"
          placeholder="例如：春日游记、科幻小说开头、产品发布会演讲"
          @keyup.enter="generate"
        >
          <template #append>
            <el-button @click="generate" :loading="loading" type="primary">生成灵感</el-button>
          </template>
        </el-input>
        
        <!-- 快速示例 -->
        <div class="quick-examples">
          <span class="examples-label">试试这些：</span>
          <el-tag 
            v-for="example in examples" 
            :key="example" 
            @click="keyword = example; generate()"
            class="example-tag"
            effect="plain"
          >
            {{ example }}
          </el-tag>
        </div>
      </el-card>

      <el-card v-if="results.length > 0" class="results-card">
        <template #header>
          <div class="results-header">
            <span>💡 生成结果</span>
            <el-tag>{{ results.length }} 条灵感</el-tag>
          </div>
        </template>
        
        <div class="results-list">
          <div 
            v-for="(item, index) in results" 
            :key="index"
            class="result-item"
          >
            <div class="result-header">
              <span class="result-number">{{ index + 1 }}</span>
              <div class="result-actions">
                <el-button size="small" @click="copy(item)">
                  <el-icon><DocumentCopy /></el-icon> 复制
                </el-button>
                <el-button size="small" type="primary" @click="useInChat(item)">
                  <el-icon><ChatDotRound /></el-icon> 去对话
                </el-button>
              </div>
            </div>
            <div class="result-text">{{ item }}</div>
          </div>
        </div>
      </el-card>
      
      <div v-else-if="!loading" class="empty-state">
        <el-empty description="">
          <template #image>
            <el-icon :size="80" color="#909399">
              <Opportunity />
            </el-icon>
          </template>
          <template #description>
            <h3>输入关键词，获取写作灵感</h3>
            <p>AI将为您生成多条创意想法</p>
          </template>
        </el-empty>
      </div>
      
      <div v-if="loading" class="loading-state">
        <div class="loading-text">🤔 AI正在思考中...</div>
        <el-skeleton :rows="3" animated />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, DocumentCopy, ChatDotRound, Opportunity } from '@element-plus/icons-vue'
import { aiApi } from '../../api/ai'

const router = useRouter()
const keyword = ref('')
const loading = ref(false)
const results = ref<string[]>([])

// 快速示例
const examples = [
  '春日游记',
  '科幻小说开头',
  '产品发布会演讲',
  '年终工作总结',
  '创业计划书',
  '情感散文'
]

const generate = async () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入关键词')
    return
  }
  
  const apiKey = localStorage.getItem('ai_api_key')
  if (!apiKey) {
    ElMessage.warning('请先在 AI对话 页面配置 API Key')
    router.push('/chat')
    return
  }

  loading.value = true
  results.value = []

  try {
    console.log('开始生成灵感，关键词:', keyword.value)
    const res = await aiApi.generateInspiration(
      keyword.value, 
      apiKey, 
      localStorage.getItem('ai_provider') || 'openai',
      localStorage.getItem('ai_model') || 'gpt-3.5-turbo',
      localStorage.getItem('ai_api_url') || undefined
    )
    
    console.log('API响应:', res.data)
    
    if (res.data.success) {
      const data = res.data.data
      console.log('生成的灵感数据:', data)
      
      // 确保data是数组
      if (Array.isArray(data)) {
        results.value = data.filter(item => item && item.trim())
        if (results.value.length === 0) {
          ElMessage.warning('未生成有效的灵感，请更换关键词重试')
        } else {
          ElMessage.success(`成功生成 ${results.value.length} 条灵感`)
        }
      } else if (typeof data === 'string') {
        // 如果返回的是字符串，尝试按行分割
        results.value = data.split('\n').filter(item => item && item.trim().length > 10)
        ElMessage.success(`成功生成 ${results.value.length} 条灵感`)
      } else {
        console.error('返回数据格式错误:', data)
        ElMessage.error('数据格式错误')
      }
    } else {
      ElMessage.error(res.data.message || '生成失败')
    }
  } catch (error: any) {
    console.error('生成灵感失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '生成失败，请检查 API Key 或网络'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

const copy = (text: string) => {
  navigator.clipboard.writeText(text)
  ElMessage.success('已复制')
}

const useInChat = (text: string) => {
  navigator.clipboard.writeText(text)
  ElMessage.success('已复制，请在对话框粘贴')
  router.push('/chat')
}
</script>

<style scoped>
.inspiration-container {
  min-height: 100vh;
  background-color: var(--bg-light);
  padding: 20px;
}

.header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  color: var(--text-dark);
}

.content {
  max-width: 800px;
  margin: 0 auto;
}

.input-card {
  margin-bottom: 30px;
}

.quick-examples {
  margin-top: 15px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.examples-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-right: 5px;
}

.example-tag {
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-light);
}

.example-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.empty-state h3 {
  margin: 15px 0 8px;
  color: var(--text-dark);
  font-size: 18px;
}

.empty-state p {
  color: var(--text-secondary);
  font-size: 14px;
}

.results-card {
  margin-top: 30px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.results-header span {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-dark);
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  padding: 16px;
  border-radius: 8px;
  background: var(--bg-white);
  transition: all 0.3s;
  color: var(--text-regular);
  cursor: pointer;
}

.result-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: var(--bg-white);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.result-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: var(--bg-light);
  color: var(--text-dark);
  border-radius: 50%;
  font-size: 13px;
  font-weight: 600;
}

.result-text {
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-dark);
}

.result-actions {
  display: flex;
  gap: 8px;
}

.loading-state {
  margin-top: 30px;
  background: var(--bg-white);
  padding: 20px;
  border-radius: 4px;
}

.loading-text {
  font-size: 16px;
  color: var(--text-dark);
  margin-bottom: 15px;
  text-align: center;
}
</style>
