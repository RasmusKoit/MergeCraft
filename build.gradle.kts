plugins {
    java
}

group = "eu.ialbhost"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.pl3x.net/")

}

dependencies {
    compileOnly("net.pl3x.purpur", "purpur-api", "1.16.5-R0.1-SNAPSHOT")
}
