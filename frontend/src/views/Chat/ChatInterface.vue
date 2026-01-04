<template>
  <div class="chat-layout">
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>历史记录</h3>
        <el-button circle size="small" @click="fetchHistory">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
      <div class="history-list">
        <div 
          v-for="item in history" 
          :key="item.id" 
          class="history-item"
          @click="loadConversation(item)"
        >
          <div class="history-content">
            <span class="history-title">{{ item.message.substring(0, 20) }}...</span>
            <span class="history-time">{{ formatDate(item.createdAt) }}</span>
          </div>
          <el-button 
            type="danger" 
            link 
            size="small" 
            @click.stop="deleteConversation(item.id)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
      <div class="sidebar-footer">
        <el-button type="primary" plain block @click="showSettings = true">
          <el-icon><Setting /></el-icon> 设置 API Key
        </el-button>
        <el-button type="info" plain block @click="router.push('/dashboard')" style="margin-top: 10px; margin-left: 0;">
          <el-icon><Back /></el-icon> 返回仪表盘
        </el-button>
      </div>
    </div>

    <div class="main-chat">
      <div class="chat-header">
        <h2>AI 写作助手</h2>
        <div class="header-actions">
          <el-button @click="clearChat" plain size="small">新对话</el-button>
        </div>
      </div>

      <div class="messages-container" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
          <el-empty description="开始一个新的创作对话吧" />
          <div class="quick-prompts">
            <el-tag 
              v-for="prompt in quickPrompts" 
              :key="prompt" 
              class="prompt-tag" 
              effect="plain" 
              round
              @click="usePrompt(prompt)"
            >
              {{ prompt }}
            </el-tag>
          </div>
        </div>
        
        <div v-for="(msg, index) in messages" :key="index" :class="['message-wrapper', msg.role]">
          <div class="message-avatar">
            <el-avatar :icon="msg.role === 'user' ? UserFilled : Service" :class="msg.role" />
          </div>
          <div class="message-content">
            <div class="message-bubble">
              <div v-if="msg.role === 'assistant'" v-html="formatMarkdown(msg.content)"></div>
              <div v-else>{{ msg.content }}</div>
            </div>
            <div class="message-actions" v-if="msg.role === 'assistant'">
              <el-button v-if="msg.isError && msg.originalInput" link size="small" type="warning" @click="retryMessage(msg.originalInput)">
                <el-icon><RefreshRight /></el-icon> 重试
              </el-button>
              <el-button v-else link size="small" @click="copyText(msg.content)">复制</el-button>
              <el-button v-if="!msg.isError" link size="small" @click="polishText(msg.content)">润色</el-button>
              <el-button v-if="!msg.isError" link size="small" type="primary" @click="openInEditor(msg, index)">
                <el-icon><Document /></el-icon> 在编辑器中打开
              </el-button>
            </div>
          </div>
        </div>
        
        <div v-if="loading" class="message-wrapper assistant">
          <div class="message-avatar">
            <el-avatar :icon="Service" class="assistant" />
          </div>
          <div class="message-content">
            <div class="message-bubble loading">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="输入您的写作需求..."
            resize="none"
            @keydown.enter.prevent="sendMessage"
          />
          <div class="input-bottom">
            <div class="input-tools">
              <el-select v-model="selectedStyle" placeholder="风格" size="small" style="width: 100px">
                <el-option label="默认" value="default" />
                <el-option label="正式" value="formal" />
                <el-option label="口语化" value="casual" />
                <el-option label="学术" value="academic" />
                <el-option label="创意" value="creative" />
                <el-option label="简洁" value="concise" />
                <el-option label="复杂" value="noconcise" />
              </el-select>
              <el-button size="small" @click="showTemplateDialog = true" text>
                <el-icon><Memo /></el-icon> 模板
              </el-button>
            </div>
            <div class="input-actions">
              <el-tag v-if="selectedStyle !== 'default'" size="small" closable @close="selectedStyle = 'default'" type="info">
                {{ getStyleLabel(selectedStyle) }}
              </el-tag>
              <el-button type="primary" @click="sendMessage" :loading="loading" circle>
                <el-icon><Position /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Settings Dialog -->
    <el-dialog v-model="showSettings" title="API 设置" width="400px">
      <el-form :model="settings" label-position="top">
        <el-form-item label="AI 服务商">
          <el-select v-model="settings.provider">
            <el-option label="OpenAI" value="openai" />
            <el-option label="通义千问" value="qianwen" disabled />
            <el-option label="文心一言" value="wenxin" disabled />
          </el-select>
        </el-form-item>
        <el-form-item label="API Key">
          <el-input v-model="settings.apiKey" type="password" show-password placeholder="sk-..." />
        </el-form-item>
        <el-form-item label="API URL">
          <el-input 
            v-model="settings.apiUrl" 
            placeholder="例如: https://api.openai.com/v1 或 https://your-proxy.com/v1" 
            clearable
          />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            留空则使用默认官方地址。填写基础URL即可（如 https://api.go-model.com/v1），系统会自动补全路径
          </div>
        </el-form-item>
        <el-form-item label="模型">
          <el-select 
            v-model="settings.model" 
            allow-create 
            filterable 
            default-first-option
            placeholder="选择或输入模型名称"
          >
            <el-option label="GPT-3.5 Turbo" value="gpt-3.5-turbo" />
            <el-option label="GPT-4" value="gpt-4" />
            <el-option label="GPT-4 Turbo" value="gpt-4-turbo" />
            <el-option label="GPT-4o" value="gpt-4o" />
            <el-option label="GPT-4o Mini" value="gpt-4o-mini" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <div class="footer-left">
            <el-button 
              type="info" 
              :loading="testing" 
              @click="testConnectionHandler"
              plain
            >
              {{ testing ? '测试中...' : '测试连接' }}
            </el-button>
            <span v-if="testResult" :style="{ color: testResult.success ? '#67C23A' : '#F56C6C', fontSize: '12px', marginLeft: '10px' }">
              {{ testResult.message }}
              <span v-if="testResult.responseTime" style="color: #909399; margin-left: 5px;">
                ({{ testResult.responseTime }})
              </span>
            </span>
          </div>
          <div class="footer-right">
            <el-button @click="showSettings = false">取消</el-button>
            <el-button type="primary" @click="saveSettings">保存</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 模板选择对话框 -->
    <el-dialog v-model="showTemplateDialog" title="选择模板" width="600px">
      <div class="templates-grid">
        <el-card 
          v-for="template in templates" 
          :key="template.name"
          class="template-card"
          shadow="hover"
          @click="useTemplate(template)"
        >
          <div class="template-header">
            <el-icon :size="24" color="#409eff"><component :is="template.icon" /></el-icon>
            <h4>{{ template.name }}</h4>
          </div>
          <p class="template-desc">{{ template.description }}</p>
          <el-tag size="small" type="info">{{template.category}}</el-tag>
        </el-card>
      </div>
    </el-dialog>

    <!-- 模板参数填写对话框 -->
    <el-dialog 
      v-model="showTemplateForm" 
      :title="currentTemplate ? `填写参数 - ${currentTemplate.name}` : '填写参数'" 
      width="500px"
    >
      <el-form :model="templateParams" label-width="100px">
        <el-form-item 
          v-for="param in currentTemplateParams" 
          :key="param.key"
          :label="param.label"
        >
          <el-input 
            v-model="templateParams[param.key]" 
            :placeholder="param.placeholder"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTemplateForm = false">取消</el-button>
        <el-button type="primary" @click="applyTemplate">应用模板</el-button>
      </template>
    </el-dialog>

    <!-- 文档编辑器 -->
    <DocumentEditor 
      v-if="showEditor"
      :document-id="editorDocId"
      :initial-content="editorContent"
      :initial-title="editorTitle"
      @close="showEditor = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Service, Position, Setting, Delete, Refresh, Back, Document, RefreshRight, Memo,
         EditPen, Reading, Notebook, Tickets, MagicStick, ChatLineRound } from '@element-plus/icons-vue'
