plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.voiceresponder"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.voiceresponder"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        // ── EmailJS credentials ───────────────────────────────────────────────
        buildConfigField("String", "EMAILJS_SERVICE_ID",       "\"service_gz1d4hl\"")
        buildConfigField("String", "EMAILJS_TEMPLATE_ID",      "\"template_mkbneej\"")   // signup OTP
        buildConfigField("String", "EMAILJS_RESET_TEMPLATE_ID","\"template_757meha\"") // password reset
        buildConfigField("String", "EMAILJS_PUBLIC_KEY",       "\"dCW2d5SDu2ABz0h2y\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("../release.jks")
            storePassword = "Asheesh@12345"
            keyAlias = "Asheeshresponder"
            keyPassword = "Asheeshgu@12345"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // ML Kit - On-device Translation (English <-> Hindi)
    implementation("com.google.mlkit:translate:17.0.2")
    
    // Google Identity Services – Phone Number Hint (free, no SMS needed)
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // OkHttp - for Cloud Speech-to-Text REST API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // WorkManager – for scheduling 24-hr Cloudinary auto-delete
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
