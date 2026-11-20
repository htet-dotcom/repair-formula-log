import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Release signing comes from a properties file OUTSIDE the repository, named by the
// RFL_SIGNING environment variable (storeFile, storePassword, keyAlias, keyPassword).
val signingProps: Properties? = System.getenv("RFL_SIGNING")?.let { path ->
    File(path).takeIf { it.isFile }?.let { f -> Properties().apply { f.inputStream().use { load(it) } } }
}

android {
    namespace = "com.htet.repairlog"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.htet.repairlog"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
    }

    signingConfigs {
        if (signingProps != null) create("release") {
            storeFile = File(signingProps.getProperty("storeFile"))
            storePassword = signingProps.getProperty("storePassword")
            keyAlias = signingProps.getProperty("keyAlias")
            keyPassword = signingProps.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    // The web app (index.html + icons at the repository root) stays the single source:
    // it is copied into the APK's assets at build time.
    sourceSets["main"].assets.srcDir(layout.buildDirectory.dir("generated/webassets"))
}

val copyWebApp by tasks.registering(Sync::class) {
    from(rootDir.parentFile) {
        include("index.html", "manifest.webmanifest", "icons/**")
    }
    into(layout.buildDirectory.dir("generated/webassets/web"))
}
tasks.named("preBuild") { dependsOn(copyWebApp) }

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
}