import { aiApi, type Conversation } from '../../api/ai'
import dayjs from 'dayjs'
import { marked } from 'marked'
import DocumentEditor from '../Documents/DocumentEditor.vue'

const router = useRouter()
const messagesContainer = ref<HTMLElement>()
const inputMessage = ref('')
const loading = ref(false)
const showSettings = ref(false)
const history = ref<Conversation[]>([])
const testing = ref(false)
const testResult = ref<any>(null)
const showEditor = ref(false)
const editorContent = ref('')
const editorTitle = ref('')
const editorDocId = ref('')
const selectedStyle = ref('default')
const showTemplateDialog = ref(false)
const showTemplateForm = ref(false)
const currentTemplate = ref<any>(null)
const currentTemplateParams = ref<any[]>([])
const templateParams = ref<Record<string, string>>({})

const messages = ref<{ 
  id: string, 
  role: 'user' | 'assistant', 
  content: string,
  isError?: boolean,
  originalInput?: string // 保存导致错误的原始输入，用于重试
}[]>([])

const settings = reactive({
  provider: localStorage.getItem('ai_provider') || 'openai',
  apiKey: localStorage.getItem('ai_api_key') || '',
  model: localStorage.getItem('ai_model') || 'gpt-3.5-turbo',
  apiUrl: localStorage.getItem('ai_api_url') || ''
})

