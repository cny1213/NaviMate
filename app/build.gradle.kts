plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.psg.navimate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.psg.navimate"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 문자열 리터럴로 바로 넣지 말고, property() 로 불러오세요
        buildConfigField("String", "NAVER_CLIENT_ID", "\"${property("NAVER_CLIENT_ID")}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${property("NAVER_CLIENT_SECRET")}\"")
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //스플래쉬 스크린 변경
    implementation(libs.androidx.core.splashscreen)
    // 네이버 지도 SDK
    implementation(libs.map.sdk)
    // 네이버 검색 api
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    // add Gson for parsing
    implementation(libs.gson)
    // OkHttp 로깅 인터셉터
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")
    // 로깅 인터셉터 의존
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
}