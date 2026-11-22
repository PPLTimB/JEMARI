# JEMARI
**Jembatan Ekonomi Mandiri Rakyat Indonesia** — Platform web penghubung UMKM/usaha *homemade* dengan freelancer penyedia jasa di Indonesia yang mendukung pencapaian **SDG 1 (No Poverty)**.

Dikembangkan oleh **Tim B — Proyek Perangkat Lunak (PPL)**.

---

## Fitur Utama

### Sprint 1 (Fondasi & Manajemen Profil/Etalase)
* **Autentikasi & Otorisasi Multi-Peran:** Registrasi dan login berbasis JWT untuk peran `UMKM`, `FREELANCER`, dan `ADMIN`.
* **Manajemen Profil Ganda:** Pengaturan profil profesional freelancer (`freelancer_profiles`) serta profil usaha UMKM.
* **Etalase Paket Jasa & Portofolio:** Pembuatan paket layanan terstruktur oleh freelancer dan etalase karya portofolio digital.
* **Katalog Jasa & Publikasi Request:** Penelusuran katalog layanan dan pembuatan permintaan kebutuhan jasa (*job request*) oleh UMKM.

### Sprint 2 (Kolaborasi Proyek, Escrow & Tata Kelola)
* **Discovery, Klarifikasi & Bidding:** Penelusuran request terbuka, pertanyaan klarifikasi, pengajuan lamaran (*bidding*), serta fitur *shortlist* & pemilihan kandidat oleh UMKM.
* **Eksekusi Proyek & Revisi:** Pemantauan progres pekerjaan, unggah berkas hasil kerja (*deliverables*), serta pengajuan revisi sesuai kuota/SLA.
* **Pemesanan Katalog, Simulasi Escrow & Dompet Virtual:** Pemesanan langsung dari katalog, simulasi pembayaran Escrow (**DP 50% & Pelunasan 50%**), hingga pencairan dana ke saldo virtual freelancer.
* **Readiness Score & Ulasan:** Penilaian kesiapan digital UMKM serta sistem rating dan ulasan pasca-proyek.
* **Monitoring Admin:** Aktivasi/nonaktif akun pengguna dan pemantauan transaksi escrow secara *read-only*.

---

## Arsitektur & Tech Stack

Proyek ini menggunakan arsitektur **Modular Monolith** dalam satu *monorepo*:

| Lapisan | Teknologi |
| :--- | :--- |
| **Backend** | Java 21 LTS, Spring Boot 3.3.5, Maven Wrapper (`mvnw`), Spring Security + JWT (JJWT 0.12.6), Spring Data JPA, Bean Validation |
| **Frontend** | Vue 3 (`<script setup>`), Vite, JavaScript, Pinia, Vue Router, Axios, `vite-plugin-pwa` |
| **Database** | MySQL 8.4.3 (`utf8mb4`), Flyway Database Migration |
| **E2E Testing** | Python 3, Pytest, Selenium WebDriver |

---
