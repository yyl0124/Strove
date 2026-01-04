<template>
  <div class="editor-container">
    <!-- 编辑器头部 -->
    <div class="editor-header">
      <div class="header-left">
        <el-input 
          v-model="documentTitle" 
          placeholder="未命名文档"
          class="title-input"
          @blur="saveDocument"
        />
        <span class="save-status">{{ saveStatus }}</span>
      </div>
      <div class="header-right">
        <el-button @click="showRewriteDialog = true" :icon="Refresh">全文重写</el-button>
        <el-button @click="polishSelected" :icon="MagicStick" :disabled="!hasSelection">润色选中</el-button>
        <el-dropdown @command="exportDocument">
          <el-button :icon="Download">
            导出 <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="txt">导出为 TXT</el-dropdown-item>
              <el-dropdown-item command="md">导出为 Markdown</el-dropdown-item>
              <el-dropdown-item command="html">导出为 HTML</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button @click="closeEditor" circle :icon="Close" />
      </div>
    </div>

    <!-- 编辑器主体 -->
    <div class="editor-body">
      <div class="editor-toolbar">
        <el-button-group>
          <el-button size="small" @click="formatText('bold')"><b>B</b></el-button>
          <el-button size="small" @click="formatText('italic')"><i>I</i></el-button>
          <el-button size="small" @click="formatText('underline')"><u>U</u></el-button>
        </el-button-group>
        <el-divider direction="vertical" />
        <el-button-group>
          <el-button size="small" @click="formatText('h1')">H1</el-button>
          <el-button size="small" @click="formatText('h2')">H2</el-button>
          <el-button size="small" @click="formatText('h3')">H3</el-button>
        </el-button-group>
        <el-divider direction="vertical" />
        <el-button-group>
          <el-button size="small" @click="formatText('ul')">• 列表</el-button>
          <el-button size="small" @click="formatText('ol')">1. 列表</el-button>
        </el-button-group>
      </div>
      
      <div 
        ref="editorContent"
        class="editor-content" 
        contenteditable="true"
        @input="handleInput"
        @mouseup="checkSelection"
        @keyup="checkSelection"
        v-html="content"
      ></div>
    </div>

    <!-- 全文重写对话框 -->
    <el-dialog v-model="showRewriteDialog" title="全文重写" width="500px">
      <el-input
        v-model="rewriteRequirement"
        type="textarea"
        :rows="4"
        placeholder="请输入重写要求，例如：使文章更专业、改为口语化风格、增加案例等"
      />
      <template #footer>
        <el-button @click="showRewriteDialog = false">取消</el-button>
        <el-button type="primary" @click="rewriteContent" :loading="rewriting">重写</el-button>
      </template>
    </el-dialog>

    <!-- 润色对话框 -->
    <el-dialog 
      v-model="showPolishDialog" 
      title="润色选中文本" 
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div v-if="!polishing">
        <div class="selected-text-preview">
          <div class="preview-label">选中的文本：</div>
          <div class="preview-content">{{ selectedText }}</div>
        </div>
        <el-input
          v-model="polishRequirement"
          type="textarea"
          :rows="3"
          placeholder="请输入润色要求，例如：更流畅、更专业、更简洁、口语化等"
        />
      </div>
      <div v-else class="polishing-state">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>AI正在润色中，请稍候...</p>
        <el-progress :percentage="100" :indeterminate="true" />
      </div>
      <template #footer>
        <el-button v-if="!polishing" @click="showPolishDialog = false">取消</el-button>
        <el-button v-if="!polishing" type="primary" @click="confirmPolish">开始润色</el-button>
        <el-button v-else type="danger" @click="cancelPolish">强制退出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, Refresh, MagicStick, Download, ArrowDown, Loading } from '@element-plus/icons-vue'
import { aiApi } from '../../api/ai'

const props = defineProps<{
  documentId: string
  initialContent: string
  initialTitle?: string
}>()

const emit = defineEmits(['close'])

const editorContent = ref<HTMLElement>()
// 优先从localStorage恢复内容，如果没有则使用initialContent
const getSavedContent = () => {
  const savedDoc = localStorage.getItem(`doc_${props.documentId}`)
  if (savedDoc) {
    try {
      const doc = JSON.parse(savedDoc)
      return doc.content
    } catch (e) {
      return props.initialContent
    }
  }
  return props.initialContent
}

const getSavedTitle = () => {
  const savedDoc = localStorage.getItem(`doc_${props.documentId}`)
  if (savedDoc) {
    try {
      const doc = JSON.parse(savedDoc)
      return doc.title
    } catch (e) {
      return props.initialTitle || '未命名文档'
    }
  }
  return props.initialTitle || '未命名文档'
}

const content = ref(getSavedContent())
const documentTitle = ref(getSavedTitle())
const saveStatus = ref('已保存')
const hasSelection = ref(false)
const selectedText = ref('')
const showRewriteDialog = ref(false)
const rewriteRequirement = ref('')
const rewriting = ref(false)
const showPolishDialog = ref(false)
const polishRequirement = ref('')
const polishing = ref(false)

