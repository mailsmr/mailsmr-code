plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common-module"))
    testRuntimeOnly(project(":common-module", "testResourceArtifacts"))
    implementation(project(":persistence-module"))
    implementation(project(":websocket-common-module"))

    // - WEB SOCKET AND MESSAGING
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    //  - SECURITY
    implementation("org.springframework.boot:spring-boot-starter-security")

    testImplementation("org.springframework.security:spring-security-test")


    //  - DATABASE
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    //  - SUPPORT
    implementation("org.springdoc", "springdoc-openapi-ui", "1.5.3")
}
