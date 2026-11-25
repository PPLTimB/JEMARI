const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

function getAuthHeaders() {
  const token = localStorage.getItem('jemari_token') || ''

  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }
}

async function handleResponse(response) {
  const result = await response.json().catch(() => null)

  if (!response.ok) {
    const errorMessage =
      result?.message || `Terjadi kesalahan pada server (HTTP ${response.status})`

    const error = new Error(errorMessage)
    error.status = response.status
    error.errors = result?.errors || []

    throw error
  }

  return result
}

export const umkmProfileService = {
  async getMyProfile() {
    const response = await fetch(`${API_BASE_URL}/umkm-profiles/me`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })

    return handleResponse(response)
  },

  async createMyProfile(payload) {
    const response = await fetch(`${API_BASE_URL}/umkm-profiles`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload),
    })

    return handleResponse(response)
  },

  async updateMyProfile(payload) {
    const response = await fetch(`${API_BASE_URL}/umkm-profiles/me`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload),
    })

    return handleResponse(response)
  },

  async deactivateMyProfile() {
    const response = await fetch(`${API_BASE_URL}/umkm-profiles/me`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    })

    return handleResponse(response)
  },

  async uploadLogo(file) {
    const formData = new FormData()
    formData.append('file', file)

    const token = localStorage.getItem('jemari_token') || ''

    const response = await fetch(`${API_BASE_URL}/umkm-profiles/me/logo`, {
      method: 'POST',
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: formData,
    })

    return handleResponse(response)
  },

  async deleteLogo() {
    const token = localStorage.getItem('jemari_token') || ''

    const response = await fetch(`${API_BASE_URL}/umkm-profiles/me/logo`, {
      method: 'DELETE',
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    })

    return handleResponse(response)
  },
}