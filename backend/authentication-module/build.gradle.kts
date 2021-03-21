plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common-module"))
    testRuntimeOnly(project(":common-module", "testResourceArtifacts"))
    implementation(project(":persistence-module"))
    implementation(project(":user-management-module"))
    implementation(project(":websocket-common-module"))

    implementation("org.springframework.boot:spring-boot-starter-websocket")

    //  - SECURITY
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(group = "org.springframework.security", name = "spring-security-messaging", version= "5.4.5")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("com.github.ulisesbocchio:jasypt-maven-plugin:3.0.2")

    //  - DATABASE
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    //  - SUPPORT
    implementation("org.springdoc", "springdoc-openapi-ui", "1.5.3")

    //  ===== TEST =====
    testImplementation("org.springframework.security:spring-security-test")

}
