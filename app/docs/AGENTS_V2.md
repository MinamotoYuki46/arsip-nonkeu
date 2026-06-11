# AGENTS.md — Manajemen Arsip BPKPAD Balangan
> **Source of Truth** for all AI agents (Gemini in Android Studio, Copilot, etc.)
> Read this file FULLY before generating or modifying any code.

---

## 0. Golden Rules (Never Break These)

1. **Offline-First always.** Every input (Manual, OCR, Excel) MUST go to Room DB (`temp_documents`) first with status `local_only`. Never write directly to Supabase from a form.
2. **Modular by feature.** Code lives in its feature package. Never put business logic in a Composable.
3. **JSONB is dynamic.** Never hardcode document-type fields in the Room schema. Use a `metadata: String` (JSON string) column and deserialize at the ViewModel layer.
4. **Soft delete only.** Never call `DELETE` on cloud tables. Always set `deleted_at = now()`.
5. **Every cloud write carries `id_user`.** Insert/Update on `archive_documents` and `storing` must include the logged-in user's ID.
6. **Material Design 3.** All UI uses MD3 components from `androidx.compose.material3`. No custom theme overrides until Figma handoff.
7. **Auth gates everything.** Every route except `/login` requires a valid session. No screen is reachable without an authenticated `UserSession`. Never store the JWT in plain `SharedPreferences`.
8. **Secrets never in source code.** Supabase URL and anon key live in `local.properties` only — never hardcoded, never committed. Access via `BuildConfig` fields injected at build time.

---

## 1. Project Identity

| Key | Value |
|-----|-------|
| App Name | Arsip BPKPAD Balangan |
| Package | `com.bpkpad.arsip` |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture (Domain / Data / UI layers) |
| DI | Hilt |
| Nav | Navigation Compose (Single Activity) |

---

## 2. Module & Package Map

```
com.bpkpad.arsip/
│
├── core/
│   ├── data/
│   │   ├── local/
│   │   │   ├── dao/          ← Room DAOs (one per entity)
│   │   │   └── entity/       ← Room @Entity classes
│   │   ├── remote/
│   │   │   ├── api/          ← Supabase client wrappers
│   │   │   └── dto/          ← Data Transfer Objects (mirrors Supabase tables)
│   │   └── repository/       ← Repository IMPLEMENTATIONS
│   ├── di/                   ← Hilt modules (@Module @InstallIn)
│   └── domain/
│       ├── model/            ← Pure Kotlin domain models (no Android imports)
│       ├── repository/       ← Repository INTERFACES
│       └── usecase/          ← One class per use case
│
├── feature/
│   ├── auth/                 ← Login screen + ViewModel + session guard
│   ├── dashboard/            ← Dashboard screen + ViewModel
│   ├── archive/              ← Archive list + detail screens + ViewModel
│   ├── staging/              ← Staging manager + input form + ViewModel
│   ├── camera/               ← CameraX + OCR screen + ViewModel
│   └── export/               ← Export Excel screen + ViewModel
│
└── ui/
    ├── navigation/           ← NavGraph, Routes, BottomNavBar
    ├── components/           ← Shared reusable Composables
    └── theme/                ← MaterialTheme, Color, Type, Shape
```

---

## 3. Tech Stack & Exact Dependencies

Add to `app/build.gradle.kts`. Always use **stable** versions.

```kotlin
// --- UI ---
implementation("androidx.compose.bom:2024.06.00")
implementation("androidx.compose.material3:material3")
implementation("androidx.navigation:navigation-compose:2.7.7")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
implementation("io.coil-kt:coil-compose:2.6.0")

// --- DI ---
implementation("com.google.dagger:hilt-android:2.51.1")
kapt("com.google.dagger:hilt-compiler:2.51.1")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

// --- Local DB (Offline-First) ---
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// --- Cloud Backend ---
implementation("io.github.jan-tennert.supabase:postgrest-kt:2.4.0")
implementation("io.github.jan-tennert.supabase:storage-kt:2.4.0")
implementation("io.ktor:ktor-client-android:2.3.11")

// --- Serialization ---
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

// --- AI & Camera ---
implementation("com.google.mlkit:text-recognition:16.0.0")
implementation("androidx.camera:camera-camera2:1.3.4")
implementation("androidx.camera:camera-lifecycle:1.3.4")
implementation("androidx.camera:camera-view:1.3.4")

// --- File Handling ---
implementation("org.apache.poi:poi:5.2.5")
implementation("org.apache.poi:poi-ooxml:5.2.5")

// --- Security ---
implementation("androidx.security:security-crypto:1.1.0-alpha06")   // EncryptedSharedPreferences
implementation("io.github.jan-tennert.supabase:gotrue-kt:2.4.0")    // Supabase Auth (GoTrue)
```

