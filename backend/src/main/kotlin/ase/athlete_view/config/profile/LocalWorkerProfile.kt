package ase.athlete_view.config.profile

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.ListContainersCmd
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("local-worker")
class LocalWorkerProfile(
        @Value("\${worker.image_name}") private val imageName: String,
        @Value("\${worker.network_name}") private val networkName: String,
        @Value("\${worker.rabbitmq.host}") private val rmqHost: String,
        @Value("\${worker.rabbitmq.port}") private val rmqPort: String,
        @Value("\${worker.rabbitmq.user}") private val rmqUser: String,
        @Value("\${worker.rabbitmq.password}") private val rmqPassword: String,
        @Value("\${worker.rabbitmq.request_queue}") private val rmqRequestQueue: String,
        @Value("\${worker.rabbitmq.response_queue}") private val rmqResponseQueue: String,
        @Value("\${worker.max_timeout}") private val maxTimeout: String,
        @Value("\${worker.count}") private val workerCount: Int
) {

    @PostConstruct
    fun startWorkerContainers() {
        val dockerClientConfig: DockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:2375")
                .withDockerTlsVerify(false)
                .build()

        val dockerHttpClient = ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.dockerHost)
                .sslConfig(dockerClientConfig.sslConfig)
                .build()

        val dockerClient = DockerClientBuilder.getInstance(dockerClientConfig).withDockerHttpClient(dockerHttpClient).build()

        // Creating containers in the "ase" network
        val env = listOf(
                "RMQ_HOST=$rmqHost",
                "RMQ_PORT=$rmqPort",
                "RMQ_USER=$rmqUser",
                "RMQ_PASSWORD=$rmqPassword",
                "RMQ_REQUESTQUEUE=$rmqRequestQueue",
                "RMQ_RESPONSEQUEUE=$rmqResponseQueue",
                "MAX_TIMEOUT=$maxTimeout"
        )

        for (i in 1..workerCount) {
            cleanup(dockerClient, "worker-$i")
            if (!exists(dockerClient, "worker-$i")) {
                val container = dockerClient.createContainerCmd(imageName)
                        .withName("worker-$i")
                        .withNetworkMode(networkName)
                        .withEnv(env)
                        .exec()

                dockerClient.startContainerCmd(container.id).exec()
            }
        }

        // Close Docker client after operation
        dockerClient.close()
    }

    private fun exists(dockerClient: DockerClient, containerName: String): Boolean {
        val listContainersCmd: ListContainersCmd = dockerClient.listContainersCmd().withShowAll(true)
        val containers: List<Container> = listContainersCmd.exec()

        for (container in containers) {
            if (container.names.contains("/$containerName") && container.state == "running") {
                return true
            }
        }
        return false
    }

    private fun cleanup(dockerClient: DockerClient, containerName: String) {
        val listContainersCmd: ListContainersCmd = dockerClient.listContainersCmd().withShowAll(true)
        val containers: List<Container> = listContainersCmd.exec()

        for (container in containers) {
            if (container.names.contains("/$containerName") && container.state != "running") {
                dockerClient.removeContainerCmd(container.id).exec()
            }
        }
    }

}