plugins {
    application
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.web3j:core:4.10.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.1")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("org.slf4j:slf4j-simple:2.0.13")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceSets {
        main {
            java {
                srcDirs("src")
            }
        }
    }
}

application {
    mainClass.set("Main")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}


