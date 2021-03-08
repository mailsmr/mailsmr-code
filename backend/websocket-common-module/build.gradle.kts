plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.4.2")implementation("org.junit.jupiter:junit-jupiter:5.4.2")// - WEB SOCKET AND MESSAGING
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation(group = "org.springframework.security", name = "spring-security-messaging", version= "5.4.5")

    //  - SECURITY
    implementation("org.springframework.boot:spring-boot-starter-security")

    testImplementation("org.springframework.security:spring-security-test")
}