const quickPrompts = [
  '写一封关于生日的感谢邮件',
  '写一份关于SMM项目的项目总结',
  '写一篇关于创新的演讲稿',
  '写一篇读书笔记',
  '写一篇科普文章',
  '生成一个创意故事'
]

// 模板数据
const templates = [
  {
    name: '文章写作',
    description: '帮我写一篇关于[主题]的文章',
    category: '写作',
    icon: EditPen,
    prompt: '请帮我写一篇关于{topic}的{genre}文章。\n\n主题：{topic}\n文体：{genre}\n字数：约{wordCount}字\n关键词：{keywords}\n目标读者：{audience}\n\n要求：\n1. 结构清晰，有引言、正文、结论\n2. 内容充实，有理有据\n3. 语言流畅，逻辑严密\n4. 融入关键词，自然不生硬\n\n请直接给出文章内容。',
    params: [
      { key: 'topic', label: '文章主题', placeholder: '例如：人工智能的发展' },
      { key: 'genre', label: '文章文体', placeholder: '例如：议论文、说明文、散文' },
      { key: 'wordCount', label: '字数要求', placeholder: '例如：800' },
      { key: 'keywords', label: '关键词', placeholder: '例如：创新、技术、未来（用顿号分隔）' },
      { key: 'audience', label: '目标读者', placeholder: '例如：普通大众、专业人士、学生' }
    ]
  },
  {
    name: '邮件撰写',
    description: '快速生成各类商务邮件',
    category: '办公',
    icon: Tickets,
    prompt: '请帮我写一封{type}邮件。\n\n收件人：{recipient}\n邮件主题：{subject}\n语气：{tone}\n字数：约{wordCount}字\n背景说明：{background}\n\n要求：\n1. 称呼和结尾得体\n2. 简明扼要，重点突出\n3. 格式规范，语气{tone}\n\n请直接给出邮件正文（包含称呼和落款）。',
    params: [
      { key: 'type', label: '邮件类型', placeholder: '例如：感谢信、申请信、通知、邀约' },
      { key: 'recipient', label: '收件人', placeholder: '例如：张经理、李教授' },
      { key: 'subject', label: '邮件主题', placeholder: '例如：项目合作感谢' },
      { key: 'tone', label: '语气风格', placeholder: '例如：诚恳、正式、友好、热情' },
      { key: 'wordCount', label: '字数要求', placeholder: '例如：200' },
      { key: 'background', label: '背景说明', placeholder: '简要说明写邮件的背景和目的' }
    ]
  },
  {
    name: '总结报告',
    description: '生成工作总结或项目报告',
    category: '办公',
    icon: Notebook,
    prompt: '请帮我写一份{type}总结报告。\n\n报告类型：{type}\n时间范围：{timeRange}\n工作内容：{content}\n字数：约{wordCount}字\n重点内容：{focus}\n\n报告应包括：\n1. 工作概况\n2. 主要成果和数据\n3. 遇到的问题及解决方案\n4. 经验总结\n5. 下一步计划\n\n请直接给出报告内容。',
    params: [
      { key: 'type', label: '报告类型', placeholder: '例如：周报、月报、季度总结、项目总结' },
      { key: 'timeRange', label: '时间范围', placeholder: '例如：2024年1月、第一季度' },
      { key: 'content', label: '工作内容', placeholder: '简要列举主要工作内容' },
      { key: 'wordCount', label: '字数要求', placeholder: '例如：500' },
      { key: 'focus', label: '重点内容', placeholder: '需要重点突出的成果或亮点' }
    ]
  },
  {
    name: '创意故事',
    description: '激发创意，生成有趣的故事',
    category: '创作',
    icon: MagicStick,
    prompt: '请创作一个{genre}故事。\n\n故事类型：{genre}\n主题：{theme}\n字数：约{wordCount}字\n主要角色：{characters}\n故事背景：{setting}\n情节要素：{plotElements}\n\n要求：\n1. 情节引人入胜，有起承转合\n2. 人物形象鲜明，性格突出\n3. 语言生动有趣，富有画面感\n4. 结局{ending}\n\n请直接给出故事内容。',
    params: [
      { key: 'genre', label: '故事类型', placeholder: '例如：科幻、悬疑、爱情、奇幻、历史' },
      { key: 'theme', label: '故事主题', placeholder: '例如：时间旅行、寻找真相、命运抉择' },
      { key: 'wordCount', label: '字数要求', placeholder: '例如：800' },
      { key: 'characters', label: '主要角色', placeholder: '例如：一个好奇的科学家、神秘的陌生人' },
      { key: 'setting', label: '故事背景', placeholder: '例如：未来都市、古代宫廷、平行世界' },
      { key: 'plotElements', label: '情节要素', placeholder: '例如：意外发现、冒险历程、反转' },
      { key: 'ending', label: '结局类型', placeholder: '例如：开放式、圆满、悬念' }
    ]
  },
  {
    name: '演讲稿',
    description: '撰写各类演讲或发言稿',
    category: '办公',
    icon: ChatLineRound,
    prompt: '请帮我写一篇{occasion}演讲稿。\n\n场合：{occasion}\n主题：{topic}\n时长：约{duration}分钟（{wordCount}字）\n听众：{audience}\n演讲目的：{purpose}\n核心观点：{keyPoints}\n\n要求：\n1. 开场吸引人，结尾有力量\n2. 逻辑清晰，层次分明\n3. 语言{style}，富有感染力\n4. 适当运用修辞手法\n\n请直接给出演讲稿内容。',
    params: [
      { key: 'occasion', label: '演讲场合', placeholder: '例如：毕业典礼、年会、竞聘、TED演讲' },
      { key: 'topic', label: '演讲主题', placeholder: '例如：追逐梦想、团队协作、创新思维' },
      { key: 'duration', label: '演讲时长', placeholder: '例如：5' },
      { key: 'wordCount', label: '字数要求', placeholder: '例如：800' },
      { key: 'audience', label: '听众群体', placeholder: '例如：同学们、同事、客户' },
      { key: 'purpose', label: '演讲目的', placeholder: '例如：激励、说服、分享经验' },
      { key: 'keyPoints', label: '核心观点', placeholder: '列出2-3个核心观点' },
      { key: 'style', label: '语言风格', placeholder: '例如：激昂、温和、幽默' }
    ]
  },
  {
    name: '读书笔记',
    description: '整理读书心得和笔记',
    category: '学习',
    icon: Reading,
    prompt: '请帮我写一篇《{bookName}》的读书笔记。\n\n书名：《{bookName}》\n作者：{author}\n阅读目的：{purpose}\n字数：约{wordCount}字\n重点章节：{keyChapters}\n个人关注点：{focus}\n\n笔记应包括：\n1. 书籍基本信息和背景\n2. 核心观点总结\n3. 印象深刻的内容和金句\n4. 个人感悟和思考\n5. 实际应用建议\n\n请直接给出笔记内容。',
    params: [
      { key: 'bookName', label: '书名', placeholder: '例如：人类简史' },
      { key: 'author', label: '作者', placeholder: '例如：尤瓦尔·赫拉利' },
      { key: 'purpose', label: '阅读目的', placeholder: '例如：拓展视野、专业学习、兴趣阅读' },
      { key: 'wordCount', label: '字数要求', placeholder: '例如：600' },
      { key: 'keyChapters', label: '重点章节', placeholder: '例如：第三章、第五章（可选）' },
      { key: 'focus', label: '关注点', placeholder: '你特别关注或感兴趣的内容' }
    ]
  }
]

