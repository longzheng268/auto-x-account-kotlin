plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    application
}

group = "com.autoxtwitteraccount"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Kotlin 标准库
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    // 异步框架
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

    // JSON 序列化
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("com.google.code.gson:gson:2.10.1")

    // HTTP 客户端
    implementation("io.ktor:ktor-client-core:2.3.6")
    implementation("io.ktor:ktor-client-okhttp:2.3.6")
    implementation("io.ktor:ktor-client-logging:2.3.6")
    implementation("io.ktor:ktor-client-serialization:2.3.6")

    // CSV 和 Excel 支持
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("org.apache.poi:poi-ooxml:5.0.0")

    // 日志
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.12")
    implementation("ch.qos.logback:logback-core:1.4.12")

    // Playwright for Chromium automation
    implementation("com.microsoft.playwright:playwright:1.40.0")

    // GUI - FlatLaf with modern look
    implementation("com.formdev:flatlaf:3.2.5")
    implementation("com.formdev:flatlaf-intellij-themes:3.2.5")

    // i18n support
    implementation("commons-lang:commons-lang:2.6")

    // 时间库
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // 测试
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

kotlin {
    jvmToolchain(20)
}

application {
    mainClass.set("com.autoxtwitteraccount.MainKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "20"
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "com.autoxtwitteraccount.MainKt"
        )
    }
    
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    // 排除签名文件
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/MANIFEST.MF")
    
    // 创建 fat jar
    from(sourceSets["main"].output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.isFile }.map { zipTree(it) }
    })
}

tasks.create<JavaExec>("runApp") {
    group = "application"
    description = "Run the application with GUI"
    mainClass.set("com.autoxtwitteraccount.MainKt")
    classpath = sourceSets["main"].runtimeClasspath
}
