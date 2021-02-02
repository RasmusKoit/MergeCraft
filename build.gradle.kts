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
    maven("https://repo.codemc.io/repository/maven-public/")
}


dependencies {
    compileOnly("net.pl3x.purpur", "purpur-api", "1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7") {
        exclude(module = "*")
    }
    compileOnly("com.gmail.filoghost.holographicdisplays", "holographicdisplays-api", "2.4.0")
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

task<Copy>("copyJar") {
    from(shadowJar)
    into(file("C:\\Users\\Rasmus Koit\\Desktop\\mcserver\\plugins\\update"))
}.dependsOn(shadowJar)
tasks["build"].finalizedBy("copyJar")