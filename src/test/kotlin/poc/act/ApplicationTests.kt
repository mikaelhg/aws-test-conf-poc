package poc.act

import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Container
import poc.act.container.BetterLocalStackContainer
import poc.act.junit.MockAmazonWebServices

@SpringBootTest
@DisplayName("POC")
class ApplicationTests {

	private val logger = KotlinLogging.logger {}

	companion object {

		@RegisterExtension
		@JvmField
		val mockAmazonWebServices = MockAmazonWebServices()

		@Container
		@JvmField
		val localstack = BetterLocalStackContainer().start()

	}

	@Test
	@DisplayName("call LocalStack describeInstances with mock credentials")
	fun describeInstancesFromLocalStackWithCredentials() {
		logger.debug { "All the initialization has been done, and the actual test starts." }
		val instances = AmazonEC2ClientBuilder.defaultClient().describeInstances()
		logger.debug { "instances: $instances" }
	}

	@Test
	@DisplayName("AWS library fetches credentials from mock web server")
	fun fetchMockCredentials() {
		val creds = EC2ContainerCredentialsProviderWrapper().credentials
		Assertions.assertEquals(MockAmazonWebServices.ACCESS_KEY_ID, creds.awsAccessKeyId)
		Assertions.assertEquals(MockAmazonWebServices.SECRET_ACCESS_KEY, creds.awsSecretKey)
	}

}
