-- =========================================================
-- BPKPAD Arsip Non Keuangan - Reset Migration
-- Target: Supabase PostgreSQL
-- Mode: DEV RESET
-- =========================================================

create extension if not exists "pgcrypto";

-- =========================================================
-- DROP OLD OBJECTS
-- WARNING: This will delete existing archive/staging data.
-- =========================================================

drop function if exists push_staging_document_to_archive(uuid, text, text, text, uuid);

drop table if exists activity_logs cascade;
drop table if exists document_placements cascade;
drop table if exists archive_documents cascade;
drop table if exists storage_locations cascade;
drop table if exists staging_documents cascade;
drop table if exists archive_classifications cascade;

drop type if exists document_type cascade;
drop type if exists physical_form cascade;
drop type if exists document_condition cascade;
drop type if exists document_status cascade;
drop type if exists staging_document_source cascade;

-- =========================================================
-- ENUM TYPES
-- =========================================================

create type document_type as enum (
    'SURAT',
    'PERDA',
    'PERBUP',
    'KEPBUP',
    'KEPGUB'
);

create type physical_form as enum (
    'SHEET',
    'BOOK'
);

create type document_condition as enum (
    'GOOD',
    'DAMAGED'
);

create type document_status as enum (
    'BORROWED',
    'ARCHIVED',
    'DISPOSED'
);

create type staging_document_source as enum (
    'MANUAL',
    'SCAN',
    'IMPORT'
);

-- =========================================================
-- UPDATED_AT TRIGGER FUNCTION
-- =========================================================

create or replace function set_updated_at()
returns trigger as $$
begin
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;

-- =========================================================
-- ARCHIVE CLASSIFICATIONS
-- =========================================================

