package poc.act.junit

import com.amazonaws.services.ec2.model.UserData
import com.amazonaws.util.EC2MetadataUtils
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy.UPPER_CAMEL_CASE
import io.javalin.Javalin
import io.javalin.plugin.json.JavalinJackson
import mu.KotlinLogging
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS
import java.time.temporal.ChronoUnit.SECONDS
import java.util.*


/**
 * We need to hook into the JUnit test lifecycle before the Spring context with its
 * AWS integration has been initialized. And later, after the testing is all done,
 * we want to switch off our web server.
 */
class MockAmazonWebServices : BeforeAllCallback, AfterAllCallback {

	companion object {
		const val ENDPOINT_OVERRIDE = "com.amazonaws.sdk.ec2MetadataServiceEndpointOverride"
		const val OVERRIDE_URL = "http://127.0.0.1:7000"
		val PROPERTIES = mapOf(
				"cloud.aws.stack.auto" to "false",
				"cloud.aws.region.auto" to "false",
				"cloud.aws.region.static" to "eu-north-1",
				"com.amazonaws.sdk.disableCertChecking" to "true",
				"aws.region" to "eu-north-1"
		)
	}

	private val logger = KotlinLogging.logger {}

	private lateinit var server: Javalin

	override fun beforeAll(context: ExtensionContext?) {
		logger.info { "creating mock AWS metadata service for ${context?.uniqueId}" }
		initializeServer()
		System.setProperty(ENDPOINT_OVERRIDE, OVERRIDE_URL)
		PROPERTIES.forEach { (k, v) -> System.setProperty(k, v) }
	}

	override fun afterAll(context: ExtensionContext?) {
		logger.info { "shutting down mock AWS metadata service" }
		System.clearProperty(ENDPOINT_OVERRIDE)
		PROPERTIES.forEach { (k, _) -> System.clearProperty(k) }
		server.stop()
	}

	private fun now() = Instant.now().truncatedTo(SECONDS).toString()

	private fun whileAgo() = Instant.now().minus(3, HOURS).truncatedTo(SECONDS).toString()

	private fun soon() = Instant.now().plus(12, HOURS).truncatedTo(SECONDS).toString()

	private fun initializeServer() {
		val om = ObjectMapper().apply {
			propertyNamingStrategy = UPPER_CAMEL_CASE
			setSerializationInclusion(NON_NULL)
		}
		JavalinJackson.configure(om)

		server = Javalin.create { config ->
			config.server {
				Server().apply {
					val sslContextFactory = SslContextFactory.Server().apply {
						keyStoreResource = Resource.newClassPathResource("/keystore")
						keyStoreType = "JKS"
						setKeyStorePassword("localhost")
					}
					connectors = arrayOf(
							ServerConnector(server, sslContextFactory).apply { port = 7001 },
							ServerConnector(server).apply { port = 7000 }
					)
				}
			}
		}
		server.start()

		server.before { ctx ->
			ctx.res.contentType = "text/plain"
			ctx.res.setHeader("Server", "EC2ws")
			logger.debug { "before ${ctx.method()} ${ctx.url()}" }
		}

		server.after { ctx ->
			logger.debug { "after" }
			logger.debug { ctx.resultString() }
		}

		server.get("/latest/meta-data/instance-id") { ctx ->
			ctx.result("i-123123123")
		}

		server.get("/latest/meta-data/iam/info") { ctx ->
			EC2MetadataUtils.IAMInfo().apply {
				code = "Success"
				lastUpdated = now()
				instanceProfileArn = "arn:aws:iam::123123:instance-profile/fake"
				instanceProfileId = "AIPAWFCBY6OJXXXXXXXXX"
			}.let { ctx.json(it) }
		}

		// https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html
		server.get("/latest/dynamic/instance-identity/document") { ctx ->
			EC2MetadataUtils.InstanceInfo(
                    now(),
                    "q2.weird",
                    "im-123123",
                    "i-123123123",
                    null,
                    "fake",
                    "123123",
                    "asd",
                    "asd",
                    "eu-north-1",
                    "2",
                    "eu-north-1a",
                    "1.2.3.4",
                    null
            ).let { ctx.json(it) }
		}

		server.get("/latest/user-data/") { ctx ->
			UserData().apply {
				data = null
			}.let { ctx.json(it) }
		}

		server.get("/latest/meta-data/iam/security-credentials/") { ctx ->
			ctx.result("fake")
		}

		server.get("/latest/meta-data/iam/security-credentials/fake") { ctx ->
			EC2MetadataUtils.IAMSecurityCredential().apply {
				code = "Success"
				lastUpdated = whileAgo()
				type = "AWS-HMAC"
				accessKeyId = "ASIXXXXXXXXXXXXXXXXX"
				secretAccessKey = "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY"
				token = Base64.getEncoder().encodeToString("X".repeat(796).toByteArray())
				expiration = soon()
			}.let { ctx.json(it) }
		}
	}

}