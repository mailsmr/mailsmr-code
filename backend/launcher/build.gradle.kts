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

    //  - SECURITY
    implementation("org.springframework.boot:spring-boot-starter-security")

    //  - SUPPORT
    implementation("org.springdoc", "springdoc-openapi-ui", "1.5.3")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

}