---

## 4. Database Schema

### 4A. Room (Local / Staging)

```kotlin
// entity/TempDocumentEntity.kt
@Entity(tableName = "temp_documents")
data class TempDocumentEntity(
    @PrimaryKey val id: String,           // UUID generated locally
    val type: String,                     // DocumentType enum name
    val title: String,
    val year: Int,
    val condition: String,
    val instance: String,
    val metadata: String,                 // JSON string — dynamic per type
    val coverLocalPath: String?,          // Local file path before upload
    val status: String = "local_only",    // local_only | synced
    val createdAt: Long = System.currentTimeMillis()
)

// entity/UserSessionEntity.kt  ← persisted ONLY in EncryptedSharedPreferences, NOT Room
// Do NOT store session in Room (unencrypted SQLite).
// Store as JSON string under key "user_session" in EncryptedSharedPreferences.
// Fields: userId, username, role, instance, accessToken, refreshToken, expiresAt (epoch ms)
```

### 4B. Supabase Tables (PostgreSQL)

> ⚠️ **Password rule:** Supabase Auth (GoTrue) manages authentication. The `users` table is a
> **profile** table synced from `auth.users`. Never store or compare raw passwords in application
> code. Password hashing is handled entirely by GoTrue on the server.

```sql
-- users
id uuid PK, name text, username text UNIQUE, password text,
role text CHECK (role IN ('Admin','Arsiparis','Kabid')),
profile_photo text NULLABLE, instance text,
created_at timestamptz, updated_at timestamptz, deleted_at timestamptz

-- storage_location
id uuid PK, room text, shelves text, number text

-- archive_documents
id uuid PK, type text, title text, year int, condition text,
is_copy bool, instance text, copy_count int, document_code text,
is_disposed bool, medium text, cover text,
metadata jsonb, created_at timestamptz, updated_at timestamptz,
deleted_at timestamptz, timestamp_user uuid REFERENCES users(id)

-- storing
id uuid PK, id_storage_location uuid REFERENCES storage_location(id),
id_archive_documents uuid REFERENCES archive_documents(id),
id_user uuid REFERENCES users(id)

-- logs
id uuid PK, id_user uuid REFERENCES users(id),
activity text, timestamp timestamptz DEFAULT now()
```

---

## 5. Domain Models

```kotlin
// domain/model/DocumentType.kt
enum class DocumentType {
    SURAT, PERDA, PERBUP, KEPUTUSAN_BUPATI, KEPUTUSAN_GUBERNUR
}

// domain/model/UserRole.kt
enum class UserRole { ADMIN, ARSIPARIS, KABID }

// domain/model/UserSession.kt  ← the single source of truth for "who is logged in"
data class UserSession(
    val userId: String,
    val username: String,
    val role: UserRole,
    val instance: String,       // SKPD/bidang — used for Kabid RLS filter
    val accessToken: String,    // Supabase JWT — attached to every API call
    val refreshToken: String,
    val expiresAt: Long         // epoch ms; refresh if System.currentTimeMillis() > expiresAt - 60_000
)

// domain/model/ArchiveDocument.kt
data class ArchiveDocument(
    val id: String,
    val type: DocumentType,
    val title: String,
    val year: Int,
    val condition: String,
    val instance: String,
    val metadata: Map<String, String>,   // deserialized JSONB
    val coverUrl: String?,
    val storageLocation: StorageLocation?,
    val deletedAt: String? = null
)

// domain/model/StorageLocation.kt
data class StorageLocation(
    val id: String,
    val room: String,
    val shelves: String,
    val number: String
)

// domain/model/StagingDocument.kt  (local only)
data class StagingDocument(
    val id: String,
    val type: DocumentType,
    val title: String,
    val year: Int,
    val metadata: Map<String, String>,
    val coverLocalPath: String?,
    val status: StagingStatus
)

enum class StagingStatus { LOCAL_ONLY, SYNCED }
```

