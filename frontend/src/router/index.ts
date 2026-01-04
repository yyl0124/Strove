import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Auth/Login.vue')
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/Auth/Register.vue')
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('../views/Dashboard/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/documents',
    name: 'documents',
    component: () => import('../views/Documents/DocumentList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'chat',
    component: () => import('../views/Chat/ChatInterface.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/inspiration',
    name: 'inspiration',
    component: () => import('../views/Inspiration/InspirationGenerator.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
