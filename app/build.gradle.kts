import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    alias(libs.plugins.jvm)
    application
}

repositories {
    mavenCentral()
}

val matlabroot = providers.gradleProperty("matlabroot").get()

dependencies {
    implementation(files("$matlabroot/extern/engines/java/jar/engine.jar"))
    implementation(platform("org.http4k:http4k-bom:5.27.0.0"))
    implementation("org.http4k:http4k-format-jackson")
    implementation("org.http4k:http4k-jsonrpc")
    implementation("org.http4k:http4k-server-undertow")
    implementation("org.http4k:http4k-client-websocket")
    implementation("org.http4k:http4k-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

kotlin {
    javaToolchains {
        jvmToolchain(11)
    }
}

application {
    mainClass = "com.gt.matlab.jsonrpc.ServerKt"
    val nativeLibsPath = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
        "$matlabroot/bin/win64"
    } else if (Os.isFamily(Os.FAMILY_MAC)) {
        if (Os.isArch("aarch64")) {
            "$matlabroot/bin/maca64"
        } else {
            "$matlabroot/bin/maci64"
        }
    } else {
        "$matlabroot/bin/glnxa64:$matlabroot/sys/os/glnxa64"
    }
    applicationDefaultJvmArgs = listOf("-Djava.library.path=$nativeLibsPath")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
