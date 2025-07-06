plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.4.0"
}

group = "com.ajax.motrechko"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // OpenAPI/Swagger dependencies
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.4.0")

    // OpenAPI Generator dependencies
    implementation("io.swagger:swagger-annotations:1.6.12")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Pact dependencies
    testImplementation("au.com.dius.pact.consumer:junit5:4.6.8")
    testImplementation("au.com.dius.pact.provider:junit5:4.6.8")
    testImplementation("au.com.dius.pact.provider:spring:4.6.8")

    // Mockito for testing
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Configure OpenAPI Generator
openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/src/main/resources/static/swagger.yaml")
    outputDir.set("$buildDir/generated")
    apiPackage.set("com.ajax.motrechko.democontract.client.api")
    modelPackage.set("com.ajax.motrechko.democontract.client.model")
    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "enumPropertyNaming" to "UPPERCASE",
        "serializationLibrary" to "jackson",
        "collectionType" to "list"
    ))
}

// Add generated sources to the main source set
sourceSets {
    main {
        java {
            srcDir("$buildDir/generated/src/main/kotlin")
        }
    }
}

// Make sure the OpenAPI code is generated before compilation
tasks.compileKotlin.configure {
    dependsOn(tasks.openApiGenerate)
}
