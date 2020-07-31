package poc.act

import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName
import poc.act.container.BetterLocalStackContainer
import poc.act.junit.MockAmazonWebServices


@SpringBootTest
class ApplicationTests {

	private val logger = KotlinLogging.logger {}

	companion object {

		@RegisterExtension
		@JvmField
		val mockAmazonWebServices = MockAmazonWebServices()

		private val localStackImage = DockerImageName.parse("localstack/localstack:latest")

		@Container
		@JvmField
		val localstack = BetterLocalStackContainer().start()

	}

	@Test
	fun contextLoads() {
		logger.debug { "All the initialization has been done, and the actual test starts." }
		AmazonEC2ClientBuilder.defaultClient().describeInstances()
	}

}