// 获取风格标签
const getStyleLabel = (style: string) => {
  const labels: Record<string, string> = {
    formal: '正式',
    casual: '口语化',
    academic: '学术',
    creative: '创意',
    concise: '简洁',
    noconcise:'复杂'
  }
  return labels[style] || style
}

// 选择模板 - 打开参数填写表单
const useTemplate = (template: typeof templates[0]) => {
  currentTemplate.value = template
  currentTemplateParams.value = template.params || []
  templateParams.value = {}
  showTemplateDialog.value = false
  showTemplateForm.value = true
}

// 应用模板 - 填充参数并放入输入框
const applyTemplate = () => {
  if (!currentTemplate.value) return
  
  let prompt = currentTemplate.value.prompt
  
  // 替换所有参数
  for (const [key, value] of Object.entries(templateParams.value)) {
    prompt = prompt.replace(new RegExp(`\\{${key}\\}`, 'g'), value)
  }
  
  inputMessage.value = prompt
  showTemplateForm.value = false
  ElMessage.success(`已应用模板：${currentTemplate.value.name}`)
  
  // 清空当前模板数据
  currentTemplate.value = null
  currentTemplateParams.value = []
  templateParams.value = {}
}

onMounted(() => {
  fetchHistory()
  if (!settings.apiKey) {
    ElMessage.warning('请先配置 API Key')
    showSettings.value = true
  }
})

