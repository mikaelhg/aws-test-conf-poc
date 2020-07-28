package poc.act.javalin

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory
import org.eclipse.jetty.http2.HTTP2Cipher
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory
import org.eclipse.jetty.server.*
import org.eclipse.jetty.util.ssl.SslContextFactory
import java.util.*


/**
 * Configuration of the embedded Jetty server is controlled here, including
 * activation of HTTPS and HTTP/2.
 */
object DemoJetty {
    fun create(): Server {
        val server = Server()
        val connector = ServerConnector(server)
        connector.port = 7001
        server.addConnector(connector)

        // HTTP Configuration
        val httpConfig = HttpConfiguration()
        httpConfig.sendServerVersion = false
        httpConfig.secureScheme = "https"
        val httpsPort = 7002
        httpConfig.securePort = httpsPort

        // SSL Context Factory for HTTPS and HTTP/2
        val sslContextFactory = SslContextFactory.Server().apply {
            keyStorePath = "/keystore/path"
            setKeyStorePassword("keystore.password")
            cipherComparator = HTTP2Cipher.COMPARATOR
        }

        // HTTPS Configuration
        val httpsConfig = HttpConfiguration(httpConfig).apply {
            addCustomizer(SecureRequestCustomizer())
        }

        // HTTP/2 Connection Factory
        val h2 = HTTP2ServerConnectionFactory(httpsConfig)
        val alpn = ALPNServerConnectionFactory().apply {
            defaultProtocol = "h2" // not to be confused with H2 DB.
        }

        // SSL Connection Factory
        val ssl = SslConnectionFactory(sslContextFactory, alpn.protocol)

        // HTTP/2 Connector
        val http2Connector = ServerConnector(server, ssl, alpn, h2, HttpConnectionFactory(httpsConfig))
        http2Connector.port = httpsPort
        server.addConnector(http2Connector)

        return server
    }
}