import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
    id("org.openapi.generator") version "5.0.0-beta"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

apply(plugin = "checkstyle")

configure<CheckstyleExtension> {
    maxErrors = 10
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springdoc:springdoc-openapi-ui:1.4.4")
    runtimeOnly("com.h2database:h2")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test")


}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
    dependsOn("openApiGenerate")
}


val spec = "$projectDir/src/main/resources/static/api-schema.yaml"
val generatedSourcesDir = "$buildDir/generated/openapi"

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set(spec)
    outputDir.set(generatedSourcesDir)

    apiPackage.set("com.freenow.api")
    invokerPackage.set("com.freenow.app")
    modelPackage.set("com.freenow.model")

    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "enumPropertyNaming" to "UPPERCASE",
            "apiSuffix" to "Controller",
            "useTags" to "true",
            "gradleBuildFile" to "false",
            "serviceInterface" to "true"
        )
    )
}

sourceSets.getByName("main") {
    java.srcDir("$generatedSourcesDir/src/main/kotlin")
}
