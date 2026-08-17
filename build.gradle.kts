import org.zaproxy.gradle.addon.AddOnStatus

plugins {
    java
    id("org.zaproxy.add-on") version "0.13.1"
}

group = "org.zaproxy.addon"
version = "1.0.0"
description =
    "GAP: Find the parameters, links and words that may not be obvious. Port of the GAP Burp extension by @xnl_h4ck3r."

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("net.htmlparser.jericho:jericho-html:3.4")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.zaproxy:zap:2.17.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

zapAddOn {
    addOnName.set("GAP")
    addOnStatus.set(AddOnStatus.ALPHA)
    zapVersion.set("2.17.0")

    manifest {
        author.set("Arkhamahn")
        url.set("https://github.com/ArkhaMahn/gap")
        bundle {
            baseName.set("org.zaproxy.addon.gap.resources.Messages")
            prefix.set("gap")
        }
    }
}

tasks.withType<JavaCompile> { options.compilerArgs.add("-Xlint:deprecation") }

tasks.withType<Test> {
    useJUnitPlatform()
}