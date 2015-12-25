package org.tomitribe.lieutenant.docker;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class DockerTest {

    private static Docker docker;

    @BeforeClass
    public static void createDockerClient() {
        Properties properties = new Properties();
        properties.put("docker.io.url", "https://192.168.99.100:2376");
        final String certPath = System.getProperty("user.home") + "/.docker/machine/machines/dev";
        properties.put("docker.io.dockerCertPath", certPath);

        Docker.DockerConfig dockerConfig = new Docker.DockerConfig();
        docker = dockerConfig.withProperties(properties).build();
    }

    @Test
    public void shouldBuildDockerContainers() {

        try {
            File dockerFile = new File(Thread.currentThread().getContextClassLoader()
                    .getResource("simple/TestDockerFile").getFile());

            docker.build(dockerFile, false, "lieutenant/test", "mytag");
            assertThat(docker.imageId("lieutenant/test", "mytag"), notNullValue());
        } finally {
            docker.remove("lieutenant/test", "mytag");
        }
    }

    @Test
    public void shouldTagAnImage() {

        try {
            File dockerFile = new File(Thread.currentThread().getContextClassLoader()
                    .getResource("simple/TestDockerFile").getFile());

            docker.build(dockerFile, false, "lieutenant/test", "mytag");
            docker.tag("lieutenant/test", "mytag", "myothertag");

            assertThat(docker.imageId("lieutenant/test", "myothertag"), notNullValue());
        } finally {
            docker.remove("lieutenant/test", "mytag");
            docker.remove("lieutenant/test", "myothertag");
        }
    }

    @Test
    public void shouldReturnFalseInCaseOfNotExistingImage() {
        assertThat(docker.imageExists("alex", "soto"), is(false));
    }

}
