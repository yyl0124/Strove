import apiClient from './client'

export interface ChatRequest {
    message: string
    apiKey: string
    provider: string
    model: string
    apiUrl?: string
}

export interface ChatResponse {
    reply: string
    conversationId: number
}

export interface Conversation {
    id: number
    userId: number
    message: string
    response: string
    createdAt: string
}

export const aiApi = {
    chat: (data: ChatRequest) => {
        return apiClient.post<{ success: boolean, data: ChatResponse }>('/api/ai/chat', data)
    },

    getHistory: (limit: number = 20) => {
        return apiClient.get<{ success: boolean, data: Conversation[] }>(`/api/ai/conversations?limit=${limit}`)
    },

    deleteConversation: (id: number) => {
        return apiClient.delete<{ success: boolean, message: string }>(`/api/ai/conversations/${id}`)
    },

    generateInspiration: (keyword: string, apiKey: string, provider: string, model: string, apiUrl?: string) => {
        return apiClient.post<{ success: boolean, data: string[] }>('/api/ai/inspiration', {
            keyword,
            apiKey,
            provider,
            model,
            apiUrl
        })
    },


    polishText: (text: string, apiKey: string, provider: string, model: string, style: string, apiUrl?: string) => {
        return apiClient.post<{ success: boolean, data: string }>('/api/ai/polish', {
            text,
            apiKey,
            provider,
            model,
            style,
            apiUrl
        })
    },

    testConnection: (apiKey: string, provider: string, model: string, apiUrl?: string) => {
        return apiClient.post<{ success: boolean, data: any, message?: string }>('/api/ai/test-connection', {
            apiKey,
            provider,
            model,
            apiUrl
        })
    }
}
