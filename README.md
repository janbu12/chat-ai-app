# ChatAI App

[![Lisensi MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Status Build](https://img.shields.io/badge/Build-Passing-brightgreen)](https://github.com/yourusername/ChatAIApp/actions) 

Sebuah aplikasi chat Android modern yang terhubung dengan backend AI menggunakan n8n, memungkinkan pengguna untuk berinteraksi dengan AI, mendaftar, dan login dengan sistem otentikasi berbasis JWT.

## Daftar Isi

* [Tentang Proyek](#tentang-proyek)
* [Fitur Utama](#fitur-utama)
* [Teknologi Digunakan](#teknologi-digunakan)
* [Prasyarat](#prasyarat)
* [Instalasi & Pengaturan](#instalasi--pengaturan)
    * [Backend (n8n & MongoDB Atlas)](#backend-n8n--mongodb-atlas)
    * [Frontend (Aplikasi Android)](#frontend-aplikasi-android)
* [Penggunaan Aplikasi](#penggunaan-aplikasi)
* [Struktur API](#struktur-api)
* [Lisensi](#lisensi)

---

## Tentang Proyek

ChatAI Aplikasi Chat AI ini menandai debut saya dalam pengembangan mobile, sebuah proyek di mana saya memprioritaskan keamanan yang kokoh dan operasi backend yang efisien. Untuk autentikasi pengguna, saya mengimplementasikan sistem berbasis JWT (JSON Web Token), memastikan akses aman melalui hashing kata sandi dan penambahan garam. Backend memanfaatkan N8N Workflow API, alat otomatisasi tanpa kode yang kuat, yang mempermudah pemrosesan data. Semua data pengguna dan riwayat obrolan disimpan secara aman di MongoDB, database NoSQL yang fleksibel dan skalabel. Untuk permintaan jaringan dan parsing JSON dalam aplikasi mobile, saya mengintegrasikan perpustakaan OkHttp dan GSON, memastikan penanganan data yang efisien dan andal.


## Fitur Utama

* **Sistem Otentikasi Pengguna:**
    * Pendaftaran Akun Baru (`/api/register`).
    * Login Pengguna (`/api/login`) dengan token JWT.
    * Logout Pengguna (`/api/logout`) dengan mekanisme *token blacklisting* (karena tidak ada expire token).
* **Interaksi Chat AI:**
    * Mengirim pesan teks ke layanan AI (`/api/chatAI`).
    * Menerima respons dari AI dan menampilkannya dalam format bubble chat yang intuitif.
    * Pemrosesan Markdown sederhana (`**bold**`, `* bullet`) untuk tampilan respons AI yang lebih baik.
* **Manajemen Sesi:** Aplikasi mengenali status login pengguna dan tidak memerlukan login ulang setiap kali dibuka (selama token valid).
* **UI/UX Responsif:**
    * Indikator loading visual pada tombol dan `RecyclerView`.
    * Penyesuaian tata letak otomatis saat keyboard muncul.
    * Desain antarmuka chat yang bersih dan modern.
* **Penanganan API Robust:** Menggunakan OkHttp untuk permintaan jaringan yang efisien dan GSON untuk parsing JSON, dengan penanganan error yang komprehensif.

## Teknologi Digunakan

**Frontend (Aplikasi Android):**
* **Bahasa:** Java
* **SDK:** Android SDK (API Level 24+ kompatibel)
* **UI Framework:** AndroidX, Material Design Components
* **Networking:** [OkHttp](https://square.github.io/okhttp/)
* **JSON Parsing:** [Gson](https://github.com/google/gson)
* **JWT Decoding (Client-side):** [java-jwt (Auth0)](https://github.com/auth0/java-jwt)
* **Manajemen UI:** `RecyclerView`, `LinearLayout`, `RelativeLayout`
* **Network Security (Development):** Network Security Configuration, `UnsafeOkHttpClient` (HANYA UNTUK DEVELOPMENT)

**Backend (n8n Workflows):**
* **Platform:** [n8n](https://n8n.io/) (Self-hosted atau Cloud)
* **Database:** [MongoDB Atlas](https://cloud.mongodb.com/) (Free Tier Cluster)
* **Logic:** Node `Code` (Python/JavaScript) untuk hashing password (bcrypt), JWT signing/verification, token blacklisting.
* **Authentication:** JWT Auth bawaan n8n untuk verifikasi endpoint.
* **Tunneling (Development):** [ngrok](https://ngrok.com/)

## Prasyarat

Sebelum Anda memulai, pastikan Anda memiliki prasyarat berikut:

**Umum:**
* Koneksi Internet Aktif.

**Untuk Backend (n8n & MongoDB Atlas):**
* Akun [MongoDB Atlas](https://cloud.mongodb.com/) (Daftar untuk Free Tier).
* Instance n8n yang berjalan (Self-hosted melalui Docker/Node.js atau akun n8n Cloud).
* Akun [ngrok](https://ngrok.com/) (jika menggunakan ngrok-free.app untuk public access).

**Untuk Frontend (Aplikasi Android):**
* [Android Studio](https://developer.android.com/studio) terinstal.
* Java Development Kit (JDK 8 atau lebih tinggi) terinstal.
* Emulator Android atau perangkat Android fisik (dengan Android 7.0 / API Level 24 atau lebih tinggi).

## Instalasi & Pengaturan

### Backend (n8n & MongoDB Atlas)

1.  **Siapkan MongoDB Atlas:**
    * Buat Cluster MongoDB Atlas (Shared Cluster/Free Tier).
    * Buat Database User baru dengan hak akses yang sesuai (misalnya, `readWriteAnyDatabase` untuk pengembangan).
    * Konfigurasi **Network Access** untuk mengizinkan koneksi dari **IP publik server n8n Anda** atau `0.0.0.0/0` (untuk pengembangan, **TIDAK DIREKOMENDASIKAN UNTUK PRODUKSI**).
    * Dapatkan **Connection String** MongoDB Atlas Anda.

2.  **Siapkan Workflow n8n:**
    * Impor workflow n8n yang sudah Anda buat (login, register, chatAI, logout).
    * **Credentials:**
        * Buat Credential **MongoDB** baru menggunakan Connection String Atlas Anda.
        * Buat Credential **Generic Credential** baru untuk **Secret Key JWT** Anda. Pastikan secret key-nya panjang, acak, dan aman.
    * **Node `WebHook`:**
        * Pastikan `Path` dikonfigurasi dengan benar (misalnya, `/api` atau kosong jika menggunakan `if` routing).
        * Untuk endpoint terlindungi (`/chatAI`, `/logout`), atur `Authentication` ke `JWT Auth` dan hubungkan ke Credential Secret Key JWT Anda.
    * **Node `Code` (Hashing Password):** Pastikan menggunakan `bcrypt.hashpw` untuk register dan `bcrypt.checkpw` untuk login.
    * **Node `Code` (Blacklisting):** Siapkan koleksi `blacklisted_tokens` di MongoDB Anda. Node logout akan menyisipkan token yang di-logout ke sini.
    * **Aktifkan Workflow:** Pastikan workflow n8n Anda dalam status **Active**.
    * **Dapatkan URL WebHook:** Salin URL WebHook dari node WebHook (`Production URL` jika ada). Ini akan menjadi `N8N_BASE_URL` Anda di aplikasi Android.

3.  **Tunneling dengan ngrok (Jika Diperlukan):**
    * Jika n8n Anda berjalan secara lokal, gunakan ngrok untuk mengeksposnya ke internet.
    * Jalankan `ngrok http 5678` (jika n8n berjalan di port 5678).
    * Gunakan URL HTTPS yang diberikan ngrok (`https://xxxxxx.ngrok-free.app`) sebagai bagian dari `N8N_BASE_URL` Anda.

### Frontend (Aplikasi Android)

1.  **Clone Repositori:**
    ```bash
    git clone [https://github.com/yourusername/ChatAIApp.git](https://github.com/yourusername/ChatAIApp.git) cd ChatAIApp
    ```
2.  **Buka di Android Studio:** Buka proyek yang di-clone di Android Studio.
3.  **Tambahkan Dependencies:** Pastikan semua dependencies di `app/build.gradle` sudah ada dan tersinkronisasi:
    ```gradle
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'com.auth0.android:jwtdecode:2.0.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.0' 
    implementation 'androidx.appcompat:appcompat:1.6.1' 
    implementation 'com.google.android.material:material:1.12.0' 
    ```
4.  **Konfigurasi URL Backend:**
    * Buka `app/src/main/java/com/example/aichatapi/activities/MainActivity.java`.
    * Ganti nilai `N8N_BASE_URL` dengan URL WebHook n8n Anda yang sebenarnya (termasuk path API, contoh: `https://your-ngrok-url/webhook/workflow_id/api`).
    * Lakukan hal yang sama di `LoginActivity.java` dan `RegisterActivity.java`.
5.  **Network Security Configuration (Untuk Debugging):**
    * Jika Anda mengalami masalah SSL/TLS (sertifikat tidak ditemukan), Anda bisa menggunakan `UnsafeOkHttpClient` (sudah disediakan di proyek ini).
    * Pastikan `AndroidManifest.xml` tidak memiliki `android:networkSecurityConfig` yang berlebihan jika menggunakan `UnsafeOkHttpClient`.
    * Pastikan `app/src/main/res/drawable/rounded_edittext_bg.xml`, `send_button_circle_bg.xml`, `chat_bubble_ai_bg.xml`, `chat_bubble_user_bg.xml` sudah ada.
    * Pastikan `app/src/main/res/color/button_background_color.xml` sudah ada.
    * Pastikan `ic_send.xml` (Vector Asset) sudah ditambahkan di `drawable`.
6.  **Build & Run:**
    * Bersihkan proyek (`Build` > `Clean Project`).
    * Bangun ulang proyek (`Build` > `Rebuild Project`).
    * Jalankan aplikasi di emulator atau perangkat fisik Anda.

## Penggunaan Aplikasi

1.  **Daftar:**
    * Saat aplikasi pertama kali dibuka, Anda akan diarahkan ke `LoginActivity`.
    * Klik tombol/link "Daftar" atau navigasi ke `RegisterActivity`.
    * Isi `Username`, `Nama Lengkap`, `Password`, dan `Konfirmasi Password`.
    * Klik "Daftar". Jika berhasil, Anda akan diarahkan kembali ke `LoginActivity`.
2.  **Login:**
    * Di `LoginActivity`, masukkan `Username` dan `Password` yang sudah Anda daftarkan.
    * Klik "Login".
    * Jika berhasil, Anda akan diarahkan ke `MainActivity` (layar chat). Username Anda akan ditampilkan di kiri atas.
3.  **Chat dengan AI:**
    * Ketik pesan Anda di `EditText` di bagian bawah layar.
    * Klik ikon "Kirim" (panah pesawat).
    * Respons dari AI akan muncul di jendela chat.
4.  **Logout:**
    * Klik tombol "Logout" di kanan atas layar.
    * Anda akan di-logout dan diarahkan kembali ke `LoginActivity`.

## Struktur API

Aplikasi berinteraksi dengan backend n8n Anda melalui endpoint berikut:

* **`POST /api/register`**
    * **Deskripsi:** Mendaftarkan pengguna baru.
    * **Request Body:**
        ```json
        {
            "username": "string",
            "name": "string",
            "password": "string",
            "confirmPassword": "string"
        }
        ```
    * **Response (Sukses):** HTTP Status `200 OK` (tanpa body).
    * **Response (Gagal):** HTTP Status `4xx` (misal `400 Bad Request`, `409 Conflict`), Body:
        ```json
        {
            "success": false,
            "message": "string"
        }
        ```
* **`POST /api/login`**
    * **Deskripsi:** Mengautentikasi pengguna dan mengembalikan token akses.
    * **Request Body:**
        ```json
        {
            "username": "string",
            "password": "string"
        }
        ```
    * **Response (Sukses):** HTTP Status `200 OK`, Body:
        ```json
        {
            "success": true,
            "message": "Login successful",
            "user": { "id": "string", "username": "string", "name": "string" },
            "access_token": "string" }
        ```
    * **Response (Gagal):** HTTP Status `401 Unauthorized` atau `404 Not Found`, Body:
        ```json
        {
            "success": false,
            "message": "Invalid username or password"
        }
        ```
* **`POST /api/chatAI`**
    * **Deskripsi:** Mengirim pesan ke model AI dan mendapatkan respons.
    * **Authentication:** `Authorization: Bearer <access_token>` (di header).
    * **Request Body:**
        ```json
        {
            "prompt": "string"
        }
        ```
    * **Response (Sukses):** HTTP Status `200 OK`, Body:
        ```json
        [
            {
                "output": "string (respons teks AI)"
            }
        ]
        ```
    * **Response (Gagal):** HTTP Status `401 Unauthorized` (token tidak valid/expired), `4xx`/`5xx` lainnya, Body:
        ```json
        {
            "success": false,
            "message": "string"
        }
        ```
* **`POST /api/logout`**
    * **Deskripsi:** Mengeluarkan pengguna dari sesi dengan mem-blacklist token akses mereka.
    * **Authentication:** `Authorization: Bearer <access_token>` (di header).
    * **Request Body:** (Tidak ada body atau body kosong `{}`).
    * **Response (Sukses):** HTTP Status `200 OK`, Body:
        ```json
        {
            "success": true,
            "message": "Logged out successfully"
        }
        ```
    * **Response (Gagal):** HTTP Status `401 Unauthorized` (token tidak valid/expired), `4xx`/`5xx` lainnya, Body:
        ```json
        {
            "success": false,
            "message": "string"
        }
        ```

## Lisensi

Proyek ini dilisensikan di bawah [Lisensi MIT](https://opensource.org/licenses/MIT).
