plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common-module"))
    testRuntimeOnly(project(":common-module", "testResourceArtifacts"))
    implementation(project(":persistence-module"))
    implementation(project(":websocket-common-module"))

    // E-Mail
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    implementation("jakarta.activation:jakarta.activation-api:2.0.1")
    implementation("com.google.guava:guava:30.1.1-jre")

    testImplementation("com.icegreen:greenmail-junit5:2.0.0-alpha-1")

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
