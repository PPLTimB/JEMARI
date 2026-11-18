/**
 * HTTP Client berbasis fetch khusus untuk modul PBI-04.
 * Dibuat tanpa dependency Axios untuk menghindari konflik package.json dengan branch lain.
 */

export const STORAGE_TOKEN_KEY = 'jemari_auth_token'

/**
 * Mendapatkan token autentikasi dari localStorage.
 * @returns {string|null}
 */
export function getPaketToken() {
  try {
    return localStorage.getItem(STORAGE_TOKEN_KEY)
  } catch {
    return null
  }
}

/**
 * Menyimpan token autentikasi ke localStorage untuk pengujian.
 * @param {string} token
 */
export function setPaketToken(token) {
  try {
    if (token) {
      localStorage.setItem(STORAGE_TOKEN_KEY, token.trim())
    } else {
      localStorage.removeItem(STORAGE_TOKEN_KEY)
    }
  } catch (err) {
    console.error('Gagal menyimpan token ke localStorage:', err)
  }
}

/**
 * Menghapus token dari localStorage.
 */
export function clearPaketToken() {
  try {
    localStorage.removeItem(STORAGE_TOKEN_KEY)
  } catch (err) {
    console.error('Gagal menghapus token dari localStorage:', err)
  }
}

/**
 * Mendapatkan base URL API backend.
 * Mengutamakan variabel lingkungan VITE_API_BASE_URL, default ke '' (relative/proxy) atau http://localhost:8080.
 * @returns {string}
 */
export function getApiBaseUrl() {
  const envUrl = import.meta.env.VITE_API_BASE_URL
  if (envUrl && typeof envUrl === 'string') {
    return envUrl.replace(/\/+$/, '')
  }
  return 'http://localhost:8080'
}

/**
 * Mengirim request HTTP ke backend dengan format ApiResponse standar JEMARI.
 *
 * @param {string} endpoint Path endpoint (mis. '/api/paket/1/rincian-biaya')
 * @param {RequestInit} [options={}] Opsi fetch bawaan
 * @returns {Promise<any>} Data hasil response (ApiResponse.data)
 * @throws {Error & { status: number, errors?: Array<{field: string, message: string}> }}
 */
export async function httpPaketRequest(endpoint, options = {}) {
  const baseUrl = getApiBaseUrl()
  const url = `${baseUrl}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`

  const headers = new Headers(options.headers || {})
  headers.set('Accept', 'application/json')

  // Hanya set Content-Type jika bukan FormData dan ada body
  if (options.body && !(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  // Sisipkan token jika tersedia
  const token = getPaketToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const fetchOptions = {
    ...options,
    headers,
  }

  let response
  try {
    response = await fetch(url, fetchOptions)
  } catch (netErr) {
    const error = new Error(
      'Gagal terhubung ke server backend. Periksa koneksi internet atau status server.',
    )
    error.status = 0
    error.originalError = netErr
    throw error
  }

  // Parse JSON response body jika ada
  let json = null
  const contentType = response.headers.get('content-type')
  if (contentType && contentType.includes('application/json')) {
    try {
      json = await response.json()
    } catch {
      json = null
    }
  }

  if (!response.ok) {
    const message = json?.message || `Permintaan gagal dengan status ${response.status}`
    const error = new Error(message)
    error.status = response.status
    error.errors = json?.errors || null
    error.data = json?.data || null

    if (response.status === 401) {
      error.message =
        'Sesi Anda telah berakhir atau token belum disetel. Silakan pasang token yang valid.'
    } else if (response.status === 403) {
      error.message =
        json?.message ||
        'Anda tidak memiliki hak akses untuk membuka atau mengelola sumber daya ini.'
    } else if (response.status === 404) {
      error.message = json?.message || 'Data tidak ditemukan atau paket tidak tersedia.'
    }

    throw error
  }

  // Jika response berupa ApiResponse standar ({ success: true, message: "...", data: ... })
  if (json && typeof json === 'object' && 'data' in json) {
    return json.data
  }

  return json
}
