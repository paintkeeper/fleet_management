import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.jaxb.Property

plugins {
    id("org.springframework.boot") version "2.3.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
    id("org.openapi.generator") version "5.0.0-beta"
    id("nu.studer.jooq") version "5.0"
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
    implementation("org.liquibase:liquibase-core:3.10.1")
    runtimeOnly("com.h2database:h2")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    jooqGenerator("com.h2database:h2")
    jooqGenerator("org.jooq:jooq-meta-extensions:3.13.4")
    jooqGenerator("org.jooq:jooq-codegen:3.13.4")
    jooqGenerator("org.jooq:jooq:3.13.4")
    jooqGenerator("org.liquibase:liquibase-core:3.10.1")
    jooqGenerator("org.yaml:snakeyaml:1.26")
    jooqGenerator("org.slf4j:slf4j-simple:1.7.30")

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


val openApiSpec = "$projectDir/src/main/resources/static/api-schema.yaml"
val openApiSourcesDir = "$buildDir/generated/openapi"

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set(openApiSpec)
    outputDir.set(openApiSourcesDir)

    apiPackage.set("com.freenow.api")
    modelPackage.set("com.freenow.model")
    invokerPackage.set("com.freenow")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "enumPropertyNaming" to "UPPERCASE",
            "useTags" to "true",
            "gradleBuildFile" to "false",
            "serviceInterface" to "true"
        )
    )
}

sourceSets.getByName("main") {
    java.srcDir(
        "$openApiSourcesDir/src/main/kotlin"
    )
}

jooq {
    version.set("3.13.3")
    edition.set(JooqEdition.OSS)

    configurations {

        create("main") {

            generateSchemaSourceOnCompilation.set(true)
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.ERROR
                generator.apply {
                    name = "org.jooq.codegen.JavaGenerator"
                    database.apply {
                        name = "org.jooq.meta.extensions.liquibase.LiquibaseDatabase"
                        jdbc.apply {
                            driver = "org.h2.Driver"
                            url = "jdbc:h2:mem:gradle"
                            user = "sa"
                            password = ""
                        }
                        properties.add(
                            Property().withKey("scripts")
                                .withValue("src/main/resources/db/changelog/master.yaml")
                        )
                        properties.add(
                            Property().withKey("includeLiquibaseTables")
                                .withValue("false")
                        )
                    }
                    generate.apply {
                        isRelations = true
                        isRecords = true
                    }
                    target.apply {
                        packageName = "com.freenow.jdbc"
                    }
                    strategy.apply {
                        name = "org.jooq.codegen.DefaultGeneratorStrategy"
                    }
                }
            }
        }
    }
}