let saveTimer: NodeJS.Timeout | null = null
let savedRange: Range | null = null  // 保存选区，用于润色后恢复

// 处理输入
const handleInput = (e: Event) => {
  const target = e.target as HTMLElement
  // 直接从DOM读取内容，不要设置content.value，避免触发v-html更新导致光标跳转
  saveStatus.value = '未保存'
  
  // 防抖保存
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    // 保存时才更新content.value
    content.value = target.innerHTML
    saveDocument()
  }, 2000)
}

// 保存文档
const saveDocument = () => {
  // 从DOM读取最新内容
  if (editorContent.value) {
    content.value = editorContent.value.innerHTML
  }
  
  // 使用documentId作为key保存到 localStorage
  const doc = {
    title: documentTitle.value,
    content: content.value,
    updatedAt: new Date().toISOString()
  }
  localStorage.setItem(`doc_${props.documentId}`, JSON.stringify(doc))
  saveStatus.value = '已保存 ' + new Date().toLocaleTimeString()
}

// 检查选中文本
const checkSelection = () => {
  const selection = window.getSelection()
  if (selection && selection.toString().trim().length > 0) {
    hasSelection.value = true
    selectedText.value = selection.toString()
  } else {
    hasSelection.value = false
    selectedText.value = ''
  }
}

// 格式化文本
const formatText = (command: string) => {
  document.execCommand(command, false)
  editorContent.value?.focus()
}

// 打开润色对话框
const polishSelected = () => {
  if (!selectedText.value) {
    ElMessage.warning('请先选中要润色的文本')
    return
  }
  
  // 保存当前选区
  const selection = window.getSelection()
  if (selection && selection.rangeCount > 0) {
    savedRange = selection.getRangeAt(0).cloneRange()
  }
  
  polishRequirement.value = ''
  showPolishDialog.value = true
}

// 确认润色
const confirmPolish = async () => {
  if (!polishRequirement.value.trim()) {
    ElMessage.warning('请输入润色要求')
    return
  }

  const apiKey = localStorage.getItem('ai_api_key')
  const provider = localStorage.getItem('ai_provider') || 'openai'
  const model = localStorage.getItem('ai_model') || 'gpt-3.5-turbo'
  const apiUrl = localStorage.getItem('ai_api_url') || undefined

  if (!apiKey) {
    ElMessage.warning('请先配置 API Key')
    return
  }

  polishing.value = true
  
  try {
    const res = await aiApi.polishText(
      selectedText.value,
      apiKey,
      provider,
      model,
      polishRequirement.value,
      apiUrl
    )

    if (res.data.success) {
      // 恢复选区并替换文本
      if (savedRange && editorContent.value) {
        try {
          // 恢复选区
          const selection = window.getSelection()
          if (selection) {
            selection.removeAllRanges()
            selection.addRange(savedRange)
            
            // 删除选中内容并插入新文本
            savedRange.deleteContents()
            const textNode = document.createTextNode(res.data.data)
            savedRange.insertNode(textNode)
            
            // 将光标移到插入文本的末尾
            savedRange.setStartAfter(textNode)
            savedRange.collapse(true)
            selection.removeAllRanges()
            selection.addRange(savedRange)
            
            ElMessage.success('润色完成')
            saveDocument()
            showPolishDialog.value = false
            savedRange = null
          }
        } catch (e) {
          console.error('替换文本失败:', e)
          ElMessage.error('替换文本失败，请重试')
        }
      } else {
        ElMessage.error('选区已丢失，请重新选择')
      }
    } else {
      ElMessage.error(res.data.message || '润色失败')
    }
  } catch (error: any) {
    console.error('润色失败:', error)
    ElMessage.error(error.response?.data?.message || '润色失败，请重试')
  } finally {
    polishing.value = false
  }
}

// 取消润色
const cancelPolish = () => {
  polishing.value = false
  showPolishDialog.value = false
  ElMessage.info('已取消润色')
}

// 全文重写
const rewriteContent = async () => {
  if (!rewriteRequirement.value.trim()) {
    ElMessage.warning('请输入重写要求')
    return
  }

  const apiKey = localStorage.getItem('ai_api_key')
  const provider = localStorage.getItem('ai_provider') || 'openai'
  const model = localStorage.getItem('ai_model') || 'gpt-3.5-turbo'
  const apiUrl = localStorage.getItem('ai_api_url') || undefined

  if (!apiKey) {
    ElMessage.warning('请先配置 API Key')
    return
  }

  rewriting.value = true
  try {
    const prompt = `请根据以下要求重写这段文本：\n\n要求：${rewriteRequirement.value}\n\n原文：\n${content.value.replace(/<[^>]*>/g, '')}`
    
    const res = await aiApi.chat({
      message: prompt,
      apiKey,
      provider,
      model,
      apiUrl
    })

    if (res.data.success) {
      content.value = res.data.data.reply
      if (editorContent.value) {
        editorContent.value.innerHTML = content.value
      }
      showRewriteDialog.value = false
      rewriteRequirement.value = ''
      ElMessage.success('重写完成')
      saveDocument()
    }
  } catch (error) {
    ElMessage.error('重写失败，请重试')
  } finally {
    rewriting.value = false
  }
}

