plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.agrotech.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.agrotech.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // URL do serviço GranjaCam (análise computacional em outro ambiente).
        // Vazia = a opção GranjaCam usa o mock local. Preenchida (https://...)
        // = abre o webviewer do serviço. Ex.: ./gradlew assembleDebug -PgranjacamUrl=https://cam.exemplo.com
        val granjacamUrl = (project.findProperty("granjacamUrl") as String?) ?: ""
        buildConfigField("String", "GRANJACAM_BASE_URL", "\"$granjacamUrl\"")
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.mlkit.text.recognition)
    implementation(libs.mlkit.barcode.scanning)

    // Auth & segurança local (bcrypt + EncryptedSharedPreferences como
    // equivalente local de HttpOnly — não exposto a outros apps, criptografado).
    implementation(libs.bcrypt)
    implementation(libs.androidx.security.crypto)

    // Localização para o check-in (foto + lat/lng + timestamp).
    implementation(libs.play.services.location)

    // Coil pra exibir a selfie salva em disco.
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
}
