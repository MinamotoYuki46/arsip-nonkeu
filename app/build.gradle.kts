import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { inputStream ->
            load(inputStream)
        }
    }
}

fun localProperty(
    name: String,
    defaultValue: String = ""
): String {
    return localProperties.getProperty(name)
        ?: project.findProperty(name) as? String
        ?: defaultValue
}

fun buildConfigString(
    value: String
): String {
    val escapedValue = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")

    return "\"$escapedValue\""
}

android {
    namespace = "com.bpkpad.arsipnonkeu"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.bpkpad.arsipnonkeu"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val ocrAiProvider = localProperty(
            name = "OCR_AI_PROVIDER",
            defaultValue = "groq"
        )

        val ocrAiModel = localProperty(
            name = "OCR_AI_MODEL",
            defaultValue = "llama-3.1-8b-instant"
        )

        val groqApiKey = localProperty(
            name = "GROQ_API_KEY"
        )

        val geminiApiKey = localProperty(
            name = "GEMINI_API_KEY"
        )

        buildConfigField(
            type = "String",
            name = "OCR_AI_PROVIDER",
            value = buildConfigString(ocrAiProvider)
        )

        buildConfigField(
            type = "String",
            name = "OCR_AI_MODEL",
            value = buildConfigString(ocrAiModel)
        )

        buildConfigField(
            type = "String",
            name = "GROQ_API_KEY",
            value = buildConfigString(groqApiKey)
        )

        buildConfigField(
            type = "String",
            name = "GEMINI_API_KEY",
            value = buildConfigString(geminiApiKey)
        )

        val supabaseUrl = localProperty("SUPABASE_URL")
        val supabaseAnonKey = localProperty("SUPABASE_ANON_KEY")

        buildConfigField(
            type = "String",
            name = "SUPABASE_URL",
            value = buildConfigString(supabaseUrl)
        )

        buildConfigField(
            type = "String",
            name = "SUPABASE_ANON_KEY",
            value = buildConfigString(supabaseAnonKey)
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.kotlinx.serialization.json)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.functions)
    implementation(libs.ktor.client.android)

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // CameraX & ML Kit
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.mlkit.text.recognition)

    // Excel export writer
    implementation("org.dhatim:fastexcel:0.20.1")

    // Import Excel sekarang dibaca manual dari ZIP/XML,
    // jadi fastexcel-reader tidak wajib.
    // implementation("org.dhatim:fastexcel-reader:0.20.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}