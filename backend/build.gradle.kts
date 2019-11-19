import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.ajoberstar.grgit.Grgit

plugins {
	id("org.springframework.boot") version "2.2.0.RELEASE"
	id("io.spring.dependency-management") version "1.0.8.RELEASE"
	kotlin("jvm") version "1.3.50"
	kotlin("plugin.spring") version "1.3.50"

	//plugins for publishing docker image
	id("com.palantir.docker") version "0.22.1"
	id("org.ajoberstar.grgit") version "3.0.0"
}

group = "nl.delft.tu.iot.seminar.cyclerr"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
}

extra["springCloudVersion"] = "Hoxton.RC1"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-hateoas")
//	implementation("org.springframework.boot:spring-boot-starter-security")

	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("org.springframework.cloud:spring-cloud-stream")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
	testImplementation("org.springframework.cloud:spring-cloud-stream-test-support")
//	testImplementation("org.springframework.security:spring-security-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}



//docker tasks
tasks.create<Copy>("unpack") {
	val bootJar = tasks.bootJar.get()
	dependsOn(bootJar)
	from(zipTree(bootJar.outputs.files.singleFile))
	into("build/dependency")
}

fun commitId(): String{
    try {
        return Grgit.open().head().abbreviatedId
    } catch (e: IllegalStateException) {
        println(e)
        return "00000"
    }
}

docker {
//	val grgit =
//	val commitId =
	name = "cloud.canister.io:5000/cyclerr/cyclerr-backend:0.1-${commitId()}"
	tag("latest", "cloud.canister.io:5000/cyclerr/cyclerr-backend:latest")
	copySpec.from(tasks.getByName<Copy>("unpack").outputs).into("dependency")
	buildArgs(mapOf("DEPENDENCY" to "dependency"))
}