plugins {
    java
}

group = "eu.ialbhost"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.pl3x.net/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")

    maven("https://jitpack.io")

}

dependencies {
    compileOnly("net.pl3x.purpur", "purpur-api", "1.16.5-R0.1-SNAPSHOT")
    compileOnly ("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl","VaultAPI","1.7")

}
