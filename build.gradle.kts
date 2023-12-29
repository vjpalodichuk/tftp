plugins {
    java
}

group = "com.capital7software.network"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs = listOf("-Xss4m")
}

tasks.register("receiver", JavaExec::class, ) {
    dependsOn(":build")
    group = "tftp.server"
    description = "Runs the TFTP Server"
    mainClass = "com.capital7software.network.tftp.server.TftpServer"
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register("sender", JavaExec::class, ) {
    dependsOn(":build")
    group = "tftp.client"
    description = "Runs the TFTP Client. Be sure to pass a file to send with the --args 'full-path-to-file' parameter"
    mainClass = "com.capital7software.network.tftp.client.FileClient"
    classpath = sourceSets["main"].runtimeClasspath
}
