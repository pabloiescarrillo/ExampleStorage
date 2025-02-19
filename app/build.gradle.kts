plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "es.iescarrillo.android.examplestorage"
    compileSdk = 35

    defaultConfig {
        applicationId = "es.iescarrillo.android.examplestorage"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildToolsVersion = "35.0.0"
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //Dependencia librería Picasso
    implementation("com.squareup.picasso:picasso:2.8")
    // Dependencias Firebase
    // BOM => herramientas generales de Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    // Herramientas de analíticas de uso
    implementation("com.google.firebase:firebase-analytics")
    // Módulo Firebase Real Time
    implementation("com.google.firebase:firebase-database")
    // Dependencias Retrofit & GSON
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10")

    implementation("com.google.firebase:firebase-storage")
}