---

## 6. Metadata Schema per Document Type

Each type uses a fixed set of keys in the `metadata: Map<String,String>`.
**ViewModel must validate these keys before staging.**

```
SURAT          → letter_number, subject, sender, receiver, letter_date, direction (IN|OUT)
PERDA          → regulation_number, authority, subject, effective_date, status (ACTIVE|REVOKED)
PERBUP         → regulation_number, subject, effective_date, status
KEPUTUSAN_BUPATI   → decision_number, subject, effective_date, status
KEPUTUSAN_GUBERNUR → decision_number, subject, effective_date, province
```

---

## 7. Repository Interfaces

```kotlin
// domain/repository/ArchiveRepository.kt
interface ArchiveRepository {
    fun getArchivedYears(): Flow<List<Int>>
    fun getDocumentsByYear(year: Int): Flow<List<ArchiveDocument>>
    suspend fun getDocumentById(id: String): ArchiveDocument?
    suspend fun pushToCloud(stagingId: String, locationId: String, userId: String): Result<Unit>
    suspend fun softDelete(id: String): Result<Unit>
}

// domain/repository/StagingRepository.kt
interface StagingRepository {
    fun getAllStaging(): Flow<List<StagingDocument>>
    suspend fun saveToStaging(doc: StagingDocument): Result<Unit>
    suspend fun updateStaging(doc: StagingDocument): Result<Unit>
    suspend fun deleteFromStaging(id: String)
    suspend fun pushAllToCloud(locationId: String, userId: String): Result<Unit>
}

// domain/repository/StorageLocationRepository.kt
interface StorageLocationRepository {
    fun getAllLocations(): Flow<List<StorageLocation>>
    suspend fun addLocation(location: StorageLocation): Result<Unit>
}

// domain/repository/AuthRepository.kt
interface AuthRepository {
    suspend fun login(username: String, password: String): Result<UserSession>
    suspend fun logout(): Result<Unit>
    suspend fun refreshSession(): Result<UserSession>
    fun getActiveSession(): UserSession?          // reads from EncryptedSharedPreferences
    fun isSessionValid(): Boolean                 // checks expiresAt; triggers refresh if near expiry
}
```

---

## 8. Use Cases (One Class Each)

```
GetArchivedYearsUseCase      → calls ArchiveRepository.getArchivedYears()
GetDocumentsByYearUseCase    → calls ArchiveRepository.getDocumentsByYear(year)
SaveToStagingUseCase         → validates metadata keys → calls StagingRepository.saveToStaging()
PushStagingToCloudUseCase    → ensures locationId not blank → calls StagingRepository.pushAllToCloud()
ImportXlsxUseCase            → parses .xlsx → validates box capacity → calls StagingRepository.saveToStaging() for each row
ExportXlsxUseCase            → queries ArchiveRepository → writes .xlsx via Apache POI
ScanOcrUseCase               → takes Bitmap → returns Map<String,String> (raw OCR fields)
MapOcrToMetadataUseCase      → takes raw OCR map + DocumentType → returns typed metadata Map
LoginUseCase                 → validates non-empty fields → calls AuthRepository.login() → saves session to EncryptedSharedPreferences
LogoutUseCase                → calls AuthRepository.logout() → clears EncryptedSharedPreferences → navigates to /login
RefreshSessionUseCase        → called on app foreground → calls AuthRepository.refreshSession() if token near expiry
```

---

## 9. Navigation Routes

```kotlin
// ui/navigation/Routes.kt
object Routes {
    const val LOGIN    = "login"                   // ← public; no auth required
    const val DASHBOARD   = "dashboard"
    const val ARCHIVE     = "archive/{year}"          // arg: year: Int
    const val STAGING     = "staging"
    const val CAMERA      = "camera"
    const val EXPORT      = "export"
    const val DETAIL      = "detail/{documentId}"     // arg: documentId: String
}
```

