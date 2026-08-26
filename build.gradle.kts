plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    id("com.google.gms.google-services") version "4.4.4" apply false

    alias(libs.plugins.ksp) apply false

    id("com.google.dagger.hilt.android") version "2.57.1" apply false

    kotlin("plugin.serialization") version "2.4.10" apply false
}