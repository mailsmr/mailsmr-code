plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common-module"))
    testRuntimeOnly(project(":common-module", "testResourceArtifacts"))
    implementation(project(":persistence-module"))

    // E-Mail
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    implementation("jakarta.activation:jakarta.activation-api:2.0.1")
    implementation("com.google.guava:guava:30.1.1-jre")

//    testImplementation("com.icegreen:greenmail-junit5:2.0.0-alpha-1")
    testImplementation(files("../libs/greenmail-2.0.0-SNAPSHOT.jar"))
    testImplementation(files("../libs/greenmail-junit5-2.0.0-SNAPSHOT.jar"))

    //  - SECURITY
    implementation("org.springframework.boot:spring-boot-starter-security")

    testImplementation("org.springframework.security:spring-security-test")

    //  - DATABASE
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
