plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Version affichée (à augmenter à chaque étape, voir CHANGELOG.md).
val appVersion = "0.12.0"

// Numéro de build fourni par GitHub Actions (1 en local). Il sert de versionCode,
// qui doit toujours augmenter pour qu'Android accepte la mise à jour.
val buildNumber = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()

// Signature : la clé est fournie par l'environnement (secret GitHub en CI).
// Sans clé, l'APK est signé avec la clé de debug locale.
val keystorePath: String? = System.getenv("SENTIER_KEYSTORE_FILE")
val keystorePassword: String? = System.getenv("SENTIER_KEYSTORE_PASSWORD")

android {
    namespace = "app.sentier"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.sentier"
        minSdk = 28
        targetSdk = 35
        versionCode = buildNumber
        versionName = appVersion
    }

    signingConfigs {
        if (keystorePath != null && keystorePassword != null) {
            create("sentier") {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = System.getenv("SENTIER_KEY_ALIAS") ?: "sentier"
                keyPassword = System.getenv("SENTIER_KEY_PASSWORD") ?: keystorePassword
            }
        }
    }

    buildTypes {
        val signing = signingConfigs.findByName("sentier") ?: signingConfigs.getByName("debug")
        release {
            // R8 retire le code et les ressources inutilisés (voir proguard-rules.pro).
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signing
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            signingConfig = signing
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
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    kspTest(libs.room.compiler)
    implementation(libs.health.connect)
    implementation(libs.datastore.preferences)
    implementation(libs.work.runtime)
    implementation(libs.glance.appwidget)
    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.junit)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.work.testing)
    debugImplementation(libs.compose.ui.test.manifest)
}

tasks.withType<Test>().configureEach {
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStackTraces = true
        showCauses = true
    }
}
