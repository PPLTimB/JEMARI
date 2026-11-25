/**
 * Utilitas pemformatan angka ke Rupiah dan estimasi durasi hari untuk PBI-04.
 */

/**
 * Memformat nilai angka ke format Rupiah standar Indonesia.
 * Contoh: 50000 -> "Rp 50.000"
 *
 * @param {number|string|null|undefined} value Nilai nominal yang akan diformat
 * @returns {string} String terformat Rupiah atau "—" jika null/undefined
 */
export function formatRupiah(value) {
  if (value === null || value === undefined || value === '') {
    return '—'
  }

  const number = Number(value)
  if (Number.isNaN(number)) {
    return '—'
  }

  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(number)
}

/**
 * Memformat durasi dalam satuan hari.
 * Contoh: 3 -> "3 hari"
 *
 * @param {number|string|null|undefined} days Jumlah hari
 * @returns {string} String durasi terformat
 */
export function formatDurasi(days) {
  if (days === null || days === undefined || days === '') {
    return '—'
  }

  const num = Number(days)
  if (Number.isNaN(num) || num <= 0) {
    return '—'
  }

  return `${num} hari`
}

/**
 * Memformat rentang durasi pengerjaan (tercepat dan terlama) untuk PBI-04.
 * Sesuai proposal UC-03 & Mockup 4.5.12:
 * - Jika paket belum memiliki varian aktif / durasi kosong: "Belum ada estimasi"
 * - Jika durasi sama (mis. 3 dan 3): "3 hari" (memakai formatDurasi)
 * - Jika durasi berbeda (mis. 3 dan 7): "3–7 hari"
 *
 * @param {number|string|null|undefined} tercepat Durasi tercepat dalam hari
 * @param {number|string|null|undefined} terlama Durasi terlama dalam hari
 * @returns {string} String rentang estimasi
 */
export function formatRentangDurasi(tercepat, terlama) {
  const min =
    tercepat !== null && tercepat !== undefined && tercepat !== '' ? Number(tercepat) : null
  const max =
    terlama !== null && terlama !== undefined && terlama !== '' ? Number(terlama) : null

  const hasMin = min !== null && !Number.isNaN(min) && min > 0
  const hasMax = max !== null && !Number.isNaN(max) && max > 0

  if (!hasMin && !hasMax) {
    return 'Belum ada estimasi'
  }

  if (hasMin && hasMax) {
    if (min === max) {
      return formatDurasi(min)
    }
    return `${min}–${max} hari`
  }

  return formatDurasi(hasMin ? min : max)
}
