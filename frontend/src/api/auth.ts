import apiClient from './client'

export interface ApiResponse<T> {
    success: boolean
    message: string
    data: T
}

export interface RegisterRequest {
    username: string
    email: string
    password: string
}

export interface LoginRequest {
    loginName: string
    password: string
}

export interface AuthResponse {
    token: string
    username: string
    email: string
}

export const authApi = {
    register: (data: RegisterRequest) => {
        return apiClient.post<ApiResponse<AuthResponse>>('/api/auth/register', data)
    },

    login: (data: LoginRequest) => {
        return apiClient.post<ApiResponse<AuthResponse>>('/api/auth/login', data)
    },

    getCurrentUser: () => {
        return apiClient.get('/api/auth/me')
    }
}