create table archive_classifications (
    code text primary key,
    name text not null,
    parent_code text null references archive_classifications(code) on delete set null,
    level integer not null default 1 check (level > 0),
    is_active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create trigger trg_archive_classifications_updated_at
before update on archive_classifications
for each row
execute function set_updated_at();

create index idx_archive_classifications_parent_code
on archive_classifications(parent_code);

create index idx_archive_classifications_name
on archive_classifications using gin (to_tsvector('simple', name));

-- =========================================================
-- STAGING DOCUMENTS
-- =========================================================

create table staging_documents (
    id uuid primary key default gen_random_uuid(),

    document_type document_type not null,
    document_number text null,
    classification_code text null references archive_classifications(code) on delete set null,

    title text not null,
    description text null,
    year integer not null check (year between 1900 and 2100),

    physical_form physical_form not null,
    condition document_condition not null default 'GOOD',
    copy_count integer not null default 1 check (copy_count > 0),
    is_copy boolean null,

    status document_status not null default 'ARCHIVED',
    origin_instance text null,
    source staging_document_source not null default 'MANUAL',

    created_by uuid null references auth.users(id) on delete set null,
    updated_by uuid null references auth.users(id) on delete set null,

    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create trigger trg_staging_documents_updated_at
before update on staging_documents
for each row
execute function set_updated_at();

create index idx_staging_documents_classification_code
on staging_documents(classification_code);

create index idx_staging_documents_document_type
on staging_documents(document_type);

create index idx_staging_documents_status
on staging_documents(status);

create index idx_staging_documents_source
on staging_documents(source);

create index idx_staging_documents_year
on staging_documents(year);

create index idx_staging_documents_title
on staging_documents using gin (to_tsvector('simple', title));

-- =========================================================
-- STORAGE LOCATIONS
-- =========================================================

create table storage_locations (
    id uuid primary key default gen_random_uuid(),

    room text not null,
    shelf text not null,
    box_number text null,

    description text null,
    is_active boolean not null default true,

    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),

    constraint uq_storage_locations_room_shelf_box
        unique (room, shelf, box_number)
);

create trigger trg_storage_locations_updated_at
before update on storage_locations
for each row
execute function set_updated_at();

create index idx_storage_locations_room
on storage_locations(room);

create index idx_storage_locations_shelf
on storage_locations(shelf);

-- =========================================================
-- ARCHIVE DOCUMENTS
-- =========================================================

create table archive_documents (
    id uuid primary key default gen_random_uuid(),

    document_type document_type not null,
    document_number text null,
    classification_code text null references archive_classifications(code) on delete set null,

    title text not null,
    description text null,
    year integer not null check (year between 1900 and 2100),

    physical_form physical_form not null,
    condition document_condition not null default 'GOOD',
    copy_count integer not null default 1 check (copy_count > 0),
    is_copy boolean null,

    status document_status not null default 'ARCHIVED',
    origin_instance text null,

    storage_location_id uuid null references storage_locations(id) on delete set null,
    source_staging_id uuid null references staging_documents(id) on delete set null,

    created_by uuid null references auth.users(id) on delete set null,
    updated_by uuid null references auth.users(id) on delete set null,

    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create trigger trg_archive_documents_updated_at
before update on archive_documents
for each row
execute function set_updated_at();

create index idx_archive_documents_classification_code
on archive_documents(classification_code);

create index idx_archive_documents_storage_location_id
on archive_documents(storage_location_id);

create index idx_archive_documents_document_type
on archive_documents(document_type);

create index idx_archive_documents_status
on archive_documents(status);

create index idx_archive_documents_year
on archive_documents(year);

create index idx_archive_documents_title
on archive_documents using gin (to_tsvector('simple', title));

-- =========================================================
-- DOCUMENT PLACEMENT HISTORY
-- =========================================================

create table document_placements (
    id uuid primary key default gen_random_uuid(),

    archive_document_id uuid not null references archive_documents(id) on delete cascade,
    storage_location_id uuid not null references storage_locations(id) on delete restrict,

    placed_at timestamptz not null default now(),
    removed_at timestamptz null,

    note text null,
    created_by uuid null references auth.users(id) on delete set null,

    created_at timestamptz not null default now()
);

create index idx_document_placements_archive_document_id
on document_placements(archive_document_id);

create index idx_document_placements_storage_location_id
on document_placements(storage_location_id);

create index idx_document_placements_active
on document_placements(archive_document_id)
where removed_at is null;

-- =========================================================
-- ACTIVITY LOGS
-- =========================================================

create table activity_logs (
    id uuid primary key default gen_random_uuid(),

    actor_id uuid null references auth.users(id) on delete set null,
    action text not null,
    entity_type text not null,
    entity_id uuid null,

    metadata jsonb not null default '{}'::jsonb,

    created_at timestamptz not null default now()
);

create index idx_activity_logs_actor_id
on activity_logs(actor_id);

create index idx_activity_logs_entity
on activity_logs(entity_type, entity_id);

create index idx_activity_logs_created_at
on activity_logs(created_at desc);

-- =========================================================
-- HELPER FUNCTION: PUSH ONE STAGING DOCUMENT TO ARCHIVE
-- =========================================================

create or replace function push_staging_document_to_archive(
    p_staging_document_id uuid,
    p_room text,
    p_shelf text,
    p_box_number text default null,
    p_actor_id uuid default null
)
returns uuid
language plpgsql
security definer
as $$
declare
    v_staging staging_documents%rowtype;
    v_location_id uuid;
    v_archive_id uuid;
begin
    select *
    into v_staging
    from staging_documents
    where id = p_staging_document_id;

    if not found then
        raise exception 'Staging document not found: %', p_staging_document_id;
    end if;

    insert into storage_locations (
        room,
        shelf,
        box_number
    )
    values (
        trim(p_room),
        trim(p_shelf),
        nullif(trim(coalesce(p_box_number, '')), '')
    )
    on conflict (room, shelf, box_number)
    do update set
        updated_at = now()
    returning id into v_location_id;

    insert into archive_documents (
        document_type,
        document_number,
        classification_code,
        title,
        description,
        year,
        physical_form,
        condition,
        copy_count,
        is_copy,
        status,
        origin_instance,
        storage_location_id,
        source_staging_id,
        created_by,
        updated_by
    )
    values (
        v_staging.document_type,
        v_staging.document_number,
        v_staging.classification_code,
        v_staging.title,
        v_staging.description,
        v_staging.year,
        v_staging.physical_form,
        v_staging.condition,
        v_staging.copy_count,
        v_staging.is_copy,
        v_staging.status,
        v_staging.origin_instance,
        v_location_id,
        v_staging.id,
        coalesce(p_actor_id, v_staging.created_by),
        coalesce(p_actor_id, v_staging.updated_by)
    )
    returning id into v_archive_id;

    insert into document_placements (
        archive_document_id,
        storage_location_id,
        note,
        created_by
    )
    values (
        v_archive_id,
        v_location_id,
        'Initial placement from staging',
        p_actor_id
    );

    delete from staging_documents
    where id = p_staging_document_id;

    insert into activity_logs (
        actor_id,
        action,
        entity_type,
        entity_id,
        metadata
    )
    values (
        p_actor_id,
        'PUSH_STAGING_TO_ARCHIVE',
        'archive_document',
        v_archive_id,
        jsonb_build_object(
            'staging_document_id', p_staging_document_id,
            'storage_location_id', v_location_id
        )
    );

    return v_archive_id;
end;
$$;

-- =========================================================
-- SEED ARCHIVE CLASSIFICATIONS
-- Based on kode_klasifikasi_arsip_non_keuangan.md
-- =========================================================

insert into archive_classifications (code, name, parent_code, level)
values
    -- 000 UMUM
    ('000', 'UMUM', null, 1),
    ('000.1', 'KETATAUSAHAAN DAN KERUMAHTANGGAAN', '000', 2),
    ('000.1.1', 'Telekomunikasi', '000.1', 3),
    ('000.1.2', 'Perjalanan Dinas Dalam Negeri', '000.1', 3),
    ('000.1.2.1', 'Perjalanan Dinas Kepala Daerah', '000.1.2', 4),
    ('000.1.2.2', 'Perjalanan Dinas DPRD', '000.1.2', 4),
    ('000.1.2.3', 'Perjalanan Dinas Pegawai', '000.1.2', 4),
    ('000.1.3', 'Perjalanan Dinas Luar Negeri', '000.1', 3),
    ('000.1.3.1', 'Perjalanan Dinas Kepala Daerah', '000.1.3', 4),
    ('000.1.3.2', 'Perjalanan Dinas DPRD', '000.1.3', 4),
    ('000.1.3.3', 'Perjalanan Dinas Pegawai', '000.1.3', 4),
    ('000.1.4', 'Penggunaan Fasilitas Kantor', '000.1', 3),
    ('000.1.5', 'Rapat Pimpinan', '000.1', 3),
    ('000.1.6', 'Penyediaan Konsumsi', '000.1', 3),
    ('000.1.7', 'Pengurusan Kendaraan Dinas', '000.1', 3),
    ('000.1.7.1', 'Pengurusan surat-surat kendaraan dinas', '000.1.7', 4),
    ('000.1.7.2', 'Pemeliharaan dan perbaikan', '000.1.7', 4),
    ('000.1.7.3', 'Pengurusan kehilangan dan masalah kendaraan', '000.1.7', 4),
    ('000.1.8', 'Pemeliharaan Gedung, Taman, dan Peralatan Kantor', '000.1', 3),
    ('000.1.8.1', 'Pertamanan/Landscape', '000.1.8', 4),
    ('000.1.8.2', 'Penghijauan', '000.1.8', 4),
    ('000.1.8.3', 'Perbaikan Gedung', '000.1.8', 4),
    ('000.1.8.4', 'Perbaikan Peralatan Kantor', '000.1.8', 4),
    ('000.1.8.5', 'Perbaikan Rumah Dinas/Wisma', '000.1.8', 4),
    ('000.1.8.6', 'Kebersihan Gedung dan Taman', '000.1.8', 4),
    ('000.1.9', 'Jaringan Listrik, Air, Telepon, dan Komputer', '000.1', 3),
    ('000.1.9.1', 'Pengelolaan', '000.1.9', 4),
    ('000.1.9.2', 'Perbaikan/Pemeliharaan/Pemasangan', '000.1.9', 4),
    ('000.1.10', 'Ketertiban dan Keamanan', '000.1', 3),
    ('000.1.10.1', 'Pengamanan, penjagaan, pengawalan terhadap pejabat, kantor, dan rumah dinas', '000.1.10', 4),
    ('000.1.10.2', 'Laporan ketertiban dan keamanan', '000.1.10', 4),
    ('000.1.11', 'Administrasi Pengelolaan Parkir', '000.1', 3),
    ('000.1.12', 'Administrasi Pakaian Dinas Pegawai, Satpam, Petugas Kebersihan', '000.1', 3),

    ('000.2', 'PERLENGKAPAN', '000', 2),
    ('000.2.1', 'Inventarisasi dan Penyimpanan', '000.2', 3),
    ('000.2.1.1', 'Data hasil inventarisasi dan penyimpanan', '000.2.1', 4),
    ('000.2.1.2', 'Laporan dan evaluasi inventarisasi dan penyimpanan', '000.2.1', 4),
    ('000.2.2', 'Pemeliharaan Peralatan Kantor', '000.2', 3),
    ('000.2.2.1', 'Data hasil pemeliharaan kantor', '000.2.2', 4),
    ('000.2.2.2', 'Laporan dan evaluasi pemeliharaan kantor', '000.2.2', 4),
    ('000.2.3', 'Distribusi', '000.2', 3),
    ('000.2.3.1', 'Barang habis pakai', '000.2.3', 4),
    ('000.2.3.2', 'Barang milik daerah', '000.2.3', 4),
    ('000.2.4', 'Penghapusan Barang Milik Daerah', '000.2', 3),
    ('000.2.5', 'Pengelolaan Database Barang Milik Daerah', '000.2', 3),

    ('000.3', 'PENGADAAN', '000', 2),
    ('000.3.1', 'Rencana Pengadaan Barang dan Jasa', '000.3', 3),
    ('000.3.2', 'Pengadaan Langsung', '000.3', 3),
    ('000.3.3', 'Pengadaan Tidak Langsung/Lelang', '000.3', 3),
    ('000.3.4', 'Swakelola', '000.3', 3),
    ('000.3.5', 'Pengolahan Sistem Informasi Pengadaan', '000.3', 3),
    ('000.3.6', 'Monitoring dan Evaluasi', '000.3', 3),

    ('000.4', 'PERPUSTAKAAN', '000', 2),
    ('000.4.1', 'Kebijakan di Bidang Perpustakaan', '000.4', 3),
    ('000.4.2', 'Deposit Bahan Pustaka', '000.4', 3),
    ('000.4.3', 'Koleksi Pustaka', '000.4', 3),
    ('000.4.4', 'Pengolahan Bahan Pustaka', '000.4', 3),
    ('000.4.5', 'Pangkalan Data Katalog Koleksi', '000.4', 3),
    ('000.4.6', 'Layanan Perpustakaan', '000.4', 3),
    ('000.4.7', 'Kerjasama Perpustakaan', '000.4', 3),
    ('000.4.8', 'Implementasi Teknologi Informasi Perpustakaan', '000.4', 3),
    ('000.4.9', 'Pangkalan Data Layanan Perpustakaan', '000.4', 3),
    ('000.4.10', 'Konservasi', '000.4', 3),
    ('000.4.11', 'Reprografi', '000.4', 3),
    ('000.4.12', 'Transformasi Digital', '000.4', 3),
    ('000.4.13', 'Kurasi Digital', '000.4', 3),
    ('000.4.14', 'Pengembangan Perpustakaan', '000.4', 3),

    ('000.5', 'KEARSIPAN', '000', 2),
    ('000.5.1', 'Kebijakan di Bidang Kearsipan', '000.5', 3),
    ('000.5.2', 'Pembinaan Kearsipan', '000.5', 3),
    ('000.5.3', 'Pengelolaan Arsip Dinamis', '000.5', 3),
    ('000.5.3.1', 'Penciptaan', '000.5.3', 4),
    ('000.5.3.2', 'Pemberkasan Arsip Aktif', '000.5.3', 4),
    ('000.5.3.3', 'Penataan Arsip Inaktif', '000.5.3', 4),
    ('000.5.3.4', 'Penggunaan', '000.5.3', 4),
    ('000.5.3.5', 'Autentikasi Arsip Dinamis', '000.5.3', 4),
    ('000.5.4', 'Program Arsip Vital', '000.5', 3),
    ('000.5.5', 'Pengelolaan Arsip Terjaga', '000.5', 3),
    ('000.5.6', 'Penyusutan Arsip', '000.5', 3),
    ('000.5.7', 'Alih Media Arsip', '000.5', 3),
    ('000.5.8', 'Database Pengelolaan Arsip Dinamis', '000.5', 3),
    ('000.5.9', 'Pengelolaan Arsip Statis', '000.5', 3),
    ('000.5.10', 'Akses Arsip Statis / Jasa Kearsipan', '000.5', 3),
    ('000.5.11', 'Pengelolaan SIKN dan JIKN', '000.5', 3),
    ('000.5.12', 'Perlindungan dan Penyelamatan Arsip Akibat Bencana', '000.5', 3),
    ('000.5.13', 'Penyelamatan Arsip Perangkat Daerah Digabung/Dibubarkan', '000.5', 3),
    ('000.5.14', 'Penerbitan Izin Penggunaan Arsip Bersifat Tertutup', '000.5', 3),
    ('000.5.15', 'Pengawasan Kearsipan', '000.5', 3),

    ('000.6', 'PERSANDIAN', '000', 2),
    ('000.6.1', 'Kebijakan di Bidang Persandian', '000.6', 3),
    ('000.6.2', 'Pengamanan Persandian', '000.6', 3),
    ('000.6.3', 'Pengkajian Persandian', '000.6', 3),
    ('000.6.4', 'Pembinaan dan Pengendalian Persandian', '000.6', 3),
    ('000.6.5', 'Layanan Sertifikasi Elektronik', '000.6', 3),

    ('000.7', 'PERENCANAAN PEMBANGUNAN', '000', 2),
    ('000.7.1', 'Musyawarah Perencanaan Pembangunan/Musrenbang', '000.7', 3),
    ('000.7.2', 'Perencanaan Pembangunan Daerah', '000.7', 3),
    ('000.7.3', 'Koordinasi dan Sinkronisasi Perencanaan Pembangunan', '000.7', 3),
    ('000.7.4', 'Konsultasi Perencanaan Pembangunan', '000.7', 3),
    ('000.7.5', 'Pemantauan, Evaluasi, Penilaian, dan Pelaporan Perencanaan Pembangunan', '000.7', 3),
    ('000.7.6', 'Aksi Strategis Daerah', '000.7', 3),
    ('000.7.7', 'Perencanaan Pendanaan Pembangunan', '000.7', 3),

    ('000.8', 'ORGANISASI DAN TATA LAKSANA', '000', 2),
    ('000.8.1', 'Struktur Organisasi Pemda', '000.8', 3),
    ('000.8.2', 'Uraian Jabatan dan Tata Kerja', '000.8', 3),
    ('000.8.3', 'Ketatalaksanaan', '000.8', 3),
    ('000.8.4', 'Standar Kompetensi Jabatan Struktural dan Fungsional', '000.8', 3),
    ('000.8.5', 'Evaluasi Kelembagaan', '000.8', 3),
    ('000.8.6', 'Koordinasi Penguatan Reformasi dan Birokrasi', '000.8', 3),

    ('000.9', 'PENELITIAN, PENGKAJIAN, DAN PENGEMBANGAN', '000', 2),
    ('000.9.1', 'Kebijakan Litbang Pemda', '000.9', 3),
    ('000.9.2', 'Pelaksanaan Penelitian, Pengkajian, dan Pengembangan', '000.9', 3),
    ('000.9.3', 'Sosialisasi dan Diseminasi Hasil Penelitian', '000.9', 3),
    ('000.9.4', 'Bimbingan Teknis Penelitian, Pengkajian, dan Pengembangan', '000.9', 3),
    ('000.9.5', 'Forum Komunikasi Penelitian, Pengembangan, dan Penerapan Iptek', '000.9', 3),
    ('000.9.6', 'Data dan Informasi Hasil Penelitian', '000.9', 3),
    ('000.9.7', 'Master Proceeding/Jurnal Penelitian', '000.9', 3),
    ('000.9.8', 'Hak atas Kekayaan Intelektual', '000.9', 3),
    ('000.9.9', 'Evaluasi Pelaksanaan Kebijakan', '000.9', 3),
    ('000.9.10', 'Seminar, Lokakarya, Temukarya, Workshop', '000.9', 3),

    -- 100 PEMERINTAHAN
    ('100', 'PEMERINTAHAN', null, 1),
    ('100.1', 'OTONOMI DAERAH', '100', 2),
    ('100.1.1', 'Kebijakan di Bidang Otonomi Daerah', '100.1', 3),
    ('100.1.2', 'Penyelenggaraan Pemerintah Daerah', '100.1', 3),
    ('100.1.3', 'Penataan Daerah, Pembinaan Daerah Pemekaran, Otonomi Khusus', '100.1', 3),
    ('100.1.4', 'Pemilihan Kepala Daerah, DPRD, dan Hubungan Antar Lembaga', '100.1', 3),
    ('100.1.5', 'Peningkatan Kapasitas dan Evaluasi Kinerja Daerah', '100.1', 3),
    ('100.1.6', 'LKPJ/LKPJAMJ dan LPPD', '100.1', 3),

    ('100.2', 'PEMERINTAHAN UMUM', '100', 2),
    ('100.2.1', 'Kebijakan di Bidang Pemerintahan Umum', '100.2', 3),
    ('100.2.2', 'Dekonsentrasi dan Kerjasama', '100.2', 3),
    ('100.2.3', 'Wilayah Administrasi dan Perbatasan', '100.2', 3),

    ('100.3', 'HUKUM', '100', 2),
    ('100.3.1', 'Program Legislasi', '100.3', 3),
    ('100.3.2', 'Rancangan Peraturan Perundang-Undangan', '100.3', 3),
    ('100.3.3', 'Keputusan/Ketetapan Pimpinan Pemerintah', '100.3', 3),
    ('100.3.4', 'Instruksi/Surat Edaran', '100.3', 3),
    ('100.3.5', 'Surat Perintah', '100.3', 3),
    ('100.3.6', 'Standar/Pedoman/Prosedur Kerja/Juklak/Juknis', '100.3', 3),
    ('100.3.7', 'Nota Kesepakatan/MoU/Kontrak/PKS', '100.3', 3),
    ('100.3.8', 'Dokumentasi Hukum', '100.3', 3),
    ('100.3.9', 'Sosialisasi/Penyuluhan/Pembinaan Hukum', '100.3', 3),
    ('100.3.10', 'Bantuan/Konsultasi Hukum/Advokasi', '100.3', 3),
    ('100.3.11', 'Kasus/Sengketa Hukum', '100.3', 3),
    ('100.3.12', 'Perizinan', '100.3', 3),
    ('100.3.13', 'Hak atas Kekayaan Intelektual / HaKI', '100.3', 3),
    ('100.3.14', 'Permohonan HaKI yang ditolak', '100.3', 3),

    -- 200 POLITIK
    ('200', 'POLITIK', null, 1),
    ('200.1', 'KESATUAN BANGSA DAN POLITIK', '200', 2),
    ('200.1.1', 'Kebijakan di Bidang Kesatuan Bangsa dan Politik', '200.1', 3),
    ('200.1.2', 'Bina Ideologi dan Wawasan Kebangsaan', '200.1', 3),
    ('200.1.3', 'Kewaspadaan Nasional', '200.1', 3),
    ('200.1.4', 'Ketahanan Seni, Budaya, Adat, Agama, dan Kemasyarakatan', '200.1', 3),
    ('200.1.5', 'Politik Dalam Negeri', '200.1', 3),
    ('200.1.6', 'Ketahanan Ekonomi', '200.1', 3),

    ('200.2', 'PEMILIHAN UMUM', '200', 2),
    ('200.2.1', 'Kebijakan di Bidang Pemilu', '200.2', 3),
    ('200.2.2', 'Pemutakhiran dan Penyusunan Daftar Pemilih', '200.2', 3),
    ('200.2.3', 'Pendaftaran dan Verifikasi Peserta Pemilu', '200.2', 3),
    ('200.2.4', 'Penetapan Daerah Pemilihan dan Jumlah Kursi Anggota', '200.2', 3),
    ('200.2.5', 'Pencalonan', '200.2', 3),
    ('200.2.6', 'Kampanye', '200.2', 3),
    ('200.2.7', 'Dana Kampanye', '200.2', 3),
    ('200.2.8', 'Pemungutan dan Penghitungan Suara', '200.2', 3),
    ('200.2.9', 'Penetapan Hasil Pemilu', '200.2', 3),
    ('200.2.10', 'Perselisihan Hasil Pemilu', '200.2', 3),
    ('200.2.11', 'Laporan Hasil Penyelenggaraan Pemilu', '200.2', 3),

    -- 300 KEAMANAN DAN KETERTIBAN
    ('300', 'KEAMANAN DAN KETERTIBAN', null, 1),
    ('300.1', 'SATUAN POLISI PAMONG PRAJA', '300', 2),
    ('300.1.1', 'Kebijakan di Bidang Polisi Pamong Praja', '300.1', 3),
    ('300.1.2', 'Tata Operasional dan Prasarana Sarana Satpol PP', '300.1', 3),
    ('300.1.3', 'Peningkatan Kapasitas SDM Satpol PP', '300.1', 3),
    ('300.1.4', 'Perlindungan Masyarakat', '300.1', 3),
    ('300.1.5', 'Penyidik Pegawai Negeri Sipil', '300.1', 3),
    ('300.1.6', 'Perlindungan Hak-Hak Sipil dan Hak Asasi Manusia', '300.1', 3),

    ('300.2', 'PENANGGULANGAN BENCANA, PENCARIAN, DAN PERTOLONGAN', '300', 2),
    ('300.2.1', 'Kebijakan di Bidang Penanggulangan Bencana', '300.2', 3),
    ('300.2.2', 'Perencanaan Penanggulangan Bencana, Pencarian, dan Pertolongan', '300.2', 3),
    ('300.2.3', 'Pencegahan dan Kesiapsiagaan', '300.2', 3),
    ('300.2.4', 'Potensi Pencarian dan Pertolongan', '300.2', 3),
    ('300.2.5', 'Bina Ketenagaan dan Pemasyarakatan', '300.2', 3),
    ('300.2.6', 'Operasi Pencarian dan Pertolongan', '300.2', 3),
    ('300.2.7', 'Rencana Pengembangan dan Standardisasi Komunikasi', '300.2', 3),
    ('300.2.8', 'Operasi Komunikasi', '300.2', 3),
    ('300.2.9', 'Inventarisasi dan Pemeliharaan', '300.2', 3),
    ('300.2.10', 'Pengembangan Sistem Informasi', '300.2', 3),
    ('300.2.11', 'Penyatuan dan Layanan Informasi', '300.2', 3),
    ('300.2.12', 'Pelaporan dan Evaluasi', '300.2', 3),

    -- 400 KESEJAHTERAAN RAKYAT
    ('400', 'KESEJAHTERAAN RAKYAT', null, 1),
    ('400.1', 'Pembangunan Daerah Tertinggal', '400', 2),
    ('400.2', 'Pemberdayaan Perempuan dan Perlindungan Anak', '400', 2),
    ('400.3', 'Pendidikan', '400', 2),
    ('400.4', 'Keolahragaan', '400', 2),
    ('400.5', 'Kepemudaan', '400', 2),
    ('400.6', 'Kebudayaan', '400', 2),
    ('400.7', 'Kesehatan', '400', 2),
    ('400.8', 'Agama dan Kepercayaan', '400', 2),
    ('400.9', 'Sosial', '400', 2),
    ('400.10', 'Pemberdayaan Masyarakat Desa', '400', 2),
    ('400.11', 'Pertamanan dan Pemakaman', '400', 2),
    ('400.12', 'Kependudukan dan Catatan Sipil', '400', 2),
    ('400.13', 'Keluarga Berencana', '400', 2),
    ('400.14', 'Hubungan Masyarakat / Humas', '400', 2),

    -- 500 PEREKONOMIAN
    ('500', 'PEREKONOMIAN', null, 1),
    ('500.1', 'Ketahanan Pangan', '500', 2),
    ('500.2', 'Perdagangan', '500', 2),
    ('500.3', 'Koperasi dan Usaha Kecil Menengah', '500', 2),
    ('500.4', 'Kehutanan', '500', 2),
    ('500.5', 'Kelautan dan Perikanan', '500', 2),
    ('500.6', 'Pertanian', '500', 2),
    ('500.7', 'Peternakan', '500', 2),
    ('500.8', 'Perkebunan', '500', 2),
    ('500.9', 'Perindustrian', '500', 2),
    ('500.10', 'Energi dan Sumber Daya Mineral', '500', 2),
    ('500.11', 'Perhubungan', '500', 2),
    ('500.12', 'Komunikasi dan Informatika', '500', 2),
    ('500.13', 'Pariwisata dan Ekonomi Kreatif', '500', 2),
    ('500.14', 'Statistik', '500', 2),
    ('500.15', 'Ketenagakerjaan', '500', 2),
    ('500.16', 'Penanaman Modal', '500', 2),
    ('500.17', 'Pertanahan', '500', 2),
    ('500.18', 'Transmigrasi', '500', 2),

    -- 600 PEKERJAAN UMUM DAN KETENAGAAN
    ('600', 'PEKERJAAN UMUM DAN KETENAGAAN', null, 1),
    ('600.1', 'Pekerjaan Umum', '600', 2),
    ('600.2', 'Perumahan Rakyat dan Kawasan Permukiman', '600', 2),
    ('600.3', 'Tata Ruang', '600', 2),
    ('600.4', 'Lingkungan Hidup', '600', 2),

    -- 700 PENGAWASAN
    ('700', 'PENGAWASAN', null, 1),
    ('700.1', 'PENGAWASAN INTERNAL', '700', 2),
    ('700.1.1', 'Rencana Pengawasan', '700.1', 3),
    ('700.1.2', 'Pelaksanaan Pengawasan', '700.1', 3),

    -- 800 KEPEGAWAIAN
    ('800', 'KEPEGAWAIAN', null, 1),
    ('800.1', 'SUMBER DAYA MANUSIA', '800', 2),
    ('800.1.1', 'Penyusunan dan Penetapan Kebutuhan ASN', '800.1', 3),
    ('800.1.2', 'Pengadaan Pegawai', '800.1', 3),
    ('800.1.3', 'Mutasi Pegawai', '800.1', 3),
    ('800.2', 'PENDIDIKAN DAN PELATIHAN', '800', 2),
    ('800.2.1', 'Perencanaan diklat, pembinaan widyaiswara, penyelenggaraan diklat', '800.2', 3)
on conflict (code)
do update set
    name = excluded.name,
    parent_code = excluded.parent_code,
    level = excluded.level,
    updated_at = now();

-- =========================================================
-- OPTIONAL SEED STAGING DUMMY DATA
-- =========================================================

insert into staging_documents (
    id,
    document_type,
    document_number,
    classification_code,
    title,
    description,
    year,
    physical_form,
    condition,
    copy_count,
    is_copy,
    status,
    origin_instance,
    source
)
values
    (
        '00000000-0000-0000-0000-000000000001',
        'SURAT',
        '001/UMUM/2025',
        '000.1.5',
        'Surat Undangan Rapat Koordinasi',
        'Surat undangan rapat koordinasi internal BPKPAD.',
        2025,
        'SHEET',
        'GOOD',
        1,
        false,
        'ARCHIVED',
        'Bagian Umum',
        'MANUAL'
    ),
    (
        '00000000-0000-0000-0000-000000000002',
        'KEPBUP',
        '188.45/25/KUM/2024',
        '100.3.3',
        'Keputusan Bupati Tentang Pembentukan Tim Kerja',
        'Dokumen keputusan bupati tentang pembentukan tim kerja daerah.',
        2024,
        'BOOK',
        'GOOD',
        1,
        false,
        'ARCHIVED',
        'Bagian Hukum',
        'SCAN'
    ),
    (
        '00000000-0000-0000-0000-000000000003',
        'PERDA',
        '12 Tahun 2025',
        '100.3.2',
        'Peraturan Daerah Tentang Pengelolaan Keuangan Daerah',
        'Dokumen Peraturan Daerah terkait pengelolaan keuangan daerah.',
        2025,
        'BOOK',
        'GOOD',
        2,
        true,
        'ARCHIVED',
        'Sekretariat Daerah',
        'IMPORT'
    )
on conflict (id)
do update set
    document_type = excluded.document_type,
    document_number = excluded.document_number,
    classification_code = excluded.classification_code,
    title = excluded.title,
    description = excluded.description,
    year = excluded.year,
    physical_form = excluded.physical_form,
    condition = excluded.condition,
    copy_count = excluded.copy_count,
    is_copy = excluded.is_copy,
    status = excluded.status,
    origin_instance = excluded.origin_instance,
    source = excluded.source,
    updated_at = now();

-- =========================================================
-- ROW LEVEL SECURITY
-- =========================================================

alter table archive_classifications enable row level security;
alter table staging_documents enable row level security;
alter table storage_locations enable row level security;
alter table archive_documents enable row level security;
alter table document_placements enable row level security;
alter table activity_logs enable row level security;

create policy "Allow authenticated read archive classifications"
on archive_classifications
for select
to authenticated
using (true);

create policy "Allow authenticated read staging documents"
on staging_documents
for select
to authenticated
using (true);

create policy "Allow authenticated insert staging documents"
on staging_documents
for insert
to authenticated
with check (true);

create policy "Allow authenticated update staging documents"
on staging_documents
for update
to authenticated
using (true)
with check (true);

create policy "Allow authenticated delete staging documents"
on staging_documents
for delete
to authenticated
using (true);

create policy "Allow authenticated read storage locations"
on storage_locations
for select
to authenticated
using (true);

create policy "Allow authenticated write storage locations"
on storage_locations
for all
to authenticated
using (true)
with check (true);

create policy "Allow authenticated read archive documents"
on archive_documents
for select
to authenticated
using (true);

create policy "Allow authenticated write archive documents"
on archive_documents
for all
to authenticated
using (true)
with check (true);

create policy "Allow authenticated read document placements"
on document_placements
for select
to authenticated
using (true);

create policy "Allow authenticated write document placements"
on document_placements
for all
to authenticated
using (true)
with check (true);

create policy "Allow authenticated read activity logs"
on activity_logs
for select
to authenticated
using (true);

create policy "Allow authenticated write activity logs"
on activity_logs
for insert
to authenticated
with check (true);