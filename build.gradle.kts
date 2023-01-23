import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    kotlin("plugin.jpa") version "1.6.10"
}

group = "ru.devkirill"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("ru.devkirill:rss:1.0.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("org.projectlombok:lombok")

    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.72")
    implementation("io.ktor:ktor-client-cio:1.3.1")
    implementation("org.telegram:telegrambots:4.9.1")
    implementation("org.telegram:telegrambotsextensions:4.9.1")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//    implementation("org.xerial:sqlite-jdbc:3.25.2")
    runtimeOnly("org.postgresql:postgresql")
    implementation("com.zsoltfabok:sqlite-dialect:1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}
