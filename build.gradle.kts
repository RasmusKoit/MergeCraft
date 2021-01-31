import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
plugins {
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "eu.ialbhost"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.pl3x.net/")
    maven("https://jitpack.io")

}


dependencies {
    compileOnly("net.pl3x.purpur", "purpur-api", "1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl","VaultAPI","1.7") {
        exclude(module = "*")
    }
    implementation("com.zaxxer", "HikariCP", "4.0.1") {
        exclude(module = "slf4j-api")
    }

}

val shadowJar by tasks.getting(ShadowJar::class) {
    val relocations = listOf(
            "com.zaxxer.hikari"
    )
    val targetPackage = "eu.ialbhost.mergecraft.lib"

    relocations.forEach {
        relocate(it, "$targetPackage.$it")
    }
}
tasks["build"].dependsOn(shadowJar)