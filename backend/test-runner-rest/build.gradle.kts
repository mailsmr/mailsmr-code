plugins {
    id("org.springframework.boot")
}


dependencies {
    implementation(project(":common-module"))
    testRuntimeOnly(project(":common-module", "testResourceArtifacts"))
    implementation(project(":persistence-module"))
    implementation(project(":authentication-module"))
    implementation(project(":user-management-module"))
    implementation(project(":launcher"))

    //  ===== TEST =====
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.apache.httpcomponents", "httpclient", "4.5.11")
}