// 导出文档
const exportDocument = (format: string) => {
  // 从DOM读取最新内容
  if (editorContent.value) {
    content.value = editorContent.value.innerHTML
  }
  
  const plainText = content.value.replace(/<[^>]*>/g, '')
  const fileName = documentTitle.value || '未命名文档'
  
  let blob: Blob
  let mimeType: string
  let extension: string

  switch (format) {
    case 'txt':
      blob = new Blob([plainText], { type: 'text/plain;charset=utf-8' })
      extension = 'txt'
      break
    case 'md':
      // 简单的HTML到Markdown转换
      let markdown = content.value
        .replace(/<h1>/g, '# ')
        .replace(/<\/h1>/g, '\n\n')
        .replace(/<h2>/g, '## ')
        .replace(/<\/h2>/g, '\n\n')
        .replace(/<h3>/g, '### ')
        .replace(/<\/h3>/g, '\n\n')
        .replace(/<p>/g, '')
        .replace(/<\/p>/g, '\n\n')
        .replace(/<br>/g, '\n')
        .replace(/<strong>|<b>/g, '**')
        .replace(/<\/strong>|<\/b>/g, '**')
        .replace(/<em>|<i>/g, '*')
        .replace(/<\/em>|<\/i>/g, '*')
        .replace(/<[^>]*>/g, '')
      blob = new Blob([markdown], { type: 'text/markdown;charset=utf-8' })
      extension = 'md'
      break
    case 'html':
      const html = `<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>${fileName}</title>
  <style>
    body { font-family: Arial, sans-serif; max-width: 800px; margin: 40px auto; padding: 20px; line-height: 1.6; }
  </style>
</head>
<body>
  <h1>${fileName}</h1>
  ${content.value}
</body>
</html>`
      blob = new Blob([html], { type: 'text/html;charset=utf-8' })
      extension = 'html'
      break
    default:
      return
  }

  // 创建下载链接
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${fileName}.${extension}`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  
  ElMessage.success(`已导出为 ${extension.toUpperCase()} 格式`)
}

// 关闭编辑器
const closeEditor = () => {
  if (saveStatus.value === '未保存') {
    saveDocument()
  }
  emit('close')
}

onMounted(() => {
  if (editorContent.value) {
    editorContent.value.innerHTML = content.value
    editorContent.value.focus()
  }
})

onBeforeUnmount(() => {
  if (saveTimer) clearTimeout(saveTimer)
  if (saveStatus.value === '未保存') {
    saveDocument()
  }
})
</script>

<style scoped>
.editor-container {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 50%;
  background: var(--bg-white);
  z-index: 2000;
  display: flex;
  flex-direction: column;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.15);
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: var(--bg-white);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
}

.title-input {
  max-width: 400px;
}

.title-input :deep(.el-input__wrapper) {
  font-size: 18px;
  font-weight: 600;
  box-shadow: none;
  border-bottom: 2px solid transparent;
  transition: border-color 0.3s;
}

.title-input :deep(.el-input__wrapper:hover),
.title-input :deep(.el-input__wrapper.is-focus) {
  border-bottom-color: #409eff;
}

.save-status {
  font-size: 12px;
  color: #909399;
}

.header-right {
  display: flex;
  gap: 10px;
}

.editor-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.editor-content {
  flex: 1;
  padding: 40px;
  overflow-y: auto;
  font-size: 16px;
  line-height: 1.8;
  outline: none;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
}

.editor-content:focus {
  outline: none;
}

.editor-content :deep(h1) {
  font-size: 32px;
  margin: 20px 0;
}

.editor-content :deep(h2) {
  font-size: 24px;
  margin: 18px 0;
}

.editor-content :deep(h3) {
  font-size: 20px;
  margin: 16px 0;
}

.editor-content :deep(p) {
  margin: 12px 0;
}

.editor-content :deep(ul),
.editor-content :deep(ol) {
  padding-left: 24px;
  margin: 12px 0;
}

/* 润色对话框样式 */
.selected-text-preview {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

.preview-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.preview-content {
  font-size: 14px;
  color: var(--text-dark);
  line-height: 1.6;
  max-height: 120px;
  overflow-y: auto;
}

.polishing-state {
  text-align: center;
  padding: 30px 20px;
}

.polishing-state p {
  font-size: 16px;
  color: var(--text-light);
  margin: 20px 0;
}

.polishing-state .el-progress {
  margin-top: 20px;
}
</style>