const saveSettings = () => {
  localStorage.setItem('ai_provider', settings.provider)
  localStorage.setItem('ai_api_key', settings.apiKey)
  localStorage.setItem('ai_model', settings.model)
  localStorage.setItem('ai_api_url', settings.apiUrl)
  showSettings.value = false
  ElMessage.success('设置已保存')
}

const testConnectionHandler = async () => {
  // 验证必填项
  if (!settings.apiKey) {
    ElMessage.warning('请先输入 API Key')
    return
  }

  if (!settings.model) {
    ElMessage.warning('请先选择模型')
    return
  }

  testing.value = true
  testResult.value = null

  try {
    const res = await aiApi.testConnection(
      settings.apiKey,
      settings.provider,
      settings.model,
      settings.apiUrl || undefined
    )

    if (res.data.success && res.data.data) {
      testResult.value = {
        success: res.data.data.success,
        message: res.data.data.message,
        responseTime: res.data.data.responseTime
      }
      
      if (res.data.data.success) {
        ElMessage.success('连接测试成功！')
      } else {
        ElMessage.error(res.data.data.error || '连接失败')
      }
    } else {
      testResult.value = {
        success: false,
        message: res.data.message || '测试失败'
      }
      ElMessage.error(res.data.message || '测试失败')
    }
  } catch (error: any) {
    testResult.value = {
      success: false,
      message: '连接失败'
    }
    ElMessage.error(error.response?.data?.message || '测试失败，请检查配置')
  } finally {
    testing.value = false
  }
}


const fetchHistory = async () => {
  try {
    const res = await aiApi.getHistory()
    if (res.data.success) {
      history.value = res.data.data
    }
  } catch (error) {
    console.error('Failed to fetch history', error)
  }
}

