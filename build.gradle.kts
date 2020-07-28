import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.3.1.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	kotlin("jvm") version "1.3.72"
	kotlin("plugin.spring") version "1.3.72"
}

group = "poc"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform("org.springframework.cloud:spring-cloud-dependencies:Hoxton.SR6"))
	implementation(platform("com.amazonaws:aws-java-sdk-bom:1.11.827"))

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.cloud:spring-cloud-starter-aws")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	implementation("io.github.microutils:kotlin-logging:1.8.3")

	testImplementation("org.testcontainers:junit-jupiter:1.15.0-rc1")
	testImplementation("org.testcontainers:localstack:1.15.0-rc1")

	testImplementation("io.javalin:javalin:3.9.1")
	testImplementation("org.eclipse.jetty.http2:http2-server:9.4.29.v20200521")
	testImplementation("org.eclipse.jetty:jetty-alpn-java-server:9.4.29.v20200521")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
}

tasks {
	test {
		useJUnitPlatform()
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}