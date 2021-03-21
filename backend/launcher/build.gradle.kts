plugins {
    id("org.springframework.boot")
}

tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = true
    launchScript()
    archiveClassifier.set("boot")
}

dependencies {
    implementation(project(":common-module"))
    testRuntimeOnly(project(":common-module", "testResourceArtifacts"))
    implementation(project(":persistence-module"))
    implementation(project(":user-management-module"))
    implementation(project(":authentication-module"))
    implementation(project(":mail-management-module"))

    //    Websocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    //  - SECURITY
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(group = "org.springframework.security", name = "spring-security-messaging", version= "5.4.5")

    //  - SUPPORT
    implementation("org.springdoc", "springdoc-openapi-ui", "1.5.3")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

}