const loadConversation = (item: Conversation) => {
  messages.value = [
    { id: `hist-${item.id}-user`, role: 'user', content: item.message },
    { id: `hist-${item.id}-assistant`, role: 'assistant', content: item.response }
  ]
}

const deleteConversation = async (id: number) => {
  try {
    await aiApi.deleteConversation(id)
    ElMessage.success('删除成功')
    fetchHistory()
    if (messages.value.length > 0) {
      clearChat()
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const clearChat = () => {
  messages.value = []
}

const usePrompt = (prompt: string) => {
  inputMessage.value = prompt
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  if (!settings.apiKey) {
    showSettings.value = true
    return
  }

  let userMsg = inputMessage.value
  
  // 应用风格设置（使用更强的提示词格式）
  if (selectedStyle.value !== 'default') {
    const styleInstructions: Record<string, string> = {
      formal: '\n\n【重要】请务必使用正式、严谨的语言风格来回答上述问题。',
      casual: '\n\n【重要】请务必使用口语化、轻松的语言风格来回答上述问题。',
      academic: '\n\n【重要】请务必使用学术性、专业的语言风格来回答上述问题。',
      creative: '\n\n【重要】请务必使用创意性、独特的语言风格来回答上述问题。',
      concise: '\n\n【重要】请务必使用简洁、精炼的语言风格来回答上述问题，避免冗余。',
      noconcise: '\n\n【重要】请务必使用复杂、冗长、详尽的语言风格来回答上述问题，充分展开论述。'
    }
    userMsg += styleInstructions[selectedStyle.value] || ''
  }

  messages.value.push({ 
    id: `msg-${Date.now()}-${Math.random()}`,
    role: 'user', 
    content: userMsg 
  })
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const res = await aiApi.chat({
      message: userMsg,
      apiKey: settings.apiKey,
      provider: settings.provider,
      model: settings.model,
      apiUrl: settings.apiUrl || undefined
    })


    if (res.data.success) {
      messages.value.push({ 
        id: `msg-${Date.now()}-${Math.random()}`,
        role: 'assistant', 
        content: res.data.data.reply 
      })
      fetchHistory() 
    } else {
      const errorMsg = res.data.message || 'AI 响应失败'
      ElMessage.error(errorMsg)
      console.error('AI响应失败:', res.data)
      messages.value.push({ 
        id: `msg-${Date.now()}-${Math.random()}`,
        role: 'assistant', 
        content: `抱歉，响应失败：${errorMsg}`,
        isError: true,
        originalInput: userMsg
      })
    }
  } catch (error: any) {
    const errorMsg = error.response?.data?.message || error.message || '发送失败，请检查 API Key 或网络'
    ElMessage.error(errorMsg)
    console.error('发送消息错误:', error)
    messages.value.push({ 
      id: `msg-${Date.now()}-${Math.random()}`,
      role: 'assistant', 
      content: `抱歉，发生错误：${errorMsg}`,
      isError: true,
      originalInput: userMsg
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const openInEditor = (msg: { id: string, role: string, content: string }, index?: number) => {
  // 将Markdown转换为HTML（如果需要）
  editorContent.value = formatMarkdown(msg.content) as string
  
  // 查找对应的用户提问作为标题
  let title = 'AI生成内容'
  if (index !== undefined && index > 0) {
    // 找到前一条用户消息
    const userMsg = messages.value[index - 1]
    if (userMsg && userMsg.role === 'user') {
      // 取前30个字作为标题
      title = userMsg.content.substring(0, 30) + (userMsg.content.length > 30 ? '...' : '')
    }
  }
  
  editorTitle.value = `${title} - ${new Date().toLocaleDateString()}`
  editorDocId.value = msg.id // 传递消息ID
  showEditor.value = true
}


const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const retryMessage = (originalInput: string) => {
  // 使用保存的原始输入重新发送
  inputMessage.value = originalInput
  sendMessage()
}

const copyText = (text: string) => {
  navigator.clipboard.writeText(text)
  ElMessage.success('已复制')
}

const polishText = (text: string) => {
  inputMessage.value = `请帮我润色以下内容：\n${text}`
}

const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('MM-DD HH:mm')
}

const formatMarkdown = (text: string) => {
  return marked.parse(text)
}
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100vh;
  background-color: var(--bg-light);
}

.sidebar {
  width: 260px;
  background-color: var(--bg-white);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  color: var(--text-dark);
}

.history-list {
  flex: 1;
  overflow-y: auto;
}

.history-item {
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  transition: background-color 0.2s;
}

.history-item:hover {
  background-color: var(--bg-lighter);
}

.history-content {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.history-title {
  font-size: 14px;
  color: var(--text-regular);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-time {
  font-size: 12px;
  color: var(--text-light);
  margin-top: 4px;
}

.sidebar-footer {
  margin-top: auto;
  padding-top: 20px;
}

.main-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.chat-header {
  height: 60px;
  background-color: var(--bg-white);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.chat-header h2 {
  margin: 0;
  font-size: 20px;
  color: var(--text-dark);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-wrapper {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message-wrapper.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-wrapper.assistant {
  align-self: flex-start;
}

.message-avatar {
  margin-top: 4px;
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  position: relative;
}

.user .message-bubble {
  background-color: var(--accent-color);
  color: var(--bg-white);
  border-bottom-right-radius: 4px;
}

.assistant .message-bubble {
  background-color: var(--bg-white);
  color: var(--text-dark);
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.message-bubble.loading {
  background-color: var(--bg-light);
  padding: 16px 20px;
}

.message-actions {
  margin-top: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.message-wrapper:hover .message-actions {
  opacity: 1;
}

.input-area {
  padding: 20px;
  background-color: var(--bg-white);
  border-top: 1px solid var(--border-color);
}

.input-wrapper {
  position: relative;
  max-width: 800px;
  margin: 0 auto;
  background: var(--bg-white);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  overflow: hidden;
  transition: all 0.3s;
}

.input-wrapper:focus-within {
  border-color: var(--accent-color);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.input-wrapper :deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  padding: 15px;
  font-size: 14px;
}

.input-wrapper :deep(.el-textarea__inner):focus {
  box-shadow: none;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-light);
}

.quick-prompts {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.prompt-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.prompt-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--text-light);
  margin: 0 2px;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.footer-left {
  display: flex;
  align-items: center;
}

.footer-right {
  display: flex;
  gap: 10px;
}

/* 输入框底部工具栏 */
.input-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: var(--bg-light);
  border-top: 1px solid var(--border-color);
}

.input-tools {
  display: flex;
  gap: 8px;
  align-items: center;
}

.input-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* 模板网格 */
.templates-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  max-height: 500px;
  overflow-y: auto;
}

.template-card {
  cursor: pointer;
  transition: all 0.3s;
}

.template-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.2);
}

.template-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.template-header h4 {
  margin: 0;
  font-size: 16px;
  color: var(--text-dark);
}

.template-desc {
  color: var(--text-light);
  font-size: 13px;
  margin: 8px 0;
  line-height: 1.5;
}

/* Markdown样式 */
.message-bubble :deep(pre) {
  background-color: var(--bg-lighter);
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 0.5em 0;
}

.message-bubble :deep(code) {
  background-color: var(--bg-lighter);
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.message-bubble :deep(p) {
  margin: 0.5em 0;
}

.message-bubble :deep(ul),
.message-bubble :deep(ol) {
  margin: 0.5em 0;
  padding-left: 1.5em;
}

.message-bubble :deep(h1),
.message-bubble :deep(h2),
.message-bubble :deep(h3),
.message-bubble :deep(h4),
.message-bubble :deep(h5),
.message-bubble :deep(h6) {
  margin: 0.8em 0 0.5em;
}

.message-bubble :deep(blockquote) {
  border-left: 4px solid var(--accent-color);
  padding-left: 1em;
  margin: 0.5em 0;
  color: var(--text-secondary);
}
</style>
