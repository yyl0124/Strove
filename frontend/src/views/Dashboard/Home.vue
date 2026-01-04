<template>
  <div class="dashboard-container">
    <el-container class="layout">
      <el-header class="header">
        <div class="header-content">
          <div class="logo">
            <div class="logo-icon-wrapper">
              <el-icon class="logo-icon"><EditPen /></el-icon>
            </div>
            <h1>Strove AI</h1>
          </div>
          
          <div class="header-actions">
            <!-- Theme Toggle -->
            <button class="neu-btn theme-toggle" @click="themeStore.toggleTheme" :title="themeStore.isDark ? 'Switch to Light' : 'Switch to Dark'">
              <el-icon v-if="themeStore.isDark"><Sunny /></el-icon>
              <el-icon v-else><Moon /></el-icon>
            </button>

            <div class="user-info">
              <div class="avatar-wrapper">
                <el-avatar :size="36" :icon="UserFilled" class="user-avatar" />
              </div>
              <span class="username">{{ authStore.username }}</span>
              <button class="neu-btn logout-btn" @click="handleLogout" title="Logout">
                <el-icon><SwitchButton /></el-icon>
              </button>
            </div>
          </div>
        </div>
      </el-header>

      <el-container class="main-container">
        <el-aside width="260px" class="sidebar">
          <el-menu :default-active="activeMenu" router class="el-menu-vertical">
            <el-menu-item index="/dashboard">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/documents">
              <el-icon><Document /></el-icon>
              <span>我的文档</span>
            </el-menu-item>
            <el-menu-item index="/chat">
              <el-icon><ChatDotRound /></el-icon>
              <span>AI对话</span>
            </el-menu-item>
            <el-menu-item index="/inspiration">
              <el-icon><MagicStick /></el-icon>
              <span>灵感生成</span>
            </el-menu-item>
          </el-menu>
          
          <!-- Sidebar Footer / Promo Area -->
          <div class="sidebar-promo">
            <div class="promo-card">
              <h4>Strove Pro</h4>
              <p>Unlock advanced AI features</p>
              <button class="neu-btn sm">Upgrade</button>
            </div>
          </div>
        </el-aside>

        <el-main class="main-content">
          <div class="welcome-section">
            <h2>欢迎回来，{{ authStore.username }} 👋</h2>
            <p>准备好开始创作了吗？Strove AI 随时为您提供灵感。</p>
          </div>

          <el-row :gutter="32" class="quick-actions">
            <el-col :span="8">
              <div class="action-card" @click="router.push('/documents')">
                <div class="icon-wrapper blue">
                  <el-icon><Document /></el-icon>
                </div>
                <h3>我的文档</h3>
                <p>继续编辑您的本地文档</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="action-card" @click="router.push('/chat')">
                <div class="icon-wrapper purple">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <h3>AI对话</h3>
                <p>获取写作灵感与建议</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="action-card" @click="router.push('/inspiration')">
                <div class="icon-wrapper green">
                  <el-icon><MagicStick /></el-icon>
                </div>
                <h3>灵感生成</h3>
                <p>输入关键词，一键获取创意</p>
              </div>
            </el-col>
          </el-row>
          
          <!-- Recent Activity / Stats Section (Placeholder for more content) -->
          <div class="stats-section">
             <div class="stat-card">
               <div class="stat-value">{{ stats.documents }}</div>
               <div class="stat-label">本地文档</div>
             </div>
             <div class="stat-card">
               <div class="stat-value">{{ stats.interactions }}</div>
               <div class="stat-label">对话次数</div>
             </div>
             <div class="stat-card">
               <div class="stat-value">{{ stats.savedTime }}</div>
               <div class="stat-label">节省时间</div>
             </div>
          </div>

        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { useThemeStore } from '../../stores/theme'
