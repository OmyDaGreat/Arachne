import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

val user: String by project
val dev: String by project
val mail: String by project
val devURL: String by project
val repo: String by project
val g: String by project
val artifact: String by project
val v: String by project
val desc: String by project
val inception: String by project

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
}

group = g
version = v

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    js {
        browser {
            testTask {
                useKarma {
                    useChromiumHeadless()
                }
            }
        }
        nodejs()
    }

    @Suppress("unused")
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(libs.kermit)
                implementation(libs.compose.runtime)
                implementation(libs.bundles.kobweb)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(g, artifact, v)

    pom {
        name = repo
        description = desc
        inceptionYear = inception
        url = "https://github.com/$user/$repo"
        licenses {
            license {
                name = "MIT License"
                url = "https://mit.malefic.xyz"
            }
        }
        developers {
            developer {
                name = dev
                email = mail
                url = devURL
            }
        }
        scm {
            url = "https://github.com/$user/$repo"
            connection = "scm:git:git://github.com/$user/$repo.git"
            developerConnection = "scm:git:ssh://github.com/$user/$repo.git"
        }
    }
}

dokka {
    pluginsConfiguration.html {
        footerMessage.set("&copy; 2025 $dev <$mail>")
    }
}
