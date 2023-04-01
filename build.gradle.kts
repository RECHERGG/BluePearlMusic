plugins {
    java
    id("com.github.johnrengelman.shadow") version ("7.1.2")
}

group = "de.rechergg"
version = "v1.0.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://m2.dv8tion.net/releases")
    }
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.6")
    implementation("net.dv8tion:JDA:5.0.0-beta.6")


    //LavaPlayer
    implementation("com.sedmelluq:lavaplayer:1.3.78")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("commons-codec:commons-codec:1.15")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    shadowJar {
        mergeServiceFiles()
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}