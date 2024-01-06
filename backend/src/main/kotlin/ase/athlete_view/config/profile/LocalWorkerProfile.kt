package ase.athlete_view.config.profile

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.ListContainersCmd
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("local-worker")
class LocalWorkerProfile {

    @PostConstruct
    fun startWorkerContainers(){
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
                "RMQ_HOST=$RMQ_HOST",
                "RMQ_PORT=$RMQ_PORT",
                "RMQ_USER=$RMQ_USER",
                "RMQ_PASSWORD=$RMQ_PASSWORD",
                "RMQ_REQUESTQUEUE=$RMQ_REQUESTQUEUE",
                "RMQ_RESPONSEQUEUE=$RMQ_RESPONSEQUEUE",
                "MAX_TIMEOUT=$MAX_TIMEOUT"

        )
        for (i in 1..1) {
            if (!doesContainerExist(dockerClient,"worker-$i")){
                val container = dockerClient.createContainerCmd(IMAGE_NAME)
                        .withName("worker-$i")
                        .withNetworkMode(NETWORK_NAME)
                        .withEnv(env)
                        .exec()

                dockerClient.startContainerCmd(container.id).exec()
            }
        }

        // Close Docker client after operation
        dockerClient.close()
    }

    private fun doesContainerExist(dockerClient: DockerClient, containerName: String): Boolean {
        val listContainersCmd: ListContainersCmd = dockerClient.listContainersCmd().withShowAll(true)
        val containers: List<Container> = listContainersCmd.exec()

        for (container in containers) {
            if (container.names.contains("/$containerName")) {
                return true // Container with the specified name exists
            }
        }
        return false // Container does not exist
    }

    companion object {
        const val IMAGE_NAME = "athlete-view-worker";
        const val NETWORK_NAME = "athlete_view"
        const val RMQ_HOST = "rabbitmq"
        const val RMQ_PORT = "5672"
        const val RMQ_USER = "guest"
        const val RMQ_PASSWORD = "guest"
        const val RMQ_REQUESTQUEUE = "athlete_view_request"
        const val RMQ_RESPONSEQUEUE ="athlete_view_response"
        const val MAX_TIMEOUT= "60"
    }
}