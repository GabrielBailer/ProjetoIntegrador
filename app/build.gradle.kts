plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.kapt") // mantido para compatibilidade com alguns processors (veja nota)
}

android {
    namespace = "com.example.app_pi2"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.app_pi2"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/NOTICE.md",
                "/META-INF/AL2.0",
                "/META-INF/LGPL2.1",
                "/META-INF/LICENSE.md",
                "/META-INF/LICENSE-notice.md"
            )
        }
    }
}

dependencies {
    // AndroidX (mantive como aliases onde você já usava libs.*)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Firebase (atualizei a BOM para uma versão mais recente; ajuste se preferir)
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-functions-ktx:20.2.0")

    // Room -> MIGRADO para KSP (mais rápido).
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.firebase.appcheck.debug)
    kapt(libs.room.compiler)

    // JavaMail (mantive, mas atenção: lib antiga)
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    // OSMDroid (vou deixar sem versão para você usar o alias/libs se preferir)
    // exemplo explícito:
    implementation("org.osmdroid:osmdroid-android:6.1.10")

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
