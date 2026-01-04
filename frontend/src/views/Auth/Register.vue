<template>
  <div class="register-container">
    <div class="register-content">
      <div class="brand-logo">
        <div class="logo-circle">
          <el-icon><EditPen /></el-icon>
        </div>
      </div>

      <div class="neu-card register-card">
        <div class="card-header">
          <h2>Create Account</h2>
          <p>Join Strove AI today</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="register-form">
          <el-form-item label="Username" prop="username">
            <el-input v-model="form.username" placeholder="Choose a username" class="neu-input" />
          </el-form-item>

          <el-form-item label="Email" prop="email">
            <el-input v-model="form.email" placeholder="Enter your email" class="neu-input" />
          </el-form-item>

          <el-form-item label="Password" prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="Create a password"
              show-password
              class="neu-input"
            />
          </el-form-item>

          <el-form-item label="Confirm Password" prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="Confirm your password"
              show-password
              class="neu-input"
            />
          </el-form-item>

          <div class="form-actions">
            <el-button class="neu-btn primary" @click="handleRegister" :loading="loading">
              Sign Up
            </el-button>
          </div>

          <div class="footer-links">
            <span>Already have an account?</span>
            <router-link to="/login">Sign In</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { EditPen } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('Passwords do not match'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: 'Please enter a username', trigger: 'blur' },
    { min: 3, max: 50, message: 'Username must be 3-50 characters', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter an email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter a password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm your password', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.register(form.username, form.email, form.password)
        ElMessage.success('Registration successful! Welcome to Strove.')
        router.push('/dashboard')
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || 'Registration failed')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: var(--bg-body);
  transition: background-color 0.5s ease;
  padding: 20px;
}

.register-content {
  width: 100%;
  max-width: 480px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
}

.brand-logo {
  display: flex;
  justify-content: center;
}

.logo-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--bg-card);
  box-shadow: var(--shadow-neu);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: var(--accent-color);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0px); }
}

.register-card {
  width: 100%;
  padding: 40px;
  background: var(--bg-card);
  border-radius: 30px;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-header h2 {
  margin: 0 0 8px 0;
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 700;
}

.card-header p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.register-form :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}

.neu-input :deep(.el-input__wrapper) {
  background-color: var(--bg-card);
  box-shadow: var(--shadow-neu-inset);
  border-radius: 12px;
  padding: 8px 16px;
}

.neu-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: var(--shadow-neu-inset), 0 0 0 1px var(--accent-color);
}

.form-actions {
  margin-top: 32px;
  margin-bottom: 24px;
}

.neu-btn.primary {
  width: 100%;
  height: 48px;
  font-size: 16px;
  color: var(--accent-color);
  font-weight: 700;
  letter-spacing: 0.5px;
}

.footer-links {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
}

.footer-links a {
  color: var(--accent-color);
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
}

.footer-links a:hover {
  text-decoration: underline;
}
</style>
