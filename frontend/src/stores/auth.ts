import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type AuthResponse } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(localStorage.getItem('token'))
    const username = ref<string | null>(localStorage.getItem('username'))
    const email = ref<string | null>(localStorage.getItem('email'))

    const isAuthenticated = computed(() => !!token.value)

    const setAuth = (authData: AuthResponse) => {
        token.value = authData.token
        username.value = authData.username
        email.value = authData.email

        localStorage.setItem('token', authData.token)
        localStorage.setItem('username', authData.username)
        localStorage.setItem('email', authData.email)
    }

    const clearAuth = () => {
        token.value = null
        username.value = null
        email.value = null

        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('email')
    }

    const login = async (loginName: string, password: string) => {
        const response = await authApi.login({ loginName, password })

        // Check if the backend returned a success response
        if (response.data && response.data.success && response.data.data) {
            setAuth(response.data.data)
            return response.data.data
        } else {
            // Backend returned an error
            const errorMessage = response.data?.message || 'Login failed'
            throw new Error(errorMessage)
        }
    }

    const register = async (username: string, email: string, password: string) => {
        const response = await authApi.register({ username, email, password })

        if (response.data && response.data.success && response.data.data) {
            setAuth(response.data.data)
            return response.data.data
        } else {
            const errorMessage = response.data?.message || 'Registration failed'
            throw new Error(errorMessage)
        }
    }

    const logout = () => {
        clearAuth()
    }

    return {
        token,
        username,
        email,
        isAuthenticated,
        login,
        register,
        logout,
        setAuth,
        clearAuth
    }
})