**NavGraph auth guard pattern:**
```kotlin
// In NavHost start destination logic:
val startDestination = if (authRepository.isSessionValid()) Routes.DASHBOARD else Routes.LOGIN

// Every protected composable() block must check session at entry:
composable(Routes.DASHBOARD) {
    val session = authRepository.getActiveSession()
    if (session == null) { navController.navigate(Routes.LOGIN) { popUpTo(0) }; return@composable }
    DashboardScreen(...)
}
```

Bottom Navigation items: **Dashboard**, **Archive**, **New Record (Staging)**, **Export**
> Bottom nav is hidden entirely on the `/login` screen.

---

## 10. Screen Specifications

### 10A. Login (`/login`) — PUBLIC ROUTE
- **ViewModel state:** `username: String`, `password: String`, `isLoading: Boolean`, `error: String?`
- **UI:** Logo, `OutlinedTextField` username, `OutlinedTextField` password (obscured, toggle visibility icon), `Button` "Masuk"
- **On success** → navigate to `/dashboard` with `popUpTo(Routes.LOGIN) { inclusive = true }` so back button cannot return to login
- **On error** → show `SnackBar` with message (do NOT expose raw server error; map to Indonesian user-friendly string)
- **No "Register" link** — account creation is Admin-only (future scope)

### 10B. Dashboard (`/dashboard`)
- **ViewModel state:** `years: List<YearSummary>`, `stagingCount: Int`, `stagingTotalSizeMb: Float`, `session: UserSession`
- `YearSummary(year: Int, recordCount: Int, isCurrent: Boolean)`
- **UI:** TopAppBar with logo + avatar (avatar tap → dropdown with "Logout"), search bar, `LazyColumn` of `YearCard`, staging panel at bottom
- **YearCard click** → navigate to `/archive/{year}`
- **FAB** → navigate to `/staging` (hidden for Kabid role)
- **Logout action** → calls `LogoutUseCase` → navigate to `/login` with full back-stack clear

### 10C. Archive List (`/archive/{year}`)
- **ViewModel state:** `documents: List<ArchiveDocument>`, `filterType: DocumentType?`, `query: String`
- **UI:** Search bar, horizontal `FilterChips` (All Types + each DocumentType), `LazyColumn` of `DocumentCard`
- **DocumentCard:** thumbnail (Coil), status badge, title, rack number, type, 3-dot menu (Edit / Delete — hidden for Kabid)
- **Card click** → navigate to `/detail/{documentId}`

### 10D. Staging / New Record (`/staging`)
- **ViewModel state:** `stagingList: List<StagingDocument>`, `inputMode: InputMode`, `selectedLocation: StorageLocation?`
- `InputMode` enum: `MANUAL`, `OCR`, `XLSX`
- **UI:** Form (Gudang dropdown, Rak field, Box field), 3-mode toggle buttons, dynamic metadata form per `DocumentType`, `LazyColumn` staging list at bottom
- **FAB (+)** → opens `BottomSheet` with mode selection (Manual / OCR / XLSX)
- **"Push Semua"** → calls `PushStagingToCloudUseCase` with selected location

### 10E. Camera / OCR (`/camera`)
- **ViewModel state:** `capturedBitmap: Bitmap?`, `selectedType: DocumentType`, `ocrResult: Map<String,String>`, `isProcessing: Boolean`
- **UI:** CameraX preview with crop overlay, `DropdownMenu` for DocumentType selection, auto-filled form fields after OCR, "Ulangi Scan" + "Simpan Record" buttons
- **Permission:** request `CAMERA` at runtime using `rememberLauncherForActivityResult`. If denied → show rationale dialog, do not crash.

### 10F. Export (`/export`)
- **ViewModel state:** `availableYears: List<Int>`, `selectedYears: Set<Int>`, `estimatedRows: Int`, `isExporting: Boolean`
- **UI:** Year grid (LazyVerticalGrid checkboxes), summary card, "Export to Excel (.xlsx)" button
- **Permission:** request `WRITE_EXTERNAL_STORAGE` (API < 29) or use `MediaStore`/`FileProvider` (API ≥ 29). Never request storage permission on API 29+ for app-scoped files.

