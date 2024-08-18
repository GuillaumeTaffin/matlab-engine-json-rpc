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

val isWindows = Os.isFamily(Os.FAMILY_WINDOWS)
val isMacOs = Os.isFamily(Os.FAMILY_MAC)

val nativeLibsPath = if (isWindows) {
    "$matlabroot/bin/win64"
} else if (isMacOs) {
    if (Os.isArch("aarch64")) {
        "$matlabroot/bin/maca64"
    } else {
        "$matlabroot/bin/maci64"
    }
} else {
    "$matlabroot/bin/glnxa64:$matlabroot/sys/os/glnxa64"
}

val nativeLibPathVar = if (isWindows) {
    "PATH"
} else if (isMacOs) {
    "DYLD_LIBRARY_PATH"
} else {
    "LD_LIBRARY_PATH"
}

val startScript =

    application {
        mainClass = "com.gt.matlab.jsonrpc.ServerKt"

        applicationDefaultJvmArgs = listOf("-Djava.library.path=$nativeLibsPath")
    }

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.register<Exec>("serveEngine") {
    dependsOn("installDist")
    environment[nativeLibPathVar] = nativeLibsPath
    commandLine(
        layout.buildDirectory.file("install/app/bin/app${if (isWindows) ".bat" else ""}").get()
    )
}
