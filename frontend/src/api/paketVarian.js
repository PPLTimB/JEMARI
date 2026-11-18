/**
 * Modul pemanggilan API backend untuk PBI-04 (Varian Paket & Rincian Biaya).
 */

import { httpPaketRequest } from './httpPaket'

/**
 * [UMKM - UC-03, FR-02]
 * Mengambil rincian biaya paket jasa (harga mulai, ringkasan estimasi durasi, dan daftar varian aktif).
 *
 * @param {number|string} paketId ID paket jasa
 * @returns {Promise<{
 *   packageId: number,
 *   hargaMulai: number|null,
 *   ringkasanEstimasi: { durasiTercepat: number, durasiTerlama: number }|null,
 *   varian: Array<{ id: number, name: string, description: string, price: number, estimatedDays: number }>
 * }>}
 */
export async function fetchRincianBiayaUmkm(paketId) {
  return httpPaketRequest(`/api/paket/${paketId}/rincian-biaya`, {
    method: 'GET',
  })
}

/**
 * [Freelancer - UC-18]
 * Mengambil daftar seluruh varian aktif pada paket milik freelancer.
 *
 * @param {number|string} paketId ID paket jasa
 * @returns {Promise<Array<{
 *   id: number,
 *   packageId: number,
 *   name: string,
 *   description: string,
 *   price: number,
 *   estimatedDays: number,
 *   active: boolean,
 *   createdAt: string,
 *   updatedAt: string
 * }>>}
 */
export async function fetchVarianFreelancer(paketId) {
  return httpPaketRequest(`/api/freelancer/paket/${paketId}/varian`, {
    method: 'GET',
  })
}

/**
 * [Freelancer - UC-18]
 * Menambahkan varian harga dan durasi baru pada paket.
 *
 * @param {number|string} paketId ID paket jasa
 * @param {{
 *   name: string,
 *   description?: string,
 *   price: number,
 *   estimatedDays: number
 * }} payload Data varian baru
 */
export async function createVarianFreelancer(paketId, payload) {
  return httpPaketRequest(`/api/freelancer/paket/${paketId}/varian`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

/**
 * [Freelancer - UC-18]
 * Memperbarui varian harga dan durasi pada paket.
 *
 * @param {number|string} paketId ID paket jasa
 * @param {number|string} varianId ID varian yang akan diperbarui
 * @param {{
 *   name: string,
 *   description?: string,
 *   price: number,
 *   estimatedDays: number
 * }} payload Data pembaruan varian
 */
export async function updateVarianFreelancer(paketId, varianId, payload) {
  return httpPaketRequest(`/api/freelancer/paket/${paketId}/varian/${varianId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

/**
 * [Freelancer - UC-18]
 * Menghapus varian secara soft-delete.
 *
 * @param {number|string} paketId ID paket jasa
 * @param {number|string} varianId ID varian yang akan dihapus
 */
export async function deleteVarianFreelancer(paketId, varianId) {
  return httpPaketRequest(`/api/freelancer/paket/${paketId}/varian/${varianId}`, {
    method: 'DELETE',
  })
}
