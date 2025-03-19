import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "de.infinityprojects"
version = "1.0.6-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.minestom:minestom-snapshots:1_21_2-805e83b0a2")
    implementation("org.apache.logging.log4j:log4j-api:2.23.1")
    // implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    annotationProcessor("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.minecrell:terminalconsoleappender:1.3.0")
    implementation("org.jline:jline-terminal:3.26.3")
    implementation("org.jline:jline-reader:3.26.3")
    implementation("org.jline:jline-terminal-jna:3.26.3")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("de.articdive:jnoise-pipeline:4.1.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Minestom has a minimum Java version of 21
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "de.infinityprojects.mcserver.MainKt" // Change this to your main class
        }
    }

    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix on the shadowjar file.
        transform(Log4j2PluginsCacheFileTransformer::class.java)
    }

    processResources {
        /* inputs.property("group", project.group)
        inputs.property("name", project.name)
        inputs.property("description", project.description)
        inputs.property("year", Calendar.getInstance().get(Calendar.YEAR))
        inputs.property("author", "InfinityProjects")
        inputs.property("website", "https://infinityprojects.de")
        inputs.property("license", "MIT")
        inputs.property("github", "")*/

        val props =
            mapOf(
                "version" to project.version,
            )

        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("melius.properties") {
            expand(props)
        }
    }
}

task("runDev") {
    dependsOn("shadowJar")
    doLast {
        javaexec {
            mainClass = "de.infinityprojects.mcserver.MainKt"
            classpath = files("build/libs/${project.name}-${project.version}.jar")
            workingDir = file("run/")
        }
    }
}
