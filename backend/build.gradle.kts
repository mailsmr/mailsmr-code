import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.spring") version "1.4.21"
    kotlin("plugin.jpa") version "1.4.21"
    jacoco

    id("org.springframework.boot") version "2.4.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    id("net.saliman.properties") version "1.5.1" apply false

    id("com.dorongold.task-tree") version "1.5"

}

java.sourceCompatibility = JavaVersion.VERSION_11

tasks.bootJar {
    enabled = false
}

allprojects {
    group = "ch.rjenni"
    version = "0.0.1-SNAPSHOT"

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.majorVersion
        targetCompatibility = JavaVersion.VERSION_11.majorVersion
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = JavaVersion.VERSION_11.majorVersion
        }
    }

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.gradle.jacoco")

    apply(plugin = "org.springframework.boot")


    apply(plugin = "net.saliman.properties")

    tasks.processResources {
        filesMatching("application.properties") {
            expand(project.properties)
        }
    }

    tasks.processTestResources {
        filesMatching("application.properties") {
            expand(project.properties)
        }
    }

    tasks {
        jar {
            enabled = true
        }
        bootJar {
            enabled = false
        }
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:2.4.2")
        }
    }


    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-websocket")

        //  - SUPPORT
        implementation("com.google.code.gson:gson:2.8.6")

        //  - CORE
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("io.github.microutils:kotlin-logging:1.12.0")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.+")
        compileOnly("org.projectlombok:lombok")

        //  ===== ANNOTATIONS =====
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        annotationProcessor("org.projectlombok:lombok")
        //  ===== TEST =====
        testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("com.h2database:h2")
        testImplementation("org.projectlombok:lombok")
        testImplementation("org.mockito:mockito-core:2.+")
        testImplementation("org.mockito:mockito-junit-jupiter:2.18.3")
        testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
        testImplementation("com.mercateo:test-clock:1.0.2")
    }


    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
    }
    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
            html.destination = file("${buildDir}/jacocoHtml")
        }
    }
}
// TODO remove if not needed in future
//configurations {
//    compileOnly {
//        extendsFrom(configurations.annotationProcessor.get())
//    }
//}
