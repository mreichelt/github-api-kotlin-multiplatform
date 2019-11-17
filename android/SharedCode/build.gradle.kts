import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

kotlin {
    //select iOS target platform depending on the Xcode environment variables
    val iOSTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iOSTarget("ios") {
        binaries {
            framework {
                baseName = "SharedCode"
            }
        }
    }

    jvm("android")

    val ktorVersion = "1.2.5"
    val coroutinesVersion = "1.3.2"
    val serializationVersion = "0.13.0"

    sourceSets["commonMain"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVersion")
        implementation("io.ktor:ktor-client-core:$ktorVersion")
        implementation("io.ktor:ktor-client-json:$ktorVersion")
        implementation("io.ktor:ktor-client-serialization:$ktorVersion")
        implementation("io.ktor:ktor-client-logging:$ktorVersion")
        api("io.islandtime:core:0.1.0-SNAPSHOT")
        implementation("co.touchlab:stately:0.9.3")
    }

    sourceSets["androidMain"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
        implementation("io.ktor:ktor-client-android:$ktorVersion")
        implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
        implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
        implementation("io.ktor:ktor-client-serialization-jvm:$ktorVersion")
        implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
        api("io.islandtime:core-jvm:0.1.0-SNAPSHOT")
    }

    sourceSets["androidTest"].dependencies {
        implementation("junit:junit:4.12")
    }

    sourceSets["iosMain"].dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$serializationVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion")
        implementation("io.ktor:ktor-client-ios:$ktorVersion")
        implementation("io.ktor:ktor-client-json-native:$ktorVersion")
        implementation("io.ktor:ktor-client-serialization-native:$ktorVersion")
        implementation("io.ktor:ktor-client-logging-native:$ktorVersion")
        api("io.islandtime:core-iosx64:0.1.0-SNAPSHOT")
        implementation("co.touchlab:stately-collections:0.9.3")
    }
}

val packForXcode by tasks.creating(Sync::class) {
    val targetDir = File(buildDir, "xcode-frameworks")

    /// selecting the right configuration for the iOS
    /// framework depending on the environment
    /// variables set by Xcode build
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val framework = kotlin.targets
        .getByName<KotlinNativeTarget>("ios")
        .binaries.getFramework(mode)
    inputs.property("mode", mode)
    dependsOn(framework.linkTask)

    from({ framework.outputDirectory })
    into(targetDir)

    /// generate a helpful ./gradlew wrapper with embedded Java path
    doLast {
        val gradlew = File(targetDir, "gradlew")
        gradlew.writeText(
            """
            #!/bin/bash
            export 'JAVA_HOME=${System.getProperty("java.home")}'
            cd '${rootProject.rootDir}'
            ./gradlew $@
            """.trimIndent()
        )
        gradlew.setExecutable(true)
    }
}

tasks.getByName("build").dependsOn(packForXcode)
