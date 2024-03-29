/*
 * Copyright 2023 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

///////////////////////////////
//////    PUBLISH TO SONATYPE / MAVEN CENTRAL
////// TESTING : (to local maven repo) <'publish and release' - 'publishToMavenLocal'>
////// RELEASE : (to sonatype/maven central), <'publish and release' - 'publishToSonatypeAndRelease'>
///////////////////////////////

gradle.startParameter.showStacktrace = ShowStacktrace.ALWAYS   // always show the stacktrace!

plugins {
    id("com.dorkbox.GradleUtils") version "3.18"
    id("com.dorkbox.Licensing") version "2.24"
    id("com.dorkbox.VersionUpdate") version "2.8"
    id("com.dorkbox.GradlePublish") version "1.22"

    kotlin("jvm") version "1.9.0"
}

object Extras {
    // set for the project
    const val description = "Kryo based serializers"
    const val group = "com.dorkbox"
    const val version = "2.9"

    // set as project.ext
    const val name = "Serializers"
    const val id = "Serializers"
    const val vendor = "Dorkbox LLC"
    const val vendorUrl = "https://dorkbox.com"
    const val url = "https://git.dorkbox.com/dorkbox/Serializers"
}

///////////////////////////////
/////  assign 'Extras'
///////////////////////////////
GradleUtils.load("$projectDir/../../gradle.properties", Extras)
GradleUtils.defaults()
GradleUtils.compileConfiguration(JavaVersion.VERSION_1_8)
GradleUtils.jpms(JavaVersion.VERSION_1_9)


licensing {
    license(License.APACHE_2) {
        description(Extras.description)
        author(Extras.vendor)
        url(Extras.url)

        extra("Kryo Serializers", License.APACHE_2) {
            url("https://github.com/magro/kryo-serializers")
            copyright(2021)
            author("Martin Grotzke")
            author("Rafael Winterhalter")
        }
    }
}


kotlin {
    sourceSets {
        main {
            // we have some java we depend on
            kotlin.include("**/*.java", "**/*.kt")
        }
        test {
            kotlin.include("**/*.java", "**/*.kt")
        }
    }
}


tasks.jar.get().apply {
    manifest {
        // https://docs.oracle.com/javase/tutorial/deployment/jar/packageman.html
        attributes["Name"] = Extras.name

        attributes["Specification-Title"] = Extras.name
        attributes["Specification-Version"] = Extras.version
        attributes["Specification-Vendor"] = Extras.vendor

        attributes["Implementation-Title"] = "${Extras.group}.${Extras.id}"
        attributes["Implementation-Version"] = GradleUtils.now()
        attributes["Implementation-Vendor"] = Extras.vendor
    }
}

dependencies {
    api("com.dorkbox:JNA:1.0")
    api("com.dorkbox:Updates:1.1")


    // how we bypass using reflection to access fields
    api("org.javassist:javassist:3.29.2-GA")


    val jnaVersion = "5.12.1"
    api("net.java.dev.jna:jna-jpms:${jnaVersion}")
    api("net.java.dev.jna:jna-platform-jpms:${jnaVersion}")


    // optionally using KRYO
    compileOnly("com.esotericsoftware:kryo:5.5.0")

    val bcVersion = "1.70"
    // listed as compile only, since we will be optionally be using bouncy castle if we use this project. **We don't want a hard dependency.**
    compileOnly("org.bouncycastle:bcprov-jdk15on:$bcVersion")


    testImplementation("org.bouncycastle:bcprov-jdk15on:$bcVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.esotericsoftware:kryo:5.5.0")
}

publishToSonatype {
    groupId = Extras.group
    artifactId = Extras.id
    version = Extras.version

    name = Extras.name
    description = Extras.description
    url = Extras.url

    vendor = Extras.vendor
    vendorUrl = Extras.vendorUrl

    issueManagement {
        url = "${Extras.url}/issues"
        nickname = "Gitea Issues"
    }

    developer {
        id = "dorkbox"
        name = Extras.vendor
        email = "email@dorkbox.com"
    }
}