import { aiApi } from '../../api/ai'
import { 
  House, 
  Document, 
  ChatDotRound, 
  EditPen, 
  UserFilled, 
  SwitchButton,
  MagicStick,
  Sunny,
  Moon
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const activeMenu = computed(() => route.path)

// 统计数据
const stats = ref({
  documents: 0,
  interactions: 0,
  savedTime: '0h'
})

// 加载统计数据
const loadStats = async () => {
  // 1. 统计本地文档数量（从localStorage）
  let docCount = 0
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key && key.startsWith('doc_')) {
      docCount++
    }
  }
  stats.value.documents = docCount

  // 2. 获取对话次数（从API）
  try {
    const res = await aiApi.getHistory()
    if (res.data.success && Array.isArray(res.data.data)) {
      stats.value.interactions = res.data.data.length
      
      // 3. 估算节省时间（每次对话平均节省15分钟）
      const totalMinutes = stats.value.interactions * 15
      const hours = Math.floor(totalMinutes / 60)
      const minutes = totalMinutes % 60
      
      if (hours > 0) {
        stats.value.savedTime = minutes > 0 ? `${hours}h${minutes}m` : `${hours}h`
      } else {
        stats.value.savedTime = `${minutes}m`
      }
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

const handleLogout = () => {
  authStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background-color: var(--bg-body);
  transition: background-color 0.5s ease;
}

.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: var(--bg-body); /* Transparent/Body color for neumorphism */
  padding: 0 32px;
  height: 80px;
  display: flex;
  align-items: center;
  z-index: 10;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--bg-card);
  padding: 10px 24px;
  border-radius: 20px;
  box-shadow: var(--shadow-neu);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-primary);
}

.logo-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--bg-card);
  box-shadow: var(--shadow-neu);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-color);
}

.logo-icon {
  font-size: 20px;
}

.header-content h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.theme-toggle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: var(--text-secondary);
  font-size: 18px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-left: 20px;
  border-left: 1px solid var(--el-border-color-light); /* Optional separator */
}

.avatar-wrapper {
  border-radius: 50%;
  padding: 3px;
  background: var(--bg-card);
  box-shadow: var(--shadow-neu);
}

.username {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 600;
}

.logout-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  color: #ef4444;
}

.main-container {
  flex: 1;
  padding: 20px 32px 32px;
  gap: 32px;
  overflow: hidden;
}

.sidebar {
  background: transparent;
  border-right: none;
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 10px 20px;
}

.el-menu-vertical {
  background: var(--bg-card);
  border-radius: 24px;
  padding: 16px 8px;
  box-shadow: var(--shadow-neu);
  border: none;
}

.sidebar-promo {
  margin-top: auto;
  padding: 20px;
  background: var(--bg-card);
  border-radius: 24px;
  box-shadow: var(--shadow-neu);
  text-align: center;
}

.promo-card h4 {
  margin: 0 0 8px;
  color: var(--text-primary);
}

.promo-card p {
  margin: 0 0 16px;
  font-size: 12px;
  color: var(--text-secondary);
}

.promo-card .neu-btn {
  width: 100%;
  padding: 8px;
  font-size: 12px;
  font-weight: bold;
  color: var(--accent-color);
}

.main-content {
  padding: 0;
  overflow-y: auto;
  padding-bottom: 20px;
}

.welcome-section {
  margin-bottom: 40px;
  padding: 0 10px;
}

.welcome-section h2 {
  margin: 0 0 12px 0;
  font-size: 28px;
  color: var(--text-primary);
  font-weight: 800;
}

.welcome-section p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 16px;
}

.quick-actions {
  margin-bottom: 40px;
}

.action-card {
  background: var(--bg-card);
  border-radius: 24px;
  padding: 32px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-neu);
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  position: relative;
  overflow: hidden;
  margin: 0 35px;
}

.action-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-neu), 0 0 20px rgba(255, 255, 255, 0.2);
}

.action-card:active {
  box-shadow: var(--shadow-neu-pressed);
  transform: translateY(0);
}

.icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-neu-inset);
}

.icon-wrapper.blue { color: var(--blue-color); }
.icon-wrapper.purple { color: var(--purple-color); }
.icon-wrapper.green { color: var(--green-color); }

.action-card h3 {
  margin: 0 0 12px 0;
  font-size: 20px;
  color: var(--text-primary);
  font-weight: 700;
}

.action-card p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: 24px;
  padding: 24px;
  box-shadow: var(--shadow-neu);
  text-align: center;
  transition: all 0.3s ease;
  margin: 0 35px;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-value {
  font-size: 36px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
}
</style>
