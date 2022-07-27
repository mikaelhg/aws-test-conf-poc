package poc.act.container

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

class BetterLocalStackContainer : GenericContainer<BetterLocalStackContainer>(localStackImage) {

    companion object {
        private val localStackImage = DockerImageName.parse("localstack/localstack:latest")
    }

    override fun configure() {
        super.configure()
        addFixedExposedPort(4566, 4566)
        withEnv("LOCALHOST_SERVICES", "ec2,s3,cloudformation")
        withFileSystemBind("/var/run/docker.sock", "/var/run/docker.sock")
        waitingFor(Wait.forLogMessage(".*Ready\\.\n", 1))
    }

}
