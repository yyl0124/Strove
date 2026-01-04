<template>
  <div class="documents-container">
    <div class="header">
      <div class="header-left">
        <el-button @click="goBack" :icon="Back" circle />
        <h2>我的文档</h2>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文档..."
          :prefix-icon="Search"
          class="search-input"
          clearable
        />
      </div>
    </div>

    <div class="content">
      <div v-if="filteredDocuments.length === 0" class="empty-state">
        <el-empty description="暂无文档">
          <template #image>
            <el-icon :size="80" color="#909399">
              <Document />
            </el-icon>
          </template>
          <el-button type="primary" @click="goToChat">开始创作</el-button>
        </el-empty>
      </div>

      <div v-else class="documents-grid">
        <el-card
          v-for="doc in filteredDocuments"
          :key="doc.id"
          class="document-card"
          shadow="hover"
          @click="openDocument(doc)"
        >
          <div class="card-header">
            <h3 class="doc-title">{{ doc.title }}</h3>
            <el-dropdown @command="(cmd) => handleDocAction(cmd, doc)">
              <el-button :icon="MoreFilled" circle text />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><EditPen /></el-icon> 编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="export">
                    <el-icon><Download /></el-icon> 导出
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon> 删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="doc-preview" v-html="getPreview(doc.content)"></div>
          <div class="doc-footer">
            <span class="doc-time">{{ formatTime(doc.updatedAt) }}</span>
            <span class="doc-words">{{ getWordCount(doc.content) }} 字</span>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 文档编辑器 -->
    <DocumentEditor 
      v-if="showEditor"
      :document-id="currentDocId"
      :initial-content="currentDocContent"
      :initial-title="currentDocTitle"
      @close="closeEditor"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, Search, Document, MoreFilled, EditPen, Download, Delete } from '@element-plus/icons-vue'
import DocumentEditor from './DocumentEditor.vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const searchKeyword = ref('')
const documents = ref<any[]>([])
const showEditor = ref(false)
const currentDocId = ref('')
const currentDocContent = ref('')
const currentDocTitle = ref('')

// 加载所有保存的文档
const loadDocuments = () => {
  const docs: any[] = []
  
  // 遍历localStorage，找出所有以 doc_ 开头的项
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key && key.startsWith('doc_')) {
      try {
        const docData = localStorage.getItem(key)
        if (docData) {
          const doc = JSON.parse(docData)
          docs.push({
            id: key.replace('doc_', ''),
            ...doc
          })
        }
      } catch (e) {
        console.error('Failed to parse document:', key, e)
      }
    }
  }
  
  // 按更新时间倒序排序
  docs.sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
  documents.value = docs
}

// 过滤文档
const filteredDocuments = computed(() => {
  if (!searchKeyword.value) {
    return documents.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return documents.value.filter(doc => 
    doc.title.toLowerCase().includes(keyword) ||
    doc.content.toLowerCase().includes(keyword)
  )
})

// 获取预览文本
const getPreview = (content: string) => {
  // 移除HTML标签，获取纯文本
  const text = content.replace(/<[^>]*>/g, '')
  return text.substring(0, 150) + (text.length > 150 ? '...' : '')
}

// 获取字数
const getWordCount = (content: string) => {
  const text = content.replace(/<[^>]*>/g, '')
  return text.length
}

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

// 打开文档
const openDocument = (doc: any) => {
  currentDocId.value = doc.id
  currentDocContent.value = doc.content
  currentDocTitle.value = doc.title
  showEditor.value = true
}

// 关闭编辑器
const closeEditor = () => {
  showEditor.value = false
  // 重新加载文档列表（可能有更新）
  loadDocuments()
}

// 处理文档操作
const handleDocAction = (command: string, doc: any) => {
  switch (command) {
    case 'edit':
      openDocument(doc)
      break
    case 'export':
      exportDocument(doc)
      break
    case 'delete':
      deleteDocument(doc)
      break
  }
}

// 导出文档
const exportDocument = (doc: any) => {
  const plainText = doc.content.replace(/<[^>]*>/g, '')
  const blob = new Blob([plainText], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${doc.title}.txt`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// 删除文档
const deleteDocument = (doc: any) => {
  ElMessageBox.confirm(
    `确定要删除文档"${doc.title}"吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    localStorage.removeItem(`doc_${doc.id}`)
    loadDocuments()
    ElMessage.success('删除成功')
  }).catch(() => {
    // 用户取消
  })
}

// 返回
const goBack = () => {
  router.push('/dashboard')
}

// 去聊天
const goToChat = () => {
  router.push('/chat')
}

onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.documents-container {
  min-height: 100vh;
  background-color: var(--bg-light);
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: var(--bg-white);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-left h2 {
  margin: 0;
  color: var(--text-dark);
}

.header-right {
  display: flex;
  gap: 10px;
}

.search-input {
  width: 300px;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
}

.empty-state {
  background: var(--bg-white);
  padding: 60px 20px;
  border-radius: 8px;
  text-align: center;
}

.documents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.document-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 240px;
  display: flex;
  flex-direction: column;
}

.document-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.doc-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-dark);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.doc-preview {
  flex: 1;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-light);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  margin-bottom: 12px;
}

.doc-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.doc-time,
.doc-words {
  font-size: 12px;
  color: #909399;
}
</style>
