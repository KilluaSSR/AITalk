plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "killua.dev.aitalk"
    compileSdk = 36

    defaultConfig {
        applicationId = "killua.dev.aitalk"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val supportedLocales = generateSupportedLocales()
        buildConfigField("String[]", "SUPPORTED_LOCALES", supportedLocales)
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        buildConfig = true
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
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

fun generateSupportedLocales(): String {
    val languages = fileTree("src/main/res")
        .filter { it.name == "strings.xml" }
        .map { file ->
            val dirName = file.parentFile.name
            when (dirName) {
                "values" -> "zh-CN"
                else -> dirName.substringAfter("values-")
                    .replace("-r", "-")
            }
        }
        .toSortedSet()
    return languages.joinToString(
        prefix = "new String[]{",
        postfix = "}",
        separator = ",",
        transform = { "\"$it\"" }
    )
}

dependencies {

    //Markdown
    implementation("com.github.jeziellago:compose-markdown:0.5.7")

    // AndroidX & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.startup.runtime)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Hilt (DI)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room (Database) & Paging
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Network & Data
    implementation(libs.okhttp)
    implementation(libs.moshi.kotlin)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)
    implementation(libs.coil.compose)
    implementation(libs.jsoup)
    implementation(libs.androidx.datastore.preferences)

    // Coroutines & Serialization
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // Other
    implementation(libs.material)
    implementation(libs.androidx.biometric)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}