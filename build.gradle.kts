plugins {
    application
    kotlin("jvm") version "1.3.61"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "com.roguepnz"
version = "1.0-SNAPSHOT"

val ktorVersion = "1.2.6"
val javaVersion = "11"
val kmongoVersion = "3.11.2"

repositories {
    jcenter()
    mavenCentral()
}

application {
    mainClassName = "com.roguepnz.memeagg.MemeAppKt"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.hazelcast:hazelcast:3.12.5")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.litote.kmongo:kmongo-coroutine:$kmongoVersion")
    implementation("com.typesafe:config:1.4.0")
    implementation("software.amazon.awssdk:s3:2.10.42")
    implementation("org.jsoup:jsoup:1.12.1")
    implementation("io.micrometer:micrometer-registry-prometheus:1.3.2")
    testCompile("junit:junit:4.12")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = javaVersion
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = javaVersion
    }
}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClassName
            )
        )
    }
}