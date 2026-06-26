plugins {
    java
}

group = "com.lmmessage"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    val playerChatJar = providers.gradleProperty("playerChatJar")
        .orElse(providers.environmentVariable("PLAYERCHAT_JAR"))
    if (playerChatJar.isPresent) {
        compileOnly(files(playerChatJar.get()))
    } else {
        logger.warn("PlayerChat API jar not configured; set -PplayerChatJar=<path> or PLAYERCHAT_JAR for local builds.")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.test {
    useJUnitPlatform()
}
