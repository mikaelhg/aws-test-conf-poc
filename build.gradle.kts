import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.2"
	id("io.spring.dependency-management") version "1.0.12.RELEASE"
	kotlin("jvm") version "1.7.10"
	kotlin("plugin.spring") version "1.7.10"
}

group = "poc"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("io.awspring.cloud:spring-cloud-starter-aws:2.4.2")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	implementation("io.github.microutils:kotlin-logging:2.1.23")

	testImplementation("org.testcontainers:junit-jupiter:1.17.2")
	testImplementation("org.testcontainers:localstack:1.17.2")

	testImplementation("io.javalin:javalin:3.13.13")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}