---

## 11. RBAC Rules

| Role | Access |
|------|--------|
| Arsiparis | Full CRUD on all screens |
| Kabid | Read-only; archive list auto-filtered by `instance` (Row Level Security on Supabase) |
| Admin | User management (future scope) |

**Client-side guard helper:**
```kotlin
// ui/components/RoleGuard.kt
@Composable
fun RoleGuard(allowedRoles: Set<UserRole>, session: UserSession, content: @Composable () -> Unit) {
    if (session.role in allowedRoles) content()
    // else render nothing — server-side RLS is the real enforcement
}
```
> Never rely on client-side role checks alone. They are UX helpers only. Supabase RLS is the true enforcement layer.

---

## 12. Security Specification

### 12A. Secret Management
- Supabase URL and anon key go in `local.properties` only:
  ```
  SUPABASE_URL=https://xxxx.supabase.co
  SUPABASE_ANON_KEY=eyJhb...
  ```
- Expose via `BuildConfig` in `build.gradle.kts`:
  ```kotlin
  buildConfigField("String", "SUPABASE_URL", "\"${properties["SUPABASE_URL"]}\"")
  buildConfigField("String", "SUPABASE_ANON_KEY", "\"${properties["SUPABASE_ANON_KEY"]}\"")
  ```
- Add `local.properties` to `.gitignore`. **Never commit secrets.**

### 12B. Session Storage
- Store `UserSession` as JSON in `EncryptedSharedPreferences` (AES256-SIV key, AES256-GCM value).
- Key name: `"user_session"`. Clear entirely on logout.
- Do NOT store tokens in Room, plain SharedPreferences, or ViewModel alone.

### 12C. Token Lifecycle
- On every app foreground (`onResume` of `MainActivity` via `Lifecycle.Event.ON_RESUME`), call `RefreshSessionUseCase`.
- Refresh threshold: if `expiresAt - System.currentTimeMillis() < 60_000` ms (1 minute), refresh immediately.
- If refresh fails (network error) and token is still valid → continue. If token is expired → force logout → navigate to `/login`.

### 12D. Supabase Row Level Security (RLS) — Required Policies
Enable RLS on all tables. Minimum required policies:

| Table | Policy |
|-------|--------|
| `archive_documents` | Arsiparis: SELECT/INSERT/UPDATE own `instance`; Kabid: SELECT own `instance` only |
| `storing` | Arsiparis: INSERT/SELECT; Kabid: SELECT |
| `storage_location` | Arsiparis: full CRUD; Kabid: SELECT |
| `logs` | INSERT for authenticated users; SELECT for Admin only |
| `users` | SELECT own row only; Admin: full |

### 12E. Android Permissions
Declare in `AndroidManifest.xml` and request at runtime where required:

| Permission | When | API |
|------------|------|-----|
| `CAMERA` | Before opening OCR screen | All |
| `READ_EXTERNAL_STORAGE` | Before XLSX import picker | API ≤ 32 |
| `READ_MEDIA_IMAGES` | Before image picker (cover) | API ≥ 33 |
| `WRITE_EXTERNAL_STORAGE` | XLSX export to Downloads | API ≤ 28 only |
| `INTERNET` | Declared only (no runtime) | All |

Use `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` pattern. Always show rationale dialog on denial before re-requesting.

### 12F. Network Security
Add `res/xml/network_security_config.xml` and reference it in `AndroidManifest.xml`:
```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="false" />
</network-security-config>
```
This blocks all HTTP (non-TLS) traffic. Supabase uses HTTPS only — this is safe.

---

## 13. Edge Cases to Handle

| Scenario | Handling |
|----------|----------|
| No internet during push | Show error snackbar; keep data in staging |
| OCR returns empty text | Show "Scan gagal" dialog; allow manual retry |
| XLSX row count > box capacity | Block import; show validation error per row |
| Cloud record newer than local | Show conflict dialog: "Gunakan versi cloud?" |
| Soft delete | Set `deleted_at`; filter out in all queries with `WHERE deleted_at IS NULL` |
| Login credentials wrong | Show "Username atau password salah" — do NOT say which field is wrong |
| Session token expired on push | Call `RefreshSessionUseCase` first; if fails, redirect to `/login` with message "Sesi habis, silakan masuk kembali" |
| Camera permission denied | Show rationale dialog; offer "Isi Manual" fallback; do not crash |
| Storage permission denied (export) | Show rationale dialog; on API ≥ 29 use `MediaStore` which needs no permission |
| Kabid tries to access staging/input route | NavGraph guard redirects to `/dashboard`; show "Akses ditolak" snackbar |

