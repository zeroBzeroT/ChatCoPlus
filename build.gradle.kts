plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
}

group = property("group") as String
version = property("version") as String
description = "An efficient chat system with colored text, ignore list, whispering and chat logging."

val minecraftVersion = property("minecraftVersion") as String

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion-R0.1-SNAPSHOT")
    compileOnly("dev.folia:folia-api:$minecraftVersion-R0.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("redis.clients:jedis:5.1.3")
}

// Replaces ${project.version} / ${project.description} in the plugin resources,
// like the Maven resource filtering did.
tasks.processResources {
    val tokens = mapOf(
        "project" to mapOf("version" to project.version, "description" to project.description)
    )
    inputs.properties(tokens)
    filesMatching(listOf("plugin.yml", "config.yml", "permissionConfig.yml")) {
        expand(tokens)
    }
}

tasks.jar {
    // The shaded jar is the only release artifact, so skip the thin one.
    enabled = false
}

tasks.shadowJar {
    archiveClassifier = ""
    manifest {
        attributes("Built-By" to "bierdosenhalter")
    }
    relocate("org.bstats", "org.zeroBzeroT.bstats")
    relocate("redis.clients", "org.zeroBzeroT.chatCo.shaded.redis")
    exclude("META-INF/maven/**", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
