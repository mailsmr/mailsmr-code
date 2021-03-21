val testConfig = configurations.create("testResourceArtifacts") {
    extendsFrom(configurations["testCompile"])
}

tasks.register("testJar", Jar::class.java) {
    dependsOn("testClasses")
    archiveClassifier.set("test")
    from(sourceSets["test"].resources)
}

artifacts {
    add("testResourceArtifacts", tasks.named<Jar>("testJar"))
}

dependencies {
    //  - SECURITY
    implementation("org.springframework.boot:spring-boot-starter-security")
}