---

## 14. Hilt Module Checklist

Create these `@Module` objects in `core/di/`:

- `DatabaseModule` → provides `AppDatabase`, all DAOs
- `RepositoryModule` → binds interfaces to implementations
- `SupabaseModule` → provides `SupabaseClient` singleton (URL + anon key from `BuildConfig`)
- `SecurityModule` → provides `EncryptedSharedPreferences` singleton
- `AuthModule` → provides `AuthRepository` implementation; depends on `SupabaseModule` + `SecurityModule`
- `UseCaseModule` → provides use case instances (if needed — prefer constructor injection)

---

## 15. Development Order (Simple → Complex)

Follow this order strictly. Do NOT jump ahead.

```
Phase 1 — Skeleton
  [1]  Theme + Color + Typography (ui/theme/)
  [2]  NavGraph + BottomNavBar (ui/navigation/) — include LOGIN route, hide bottom nav on it
  [3]  Placeholder screens (each feature/ folder)
  [4]  network_security_config.xml — block cleartext from day 1

Phase 2 — Auth Layer (do this before ANY feature screen)
  [5]  EncryptedSharedPreferences setup (SecurityModule)
  [6]  UserSession domain model + AuthRepository interface
  [7]  AuthRepositoryImpl (Supabase GoTrue login/logout/refresh)
  [8]  LoginUseCase + LogoutUseCase + RefreshSessionUseCase
  [9]  Login screen + LoginViewModel
  [10] NavGraph auth guard — start destination logic + per-route session check

Phase 3 — Local Data Layer
  [11] Room Entity + DAO (TempDocumentEntity)
  [12] DatabaseModule (Hilt)
  [13] StagingRepository (local only, no Supabase yet)

Phase 4 — Core Screens (UI + ViewModel, fake/local data)
  [14] Dashboard screen — YearCard list, logout button
  [15] Archive list screen — DocumentCard list with RoleGuard on edit/delete
  [16] Staging screen — form + LazyColumn + FAB (Arsiparis only)
  [17] Manual input form — dynamic metadata fields per DocumentType

Phase 5 — File Features
  [18] Import XLSX (ImportXlsxUseCase + UI trigger + READ_MEDIA_IMAGES permission)
  [19] Export XLSX (ExportXlsxUseCase + Export screen + storage permission handling)

Phase 6 — Camera & OCR
  [20] Runtime CAMERA permission request composable
  [21] CameraX preview composable
  [22] ML Kit OCR integration (ScanOcrUseCase)
  [23] MapOcrToMetadataUseCase + auto-fill form

Phase 7 — Cloud Sync
  [24] SupabaseModule + DTO classes
  [25] ArchiveRepositoryImpl (Supabase CRUD — JWT attached via GoTrue client)
  [26] PushStagingToCloudUseCase (with RefreshSession check before push)
  [27] Conflict resolution dialog

Phase 8 — Polish & Security Hardening
  [28] Supabase RLS policies (SQL — document in /supabase/migrations/)
  [29] RBAC client guards on all composables (RoleGuard helper)
  [30] Audit log writes on every cloud write
  [31] Soft delete everywhere
  [32] Finalize UI per Figma
```

---

## 16. Code Style Rules

- All `@Composable` functions: **stateless**, receive state + lambdas from ViewModel.
- ViewModel exposes `StateFlow<UiState>` (sealed class with Loading/Success/Error).
- No `coroutineScope.launch` inside Composables — use `LaunchedEffect` or ViewModel.
- Repository implementations in `core/data/repository/`, never in feature packages.
- All strings in `res/values/strings.xml` (Indonesian).
- Use `Result<T>` for all suspend functions that can fail.
