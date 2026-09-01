plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    id("com.google.gms.google-services") version "4.4.4" apply false

    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    //id("com.google.dagger.hilt.android") version "2.57.1" apply false

    kotlin("plugin.serialization") version "2.1.0" apply false
}