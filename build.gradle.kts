plugins {
    kotlin("jvm") version "1.3.61"
}

group = "com.roguepnz"
version = "1.0-SNAPSHOT"

val ktorVersion = "1.2.6"
val javaVersion = "11"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("ch.qos.logback:logback-classic:1.2.3")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = javaVersion
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = javaVersion
    }
}