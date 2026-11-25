/**
 * Service untuk Freelancer Experiences (PBI-11)
 * CRUD pengalaman kerja freelancer yang tampil di profil publik
 */

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
    const errorMessage = result?.message || `Terjadi kesalahan pada server (HTTP ${response.status})`
    const error = new Error(errorMessage)
    error.status = response.status
    error.errors = result?.errors || []
    throw error
  }
  return result
}

export const freelancerExperienceService = {
  // GET /api/freelancer-profiles/me/experiences
  async getMyExperiences() {
    const response = await fetch(`${API_BASE_URL}/freelancer-profiles/me/experiences`, {
      method: 'GET',
      headers: getAuthHeaders(),
    })
    return handleResponse(response)
  },

  // POST /api/freelancer-profiles/me/experiences
  async createExperience(payload) {
    const response = await fetch(`${API_BASE_URL}/freelancer-profiles/me/experiences`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload),
    })
    return handleResponse(response)
  },

  // PUT /api/freelancer-profiles/me/experiences/{experienceId}
  async updateExperience(experienceId, payload) {
    const response = await fetch(`${API_BASE_URL}/freelancer-profiles/me/experiences/${experienceId}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload),
    })
    return handleResponse(response)
  },

  // DELETE /api/freelancer-profiles/me/experiences/{experienceId}
  async deleteExperience(experienceId) {
    const response = await fetch(`${API_BASE_URL}/freelancer-profiles/me/experiences/${experienceId}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    })
    return handleResponse(response)
  },
}