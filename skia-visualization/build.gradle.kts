plugins {
    kotlin("multiplatform") version "1.7.10"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

//project.properties["skiko.version"] = "0.7.27"

var version = "0.7.27" // "0.0.0-SNAPSHOT"
if (project.hasProperty("skiko.version")) {
    version = project.properties["skiko.version"] as String
}

val resourcesDir = "$buildDir/resources/"

val skikoWasm by configurations.creating

//val tempTask = tasks.named("jsDevelopmentExecutableCompileSync").get().property("duplicatesStrategy") = DuplicatesStrategy.INCLUDE

gradle.taskGraph.whenReady {

    allTasks
        .filter { it.hasProperty("duplicatesStrategy") } // Because it's some weird decorated wrapper that I can't cast.
        .forEach {
            it.setProperty("duplicatesStrategy", "INCLUDE")
        }
}


dependencies {
    skikoWasm("org.jetbrains.skiko:skiko-js-wasm-runtime:$version")
}

val unzipTask = tasks.register("unzipWasm", Copy::class) {
    destinationDir = file(resourcesDir)
    from(skikoWasm.map { zipTree(it) })

//    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    dependsOn(unzipTask)

}


kotlin {

    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.skiko:skiko:$version")
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
            resources.setSrcDirs(resources.srcDirs)
            resources.srcDirs(unzipTask.map { it.destinationDir })
        }
    }


    val compileSync = tasks.named("jsDevelopmentExecutableCompileSync").get()
    val jsBrowserDevelopmentRun = tasks.named("jsBrowserDevelopmentRun").get()
    jsBrowserDevelopmentRun.dependsOn(compileSync)

}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}

// Fixes webpack-cli incompatibility by pinning the newest version.
rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
    versions.webpackCli.version = "4.10.0"
}

//tasks.all {
//    println(name)
//}

