import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget =  JavaVersion.VERSION_11.toString()
}

kotlin {
    targets {
        jvm("android") // for java/kotlin modules only
//        android() //for an Android base module
    }
////    select iOS target platform depending on the Xcode environment variables
//Ref : https://medium.com/icerock/enable-iosmain-sourceset-in-kotlin-multiplatform-mobile-projects-91168db9ac5c
//Ref : https://github.com/Kotlin/kotlin-examples/pull/107/files#diff-7d72dddeceb582e6fb3a3b18b18c0e93a590ed670bf567349ea59bf5df35f1b7
//Ref : https://github.com/cashapp/sqldelight/issues/2044
//Ref : https://github.com/touchlab/KaMPKit/blob/main/shared/build.gradle.kts#L28
//    val iOSTarget = System.getenv("SDK_NAME")?.startsWith("iphoneos") ?: false
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
//    if (iOSTarget) {
//        iosArm64("ios") {
//            binaries {
//                framework {
//                    baseName = "SharedCode"
//                }
//            }
//        }
//    }
//    else{
//        iosX64("ios") {
//            binaries {
//                framework {
//                    baseName = "SharedCode"
//                }
//            }
//        }
//    }

    val ktorVersion = "2.1.2"
    val coroutinesVersion = "1.6.4"
    //https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/migration.md
    val serializationVersion = "1.4.0"
    val statelyVersion = "1.0.3"
    val islandTimeVersion = "0.2.4"

    sourceSets["commonMain"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

        implementation("io.ktor:ktor-client-core:$ktorVersion")
        implementation("io.ktor:ktor-client-json:$ktorVersion")
        implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
        implementation("io.ktor:ktor-client-serialization:$ktorVersion")
        implementation("io.ktor:ktor-client-logging:$ktorVersion")
        api("io.islandtime:core:$islandTimeVersion")
        implementation("co.touchlab:stately-common:$statelyVersion")
    }

    sourceSets["androidMain"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
        implementation("io.ktor:ktor-client-android:$ktorVersion")
        implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
        implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
        implementation("io.ktor:ktor-client-serialization-jvm:$ktorVersion")
        implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
        api("io.islandtime:core-jvm:$islandTimeVersion")
    }

    sourceSets["androidTest"].dependencies {
        implementation("junit:junit:4.12")
    }

    sourceSets["iosMain"].dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-iosarm64:$serializationVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-iosarm64:$serializationVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-iosx64:$serializationVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-iosx64:$serializationVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-iossimulatorarm64:$serializationVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-iossimulatorarm64:$serializationVersion")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-iosx64:$coroutinesVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-iosarm64:$coroutinesVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-iossimulatorarm64:$coroutinesVersion")

        implementation("io.ktor:ktor-client-ios:$ktorVersion")
        implementation("io.ktor:ktor-client-ios-iosx64:$ktorVersion")

        implementation("io.ktor:ktor-client-serialization-iosx64:$ktorVersion")
        implementation("io.ktor:ktor-client-serialization-iosarm64:$ktorVersion")

        implementation("io.ktor:ktor-client-json-iosarm64:$ktorVersion")
        implementation("io.ktor:ktor-client-json-iosx64:$ktorVersion")

        implementation("io.ktor:ktor-client-json-iossimulatorarm64:$ktorVersion")

        implementation("io.ktor:ktor-client-logging-iosx64:$ktorVersion")
        implementation("io.ktor:ktor-client-logging-iosarm64:$ktorVersion")

        api("io.islandtime:core-iosx64:$islandTimeVersion")
        implementation("co.touchlab:stately-collections:$statelyVersion")
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
